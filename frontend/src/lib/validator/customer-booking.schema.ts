import { z } from "zod";

export const bookingDateField = z
	.string()
	.regex(/^\d{4}-\d{2}-\d{2}$/, { error: "Selecione uma data válida" });

export const bookingTimeSlotField = z.string().regex(/^\d{2}:\d{2}(:\d{2})?$/, {
	error: "Selecione um horário disponível",
});

export const bookingNotesField = z
	.string()
	.max(500, { error: "Observações devem ter no máximo 500 caracteres" });

export const bookAppointmentSchema = z.object({
	date: bookingDateField,
	timeSlot: bookingTimeSlotField,
	notes: bookingNotesField,
});
export type BookAppointmentFormValues = z.infer<typeof bookAppointmentSchema>;
