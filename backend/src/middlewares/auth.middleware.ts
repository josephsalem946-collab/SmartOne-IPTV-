import { Request, Response, NextFunction } from "express";
import { firebaseAuth } from "../config/firebase";

export interface AuthenticatedRequest extends Request {
  user?: {
    uid: string;
    email?: string;
    role?: string;
  };
}

/**
 * Validates Firebase ID Token from Authorization header: Bearer <token>
 */
export async function authenticateFirebaseUser(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({
      error: "UNAUTHORIZED",
      message: "Jeton d'authentification manquant ou format invalide."
    });
    return;
  }

  const token = authHeader.split("Bearer ")[1].trim();

  try {
    const decodedToken = await firebaseAuth.verifyIdToken(token, true); // checkRevoked = true
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email,
      role: (decodedToken.role as string) || "client"
    };
    next();
  } catch (error) {
    res.status(401).json({
      error: "INVALID_TOKEN",
      message: "Session expirée ou jeton d'authentification invalide."
    });
  }
}

/**
 * Role-Based Access Control (Admin only)
 */
export function requireAdminRole(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): void {
  const adminEmail = process.env.ADMIN_EMAIL || "supportdisponible@gmail.com";
  const allowedAdminEmails = [adminEmail, "admin@smartoneiptv.com", "supportdisponible@gmail.com"];
  if (req.user?.role !== "admin" && (!req.user?.email || !allowedAdminEmails.includes(req.user.email))) {
    res.status(403).json({
      error: "FORBIDDEN",
      message: "Accès réservé exclusivement aux administrateurs."
    });
    return;
  }
  next();
}
