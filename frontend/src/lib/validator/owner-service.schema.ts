import { z } from "zod";
import type { PricingPolicyType } from "@/lib/types";

const PRICING_POLICIES = [
	"FIXED",
	"DURATION_BASED",
	"DISCOUNTED",
] as const satisfies readonly PricingPolicyType[];

export const pricingPolicyTypeField = z.enum(PRICING_POLICIES, {
	error: "Selecione uma política de preço",
});

const serviceShape = {
	name: z
		.string()
		.min(2, "Mínimo de 2 caracteres")
		.max(150, "Máximo de 150 caracteres"),
	basePrice: z
		.number({ error: "Informe um valor numérico" })
		.min(0.01, "Preço mínimo R$ 0,01"),
	durationMinutes: z
		.number({ error: "Informe um valor numérico" })
		.int("Use minutos inteiros")
		.min(1, "Duração mínima de 1 min"),
	description: z.string().max(500, "Máximo de 500 caracteres"),
	pricingPolicyType: pricingPolicyTypeField,
};

export const createOfferedServiceSchema = z.object(serviceShape);
export type CreateOfferedServiceFormValues = z.infer<
	typeof createOfferedServiceSchema
>;

export const updateOfferedServiceSchema = z.object(serviceShape);
export type UpdateOfferedServiceFormValues = z.infer<
	typeof updateOfferedServiceSchema
>;
