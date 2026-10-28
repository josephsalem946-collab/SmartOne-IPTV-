import rateLimit from "express-rate-limit";
import { AuthenticatedRequest } from "./auth.middleware";

// Global API rate limiter (protecting server against DoS and scraping)
export const globalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: "TOO_MANY_REQUESTS",
    message: "Trop de requêtes effectuées. Veuillez réessayer dans quelques minutes."
  }
});

// Stricter rate limiter for sensitive payment creation endpoints
export const paymentCreationLimiter = rateLimit({
  windowMs: 10 * 60 * 1000, // 10 minutes
  max: 10, // Max 10 payment initiation attempts per window
  keyGenerator: (req) => {
    // Key by authenticated user UID if present, otherwise by IP
    const authReq = req as AuthenticatedRequest;
    return authReq.user?.uid || req.ip || "unknown_ip";
  },
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: "RATE_LIMIT_EXCEEDED",
    message: "Limite de création de transactions atteinte. Patientez avant de réessayer."
  }
});

// Webhook / IPN rate limiter
export const webhookLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute
  max: 60,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: "WEBHOOK_RATE_LIMIT",
    message: "Débit excessif de notifications IPN."
  }
});
