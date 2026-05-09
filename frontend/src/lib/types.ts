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

export type UserRole = "customer" | "owner" | "admin";

export type AccessLevel = "SUPER_ADMIN" | "BUSINESS_ADMIN";

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

export interface BusinessHoursResponse {
	id: number;
	dayOfWeek: DayOfWeek;
	startTime: string;
	endTime: string;
	active: boolean;
}

export interface CreateBusinessHoursRequest {
	dayOfWeek: DayOfWeek;
	startTime: string;
	endTime: string;
	active?: boolean | null;
}

export interface UpdateBusinessHoursEntryRequest {
	startTime: string;
	endTime: string;
	active: boolean;
}

export interface UpdateBusinessHoursRequest {
	hours: BusinessHoursDTO[];
}

export type AppointmentStatusAction = "CONFIRM" | "COMPLETE" | "MARK_NO_SHOW";

export interface UpdateAppointmentStatusRequest {
	action: AppointmentStatusAction;
}

export interface RescheduleAppointmentRequest {
	newScheduledAt: string;
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

// Admin: Business

export interface UpdateBusinessRequest {
	tradeName: string;
	email: string;
	phone?: string | null;
	category: BusinessCategory;
	plan: BusinessPlan;
	cancellationPolicyType: CancellationPolicyType;
}

export interface UpdateOfferedServiceRequest {
	name: string;
	basePrice: number;
	durationMinutes: number;
	description?: string | null;
	pricingPolicyType: PricingPolicyType;
}

// Admin: Customer

export interface CustomerResponse {
	id: number;
	name: string;
	email: string;
	phone: string | null;
	birthDate: string | null;
	notificationPreferences: NotificationChannel[];
	active: boolean;
}

export interface UpdateCustomerRequest {
	name: string;
	phone?: string | null;
	birthDate?: string | null;
	notificationPreferences?: NotificationChannel[] | null;
}

// Admin: Administrator

export interface AdministratorResponse {
	id: number;
	name: string;
	email: string;
	phone: string | null;
	accessLevel: AccessLevel;
	active: boolean;
}

export interface CreateAdministratorRequest {
	name: string;
	email: string;
	password: string;
	phone?: string | null;
	accessLevel: AccessLevel;
}

export interface UpdateAdministratorRequest {
	name: string;
	phone?: string | null;
	accessLevel: AccessLevel;
	newPassword?: string | null;
}

// Promotions

export interface PromotionResponse {
	id: number;
	businessId: number;
	name: string;
	description: string | null;
	discountPercentage: number | null;
	discountAmount: number | null;
	validFrom: string;
	validTo: string;
	eligibleServiceIds: number[];
	active: boolean;
}

export interface CreatePromotionRequest {
	name: string;
	description?: string | null;
	discountPercentage?: number | null;
	discountAmount?: number | null;
	validFrom: string;
	validTo: string;
	eligibleServiceIds?: number[] | null;
}

export interface UpdatePromotionRequest {
	name: string;
	description?: string | null;
	discountPercentage?: number | null;
	discountAmount?: number | null;
	validFrom: string;
	validTo: string;
	eligibleServiceIds?: number[] | null;
	active: boolean;
}

// Reviews

export interface ReviewResponse {
	id: number;
	appointmentId: number;
	customerId: number;
	customerName: string;
	businessId: number;
	businessName: string;
	serviceId: number;
	serviceName: string;
	rating: number;
	comment: string | null;
}

export interface CreateReviewRequest {
	rating: number;
	comment?: string | null;
}

export interface UpdateReviewRequest {
	rating: number;
	comment?: string | null;
}

// Erros

export interface ApiErrorBody {
	error: string;
	code: string;
	timestamp: string;
}
