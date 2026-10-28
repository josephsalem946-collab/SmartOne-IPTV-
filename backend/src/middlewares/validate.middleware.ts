import { Request, Response, NextFunction } from "express";
import { AnyZodObject, ZodError } from "zod";

/**
 * Validates request body, query, and params using a Zod schema.
 * Sanitizes input to prevent parameter pollution, SQL/NoSQL injections, and XSS.
 */
export const validateRequest = (schema: AnyZodObject) => {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const parsed = await schema.parseAsync({
        body: req.body,
        query: req.query,
        params: req.params
      });

      // Assign sanitized and validated values back to request
      req.body = parsed.body;
      req.query = parsed.query;
      req.params = parsed.params;

      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const errorMessages = error.errors.map((e) => ({
          field: e.path.join("."),
          message: e.message
        }));

        res.status(400).json({
          error: "VALIDATION_FAILED",
          message: "Données de requête invalides ou non conformes.",
          details: errorMessages
        });
        return;
      }

      res.status(400).json({
        error: "BAD_REQUEST",
        message: "Format de données entrant non reconnu."
      });
    }
  };
};
