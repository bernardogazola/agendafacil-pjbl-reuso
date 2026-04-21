import type {
	AppointmentStatus,
	BusinessCategory,
	BusinessPlan,
	CancellationPolicyType,
	DayOfWeek,
	PricingPolicyType,
	ReportPeriod,
} from "./types";

export const BUSINESS_CATEGORY_LABELS: Record<BusinessCategory, string> = {
	BARBER_SHOP: "Barbearia",
	SALON: "Salão de Beleza",
	CLINIC: "Clínica",
	AESTHETICS: "Estética",
	PERSONAL_TRAINER: "Personal Trainer",
	PSYCHOLOGIST: "Psicólogo(a)",
	OTHER: "Outro",
};

export const PLAN_LABELS: Record<BusinessPlan, string> = {
	BASIC: "Básico",
	PROFESSIONAL: "Profissional",
	PREMIUM: "Premium",
};

export const STATUS_LABELS: Record<AppointmentStatus, string> = {
	SCHEDULED: "Agendado",
	CONFIRMED: "Confirmado",
	CANCELED: "Cancelado",
	COMPLETED: "Concluído",
	NO_SHOW: "Não compareceu",
};

export const PRICING_POLICY_LABELS: Record<PricingPolicyType, string> = {
	FIXED: "Preço fixo",
	DURATION_BASED: "Por duração",
	DISCOUNTED: "Com desconto",
};

export const PRICING_POLICY_HINTS: Record<PricingPolicyType, string> = {
	FIXED: "Cobra o preço base do serviço sem ajustes.",
	DURATION_BASED: "Cobra por minuto, com base no preço e duração configurados.",
	DISCOUNTED: "Aplica 10% de desconto sobre o preço fixo.",
};

export const CANCELLATION_POLICY_LABELS: Record<
	CancellationPolicyType,
	string
> = {
	FREE: "Cancelamento livre",
	DEADLINE: "Prazo mínimo",
	FEE_BASED: "Com taxa",
};

export const PERIOD_LABELS: Record<ReportPeriod, string> = {
	DAILY: "Diário",
	WEEKLY: "Semanal",
	MONTHLY: "Mensal",
};

export const DAY_LABELS: Record<DayOfWeek, string> = {
	MONDAY: "Segunda-feira",
	TUESDAY: "Terça-feira",
	WEDNESDAY: "Quarta-feira",
	THURSDAY: "Quinta-feira",
	FRIDAY: "Sexta-feira",
	SATURDAY: "Sábado",
	SUNDAY: "Domingo",
};

export const DAY_ORDER: DayOfWeek[] = [
	"MONDAY",
	"TUESDAY",
	"WEDNESDAY",
	"THURSDAY",
	"FRIDAY",
	"SATURDAY",
	"SUNDAY",
];
