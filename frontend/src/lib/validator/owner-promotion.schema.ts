import { z } from "zod";

const DISCOUNT_KINDS = ["PCT", "AMT"] as const;
export type DiscountKind = (typeof DISCOUNT_KINDS)[number];

export const discountKindField = z.enum(DISCOUNT_KINDS, {
	error: "Selecione o tipo de desconto",
});

const isoDateRegex = /^\d{4}-\d{2}-\d{2}$/;

const promotionShape = {
	name: z
		.string()
		.min(2, "Mínimo de 2 caracteres")
		.max(150, "Máximo de 150 caracteres"),
	description: z.string().max(500, "Máximo de 500 caracteres"),
	discountKind: discountKindField,
	discountPercentage: z.string(),
	discountAmount: z.string(),
	validFrom: z.string().regex(isoDateRegex, "Data inválida"),
	validTo: z.string().regex(isoDateRegex, "Data inválida"),
	eligibleServiceIds: z.array(z.number().int().positive()),
};

function refinePromotion<T extends z.ZodTypeAny>(schema: T): T {
	return schema.superRefine((v, ctx) => {
		const value = v as {
			discountKind: DiscountKind;
			discountPercentage: string;
			discountAmount: string;
			validFrom: string;
			validTo: string;
		};
		if (value.discountKind === "PCT") {
			const n = Number(value.discountPercentage);
			if (!Number.isFinite(n) || n <= 0 || n > 100) {
				ctx.addIssue({
					code: "custom",
					path: ["discountPercentage"],
					message: "Entre 0,01 e 100",
				});
			}
		} else {
			const n = Number(value.discountAmount);
			if (!Number.isFinite(n) || n <= 0) {
				ctx.addIssue({
					code: "custom",
					path: ["discountAmount"],
					message: "Mínimo R$ 0,01",
				});
			}
		}
		if (
			isoDateRegex.test(value.validFrom) &&
			isoDateRegex.test(value.validTo) &&
			value.validTo < value.validFrom
		) {
			ctx.addIssue({
				code: "custom",
				path: ["validTo"],
				message: "Fim antes do início",
			});
		}
	});
}

export const createPromotionSchema = refinePromotion(z.object(promotionShape));
export type CreatePromotionFormValues = z.infer<typeof createPromotionSchema>;

export const updatePromotionSchema = refinePromotion(
	z.object({ ...promotionShape, active: z.boolean() }),
);
export type UpdatePromotionFormValues = z.infer<typeof updatePromotionSchema>;
