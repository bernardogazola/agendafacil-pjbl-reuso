export type BusinessCategory =
	| "BARBER_SHOP"
	| "SALON"
	| "CLINIC"
	| "AESTHETICS"
	| "PERSONAL_TRAINER"
	| "PSYCHOLOGIST"
	| "OTHER";

export type BusinessPlan = "BASIC" | "PROFESSIONAL" | "PREMIUM";

export type AppointmentStatus =
	| "SCHEDULED"
	| "CONFIRMED"
	| "CANCELED"
	| "COMPLETED"
	| "NO_SHOW";

export type NotificationChannel = "EMAIL" | "SMS" | "WHATSAPP";

export type PricingPolicyType = "FIXED" | "DURATION_BASED" | "DISCOUNTED";

export type CancellationPolicyType = "FREE" | "DEADLINE" | "FEE_BASED";

export type ReportPeriod = "DAILY" | "WEEKLY" | "MONTHLY";

export type DayOfWeek =
	| "MONDAY"
	| "TUESDAY"
	| "WEDNESDAY"
	| "THURSDAY"
	| "FRIDAY"
	| "SATURDAY"
	| "SUNDAY";

export type UserRole = "customer" | "owner";

// Auth

export interface SignupCustomerRequest {
	name: string;
	email: string;
	password: string;
	birthDate?: string | null;
	phone?: string | null;
}

export interface SignupOwnerRequest {
	ownerName: string;
	ownerEmail: string;
	ownerPassword: string;
	businessTradeName: string;
	businessEmail: string;
	category: BusinessCategory;
	plan: BusinessPlan;
}

export interface LoginRequest {
	email: string;
	password: string;
}

export interface LoginResponse {
	token: string;
	role: UserRole;
	userId: number;
	email: string;
	name: string;
	businessId?: number | null;
}

// Business/Service

export interface BusinessResponse {
	id: number;
	tradeName: string;
	email: string;
	phone: string | null;
	category: BusinessCategory;
	plan: BusinessPlan;
	cancellationPolicyType: CancellationPolicyType;
	active: boolean;
}

export interface CreateOfferedServiceRequest {
	name: string;
	basePrice: number;
	durationMinutes: number;
	description?: string | null;
	pricingPolicyType?: PricingPolicyType | null;
}

export interface OfferedServiceResponse {
	id: number;
	businessId: number;
	name: string;
	basePrice: number;
	durationMinutes: number;
	description: string | null;
	pricingPolicyType: PricingPolicyType;
	active: boolean;
}

export interface BusinessHoursDTO {
	dayOfWeek: DayOfWeek;
	startTime: string;
	endTime: string;
	active: boolean;
}

export interface UpdateBusinessHoursRequest {
	hours: BusinessHoursDTO[];
}

// Appointment/Schedule

export interface AvailableSlotsResponse {
	businessId: number;
	serviceId: number;
	date: string;
	slots: string[];
}

export interface BookAppointmentRequest {
	businessId: number;
	serviceId: number;
	scheduledAt: string;
	notes?: string | null;
}

export interface AppointmentResponse {
	id: number;
	businessId: number;
	businessName: string;
	serviceId: number;
	serviceName: string;
	customerId: number;
	customerName: string;
	scheduledAt: string;
	estimatedDurationMinutes: number;
	pricePaid: number;
	status: AppointmentStatus;
	notes: string | null;
}

export interface CancellationResponse {
	success: boolean;
	status: AppointmentStatus;
	fee: number;
	reason: string;
}

// Reports

export interface ReportResponse {
	period: ReportPeriod;
	startDate: string;
	endDate: string;
	appointmentCount: number;
	totalRevenue: number;
	formatted: string;
}

// Erros

export interface ApiErrorBody {
	error: string;
	code: string;
	timestamp: string;
}
