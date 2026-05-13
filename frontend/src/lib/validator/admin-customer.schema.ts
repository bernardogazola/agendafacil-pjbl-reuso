import { z } from "zod";
import type { NotificationChannel } from "@/lib/types";
import { nameField } from "./auth.schema";
import { birthDateField, phoneField } from "./signup.schema";

const NOTIFICATION_CHANNELS = [
	"EMAIL",
	"SMS",
	"WHATSAPP",
] as const satisfies readonly NotificationChannel[];

export const notificationChannelField = z.enum(NOTIFICATION_CHANNELS, {
	error: "Canal de notificação inválido",
});

export const updateCustomerSchema = z.object({
	name: nameField,
	phone: phoneField,
	birthDate: birthDateField,
	notificationPreferences: z.array(notificationChannelField),
});

export type UpdateCustomerFormValues = z.infer<typeof updateCustomerSchema>;
