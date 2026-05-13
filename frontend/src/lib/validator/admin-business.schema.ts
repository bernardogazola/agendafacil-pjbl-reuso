import { z } from "zod";
import type { CancellationPolicyType } from "@/lib/types";
import { emailField } from "./auth.schema";
import {
	businessTradeNameField,
	categoryField,
	phoneField,
	planField,
} from "./signup.schema";

const CANCELLATION_POLICIES = [
	"FREE",
	"DEADLINE",
	"FEE_BASED",
] as const satisfies readonly CancellationPolicyType[];

export const cancellationPolicyTypeField = z.enum(CANCELLATION_POLICIES, {
	error: "Selecione uma política de cancelamento",
});

export const updateBusinessSchema = z.object({
	tradeName: businessTradeNameField,
	email: emailField,
	phone: phoneField,
	category: categoryField,
	plan: planField,
	cancellationPolicyType: cancellationPolicyTypeField,
});

export type UpdateBusinessFormValues = z.infer<typeof updateBusinessSchema>;
