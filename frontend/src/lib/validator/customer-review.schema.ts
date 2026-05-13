import { z } from "zod";

export const ratingField = z
	.number({ error: "Selecione uma nota de 1 a 5 estrelas" })
	.int({ error: "Nota inválida" })
	.min(1, { error: "Nota mínima é 1 estrela" })
	.max(5, { error: "Nota máxima é 5 estrelas" });

export const commentField = z
	.string()
	.max(1000, { error: "Comentário deve ter no máximo 1000 caracteres" });

export const createReviewSchema = z.object({
	rating: ratingField,
	comment: commentField,
});
export type CreateReviewFormValues = z.infer<typeof createReviewSchema>;

export const updateReviewSchema = createReviewSchema;
export type UpdateReviewFormValues = z.infer<typeof updateReviewSchema>;
