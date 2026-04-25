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

export const PLAN_HINTS: Record<BusinessPlan, string> = {
	BASIC: "Para começar e validar seu negócio",
	PROFESSIONAL: "Para negócios em crescimento",
	PREMIUM: "Para operações completas e em escala",
};

export const PLAN_PRICES: Record<
	BusinessPlan,
	{ amount: string; period: string }
> = {
	BASIC: { amount: "R$ 0", period: "/mês" },
	PROFESSIONAL: { amount: "R$ 49", period: "/mês" },
	PREMIUM: { amount: "R$ 99", period: "/mês" },
};

export const PLAN_FEATURES: Record<BusinessPlan, string[]> = {
	BASIC: [
		"Até 50 agendamentos por mês",
		"1 profissional",
		"Suporte por e-mail",
	],
	PROFESSIONAL: [
		"Agendamentos ilimitados",
		"Até 5 profissionais",
		"Relatórios mensais",
		"Suporte prioritário",
	],
	PREMIUM: [
		"Agendamentos ilimitados",
		"Profissionais ilimitados",
		"Relatórios avançados",
		"Integrações premium",
		"Suporte dedicado",
	],
};

export const BUSINESS_CATEGORY_HINTS: Record<BusinessCategory, string> = {
	BARBER_SHOP: "Cortes, barba e cuidados masculinos",
	SALON: "Cabelo, coloração e tratamentos",
	CLINIC: "Consultas e procedimentos clínicos",
	AESTHETICS: "Estética facial e corporal",
	PERSONAL_TRAINER: "Treinos e acompanhamento físico",
	PSYCHOLOGIST: "Sessões de terapia e acompanhamento",
	OTHER: "Outro tipo de serviço com agenda",
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
