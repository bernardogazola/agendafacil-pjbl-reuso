import { z } from "zod";
import type { AccessLevel } from "@/lib/types";
import { emailField, nameField, passwordField } from "./auth.schema";
import { phoneField } from "./signup.schema";

const ACCESS_LEVELS = [
	"SUPER_ADMIN",
	"BUSINESS_ADMIN",
] as const satisfies readonly AccessLevel[];

export const accessLevelField = z.enum(ACCESS_LEVELS, {
	error: "Selecione um nível de acesso",
});

export const newPasswordField = z
	.string()
	.refine((v) => v === "" || passwordField.safeParse(v).success, {
		error:
			"Senha deve ter no mínimo 8 caracteres, com letra maiúscula, minúscula e número",
	});

export const createAdministratorSchema = z.object({
	name: nameField,
	email: emailField,
	password: passwordField,
	phone: phoneField,
	accessLevel: accessLevelField,
});

export type CreateAdministratorFormValues = z.infer<
	typeof createAdministratorSchema
>;

export const updateAdministratorSchema = z.object({
	name: nameField,
	phone: phoneField,
	accessLevel: accessLevelField,
	newPassword: newPasswordField,
});

export type UpdateAdministratorFormValues = z.infer<
	typeof updateAdministratorSchema
>;
