import type { ReactFormExtendedApi } from "@tanstack/react-form";
import type { BusinessCategory, BusinessPlan, UserRole } from "@/lib/types";

export interface SignupFormValues {
	role: UserRole | "";
	name: string;
	email: string;
	password: string;
	confirmPassword: string;
	phone: string;
	birthDate: string;
	businessTradeName: string;
	businessEmail: string;
	category: BusinessCategory | "";
	plan: BusinessPlan | "";
}

export const signupDefaultValues: SignupFormValues = {
	role: "",
	name: "",
	email: "",
	password: "",
	confirmPassword: "",
	phone: "",
	birthDate: "",
	businessTradeName: "",
	businessEmail: "",
	category: "",
	plan: "",
};

export type StepId =
	| "accountType"
	| "credentials"
	| "customerFinalize"
	| "business"
	| "plan";

export type StepFieldName = keyof SignupFormValues;

export const STEP_FIELDS: Record<StepId, readonly StepFieldName[]> = {
	accountType: ["role"],
	credentials: ["name", "email", "password", "confirmPassword"],
	customerFinalize: ["phone", "birthDate"],
	business: ["businessTradeName", "businessEmail", "category"],
	plan: ["plan"],
};

export const STEP_TITLES: Record<StepId, string> = {
	accountType: "Tipo de conta",
	credentials: "Suas credenciais",
	customerFinalize: "Quase lá",
	business: "Sobre seu negócio",
	plan: "Escolha seu plano",
};

export const CUSTOMER_STEPS: readonly StepId[] = [
	"accountType",
	"credentials",
	"customerFinalize",
];

export const OWNER_STEPS: readonly StepId[] = [
	"accountType",
	"credentials",
	"business",
	"plan",
];

export type SignupFormApi = ReactFormExtendedApi<
	SignupFormValues,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	undefined,
	unknown
>;
