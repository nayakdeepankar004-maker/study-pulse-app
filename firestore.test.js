const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DB_ID = "ai-studio-android-studypul-f2dd2f8d-bcb5-4e6d-8470-954c7a6cf12c";
const ALICE_UID = "alice_uid_1";
const BOB_UID = "bob_uid_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const validTs = () => new Date(Date.now() - 2000);

function validProfile(uid) {
  const ts = validTs();
  return {
    userId: uid,
    displayName: "Prince",
    email: "prince@example.com",
    selectedStream: "NEET",
    selectedSubjectFilter: "ALL",
    dailyTargetLectures: 4,
    dailyTargetHours: 6,
    loggedHoursToday: 2.5,
    streak: 7,
    alarmTone: "neon",
    lastLoggedDate: "2026-11-14",
    isSeeded: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validTask(uid) {
  const ts = validTs();
  return {
    userId: uid,
    title: "Motion in a Straight Line",
    subject: "Physics",
    date: "2026-11-14",
    time: "10:00",
    repeatCode: "daily",
    notes: "Solve 15 MCQs",
    completed: false,
    celebrated: false,
    firedKey: "",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validGoal(uid) {
  const ts = validTs();
  return {
    userId: uid,
    title: "Complete Kinematics",
    spanLabel: "Next 2 Days",
    subject: "Physics",
    done: false,
    celebrated: false,
    createdAt: ts,
    updatedAt: ts,
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
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or write profiles, tasks, or goals", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DB_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").doc("t1").set(validTask(ALICE_UID)));
});

test("Authenticated owner can create, read, update, and delete own profile, tasks, and goals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DB_ID);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  const taskRef = profileRef.collection("tasks").doc("t_1");
  const goalRef = profileRef.collection("goals").doc("g_1");

  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(profileRef.get());
  await assertSucceeds(profileRef.update({ streak: 8, updatedAt: validTs() }));

  await assertSucceeds(taskRef.set(validTask(ALICE_UID)));
  await assertSucceeds(profileRef.collection("tasks").get());
  await assertSucceeds(taskRef.update({ completed: true, celebrated: true, updatedAt: validTs() }));
  await assertSucceeds(taskRef.delete());

  await assertSucceeds(goalRef.set(validGoal(ALICE_UID)));
  await assertSucceeds(profileRef.collection("goals").get());
  await assertSucceeds(goalRef.update({ done: true, updatedAt: validTs() }));
  await assertSucceeds(goalRef.delete());
});

test("Cross-user access is strictly denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DB_ID);
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("t1").set(validTask(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DB_ID);
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("tasks").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("tasks").doc("t1").update({ completed: true, updatedAt: validTs() }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("tasks").doc("t2").set(validTask(BOB_UID)));
});

test("Shadow fields, invalid enums, future timestamps, and spoofed userId are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DB_ID);
  const taskRef = aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("t_bad");

  const shadowPayload = { ...validTask(ALICE_UID), isAdmin: true };
  await assertFails(taskRef.set(shadowPayload));

  const badEnum = { ...validTask(ALICE_UID), repeatCode: "weekly" };
  await assertFails(taskRef.set(badEnum));

  const futureTs = { ...validTask(ALICE_UID), createdAt: new Date(Date.now() + 60000) };
  await assertFails(taskRef.set(futureTs));

  const spoofedUid = { ...validTask(BOB_UID) };
  await assertFails(taskRef.set(spoofedUid));
});
