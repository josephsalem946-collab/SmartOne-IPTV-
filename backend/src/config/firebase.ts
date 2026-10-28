import * as admin from "firebase-admin";
import { ENV } from "./env";

// Initialize Firebase Admin SDK using verified environment credentials
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: ENV.FIREBASE_PROJECT_ID,
      clientEmail: ENV.FIREBASE_CLIENT_EMAIL,
      privateKey: ENV.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n")
    })
  });
}

export const firebaseAuth = admin.auth();
export const firestore = admin.firestore();
