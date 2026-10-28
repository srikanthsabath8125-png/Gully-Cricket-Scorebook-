const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const SCORER_UID = "scorer_alice";
const SPECTATOR_BOB_UID = "spectator_bob";
const MATCH_CODE = "GC7492";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function createValidMatchPayload(code = MATCH_CODE, scorer = SCORER_UID) {
  const now = new Date();
  return {
    matchCode: code,
    scorerUid: scorer,
    team1Name: "Royal Strikers",
    team2Name: "Gully Kings",
    totalOvers: 5,
    tossWinner: "Royal Strikers",
    tossDecision: "BAT",
    currentInnings: 1,
    battingTeam: "Royal Strikers",
    bowlingTeam: "Gully Kings",
    runs: 24,
    wickets: 1,
    status: "LIVE",
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated rejection
test("Unauthenticated: cannot read match", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("matches").doc(MATCH_CODE).get());
});

test("Unauthenticated: cannot create match", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("matches").doc(MATCH_CODE).set(createValidMatchPayload()));
});

// 2. Authenticated scorer can create valid match
test("Scorer: can create valid match document", async () => {
  const aliceDb = testEnv.authenticatedContext(SCORER_UID).firestore();
  await assertSucceeds(aliceDb.collection("matches").doc(MATCH_CODE).set(createValidMatchPayload()));
});

// 3. Authenticated spectator can read match by code
test("Spectator: can read live match by match code", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("matches").doc(MATCH_CODE).set(createValidMatchPayload());
  });

  const bobDb = testEnv.authenticatedContext(SPECTATOR_BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("matches").doc(MATCH_CODE).get());
});

// 4. Non-scorer cannot update another scorer's match
test("Spectator: cannot update scorer's match", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("matches").doc(MATCH_CODE).set(createValidMatchPayload());
  });

  const bobDb = testEnv.authenticatedContext(SPECTATOR_BOB_UID).firestore();
  await assertFails(
    bobDb.collection("matches").doc(MATCH_CODE).update({
      runs: 30,
      updatedAt: new Date(),
    })
  );
});

// 5. Scorer can update their own match
test("Scorer: can update score on their own match", async () => {
  const initial = createValidMatchPayload();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("matches").doc(MATCH_CODE).set(initial);
  });

  const aliceDb = testEnv.authenticatedContext(SCORER_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc(MATCH_CODE).set({
      ...initial,
      runs: 30,
      updatedAt: new Date(),
    })
  );
});
