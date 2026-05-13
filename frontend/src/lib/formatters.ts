import { formatIsoDateLocal, parseIsoDateLocal } from "@/lib/date";

const BRL = new Intl.NumberFormat("pt-BR", {
	style: "currency",
	currency: "BRL",
});

const DATE_FMT = new Intl.DateTimeFormat("pt-BR", {
	day: "2-digit",
	month: "2-digit",
	year: "numeric",
});

const DATETIME_FMT = new Intl.DateTimeFormat("pt-BR", {
	day: "2-digit",
	month: "2-digit",
	year: "numeric",
	hour: "2-digit",
	minute: "2-digit",
});

export const formatCurrency = (value: number): string => BRL.format(value);

export const toIsoDate = (input: Date | string): string => {
	if (typeof input === "string") {
		const parsed = parseIsoDateLocal(input);
		if (parsed) return formatIsoDateLocal(parsed);
		return formatIsoDateLocal(new Date(input));
	}
	return formatIsoDateLocal(input);
};

export const combineDateTime = (isoDate: string, hhmm: string): string =>
	`${isoDate}T${hhmm.length === 5 ? `${hhmm}:00` : hhmm}`;

/**
 * Formata uma data como `dd/MM/yyyy`. Aceita `Date` ou string ISO `YYYY-MM-DD`.
 */
export const formatDate = (input: Date | string): string => {
	if (typeof input === "string") {
		const parsed = parseIsoDateLocal(input);
		if (parsed) return DATE_FMT.format(parsed);
		return DATE_FMT.format(new Date(input));
	}
	return DATE_FMT.format(input);
};

/**
 * Formata uma data e hora como `dd/MM/yyyy HH:mm`. Aceita `Date` ou string ISO
 * completa (`YYYY-MM-DDTHH:mm:ss`).
 */
export const formatDateTime = (input: Date | string): string => {
	const d = typeof input === "string" ? new Date(input) : input;
	return DATETIME_FMT.format(d);
};
