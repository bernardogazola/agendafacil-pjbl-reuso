import { z } from "zod";
import { MAX_BIRTHDATE_AGE_YEARS, parseIsoDateLocal } from "@/lib/date";
import type { BusinessCategory, BusinessPlan, UserRole } from "@/lib/types";
import {
	confirmPasswordField,
	emailField,
	nameField,
	passwordField,
} from "./auth.schema";

export const ROLE_OPTIONS = [
	"customer",
	"owner",
] as const satisfies readonly UserRole[];

export const CATEGORY_OPTIONS = [
	"BARBER_SHOP",
	"SALON",
	"CLINIC",
	"AESTHETICS",
	"PERSONAL_TRAINER",
	"PSYCHOLOGIST",
	"OTHER",
] as const satisfies readonly BusinessCategory[];

export const PLAN_OPTIONS = [
	"BASIC",
	"PROFESSIONAL",
	"PREMIUM",
] as const satisfies readonly BusinessPlan[];

export const phoneField = z
	.string()
	.refine((val) => val === "" || /^\d{10,11}$/.test(val), {
		message: "Telefone deve ter 10 ou 11 dígitos",
	});

export const birthDateField = z.string().refine(
	(v) => {
		if (v === "") return true;
		const parsed = parseIsoDateLocal(v);
		if (!parsed) return false;
		const today = new Date();
		today.setHours(0, 0, 0, 0);
		if (parsed > today) return false;
		const earliest = new Date();
		earliest.setFullYear(earliest.getFullYear() - MAX_BIRTHDATE_AGE_YEARS);
		earliest.setHours(0, 0, 0, 0);
		return parsed >= earliest;
	},
	{ message: "Informe uma data válida" },
);

export const businessTradeNameField = z
	.string()
	.min(2, "Nome muito curto")
	.max(100, "Nome muito longo");

export const roleField = z.enum(ROLE_OPTIONS, {
	error: "Selecione um tipo de conta",
});

export const categoryField = z.enum(CATEGORY_OPTIONS, {
	error: "Selecione uma categoria",
});

export const planField = z.enum(PLAN_OPTIONS, {
	error: "Selecione um plano",
});

const passwordsMatch = (v: { password: string; confirmPassword: string }) =>
	v.password === v.confirmPassword;

const passwordMismatch = {
	path: ["confirmPassword"] as ["confirmPassword"],
	error: "As senhas não coincidem",
};

export const signupCustomerSchema = z
	.object({
		role: z.literal("customer"),
		name: nameField,
		email: emailField,
		password: passwordField,
		confirmPassword: confirmPasswordField,
		phone: phoneField,
		birthDate: birthDateField,
	})
	.refine(passwordsMatch, passwordMismatch);

export const signupOwnerSchema = z
	.object({
		role: z.literal("owner"),
		name: nameField,
		email: emailField,
		password: passwordField,
		confirmPassword: confirmPasswordField,
		businessTradeName: businessTradeNameField,
		businessEmail: emailField,
		category: categoryField,
		plan: planField,
	})
	.refine(passwordsMatch, passwordMismatch);
