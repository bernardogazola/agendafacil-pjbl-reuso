import { z } from "zod";

export const nameField = z
	.string()
	.min(1, "Nome é obrigatório")
	.min(2, "Nome muito curto")
	.max(100, "Nome muito longo");

export const emailField = z
	.string()
	.min(1, "E-mail é obrigatório")
	.max(32, "E-mail inválido")
	.pipe(z.email({ error: "E-mail inválido" }));

export const PASSWORD_REQUIREMENTS = [
	{ schema: z.string().min(8), text: "Ao menos 8 caracteres" },
	{ schema: z.string().regex(/\d/), text: "Ao menos 1 número" },
	{ schema: z.string().regex(/[a-z]/), text: "Ao menos 1 letra minúscula" },
	{ schema: z.string().regex(/[A-Z]/), text: "Ao menos 1 letra maiúscula" },
] as const satisfies ReadonlyArray<{ schema: z.ZodString; text: string }>;

export const PASSWORD_STRENGTH_LABELS = {
	empty: "Digite uma senha",
	weak: "Senha fraca",
	medium: "Senha média",
	strong: "Senha forte",
} as const;

export function getPasswordStrengthLabel(score: number, total: number): string {
	if (score === 0) return PASSWORD_STRENGTH_LABELS.empty;
	const ratio = score / total;
	if (ratio < 0.5) return PASSWORD_STRENGTH_LABELS.weak;
	if (ratio < 1) return PASSWORD_STRENGTH_LABELS.medium;
	return PASSWORD_STRENGTH_LABELS.strong;
}

export const passwordField = z
	.string()
	.min(1, "Senha é obrigatória")
	.max(128, "Senha muito longa")
	.refine(
		(val) =>
			PASSWORD_REQUIREMENTS.every(({ schema }) => schema.safeParse(val).success),
		{ message: "Senha não atende aos requisitos" },
	);

export const confirmPasswordField = z.string().min(1, "Confirme sua senha");

const passwordsMatch = (v: { password: string; confirmPassword: string }) =>
	v.password === v.confirmPassword;

const passwordMismatch = {
	path: ["confirmPassword"] as ["confirmPassword"],
	error: "As senhas não coincidem",
};

export const credentialsSchema = z
	.object({
		name: nameField,
		email: emailField,
		password: passwordField,
		confirmPassword: confirmPasswordField,
	})
	.refine(passwordsMatch, passwordMismatch);

export const loginSchema = z.object({
	email: emailField,
	password: z.string().min(1, "Senha é obrigatória").max(128, "Senha inválida"),
});
