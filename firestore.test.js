const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read or write any user profile, notes, pdfs, or testAttempts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Unauth",
      board: "CBSE",
      studentClass: "Class 11",
      stream: "Science PCM",
      targetExam: "JEE Main",
      onboardingCompleted: true,
      totalTestsTaken: 0,
      bestAccuracy: 0,
      streakDays: 1,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated owner: valid UserProfile create & partial update succeed; ghost/future/spoof fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav Sharma",
      email: "aarav@example.com",
      board: "CBSE",
      studentClass: "Class 12",
      stream: "Science PCB",
      targetExam: "NEET",
      onboardingCompleted: true,
      totalTestsTaken: 0,
      bestAccuracy: 0,
      streakDays: 1,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update on allowed keys
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalTestsTaken: 1,
      bestAccuracy: 92,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field on update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      streakDays: 2,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice profile, notes, pdfs, or testAttempts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      board: "ICSE",
      studentClass: "Class 10",
      stream: "Foundation",
      targetExam: "Board Exams",
      onboardingCompleted: true,
      totalTestsTaken: 0,
      bestAccuracy: 0,
      streakDays: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      title: "Electrostatics Laws",
      subject: "Physics",
      content: "Gauss law relates electric flux to enclosed charge.",
      pinned: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated owner: notes, pdfs, and testAttempts CRUD and validation bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Notes create, list, update, delete
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      title: "Organic Chemistry Reactions",
      subject: "Chemistry",
      content: "Aldol condensation involves enolate addition.",
      pinned: false,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      pinned: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject @DocumentId 'id' inside note payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_bad").set({
      id: "note_bad",
      userId: ALICE_UID,
      title: "Bad Note",
      subject: "Physics",
      content: "Should fail",
      pinned: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // PDFs create & list
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("pdfs").doc("pdf_1").set({
      userId: ALICE_UID,
      title: "NCERT_Physics_Ch1.pdf",
      subject: "Physics",
      uriString: "content://com.android.providers.downloads/document/101",
      pageCount: 28,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("pdfs").get());

  // TestAttempts create & list; updates are immutable/disallowed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("testAttempts").doc("att_1").set({
      userId: ALICE_UID,
      title: "NEET Biology Mock 1",
      track: "NEET",
      subject: "Biology",
      totalQuestions: 10,
      correctCount: 8,
      score: 30,
      maxScore: 40,
      accuracyPercent: 80,
      durationSeconds: 320,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("testAttempts").get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("testAttempts").doc("att_1").update({
      score: 40,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
