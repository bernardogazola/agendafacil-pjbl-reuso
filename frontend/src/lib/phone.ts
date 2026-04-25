export const BRAZIL_PHONE_MAX_DIGITS = 11;
export const BRAZIL_PHONE_MAX_DISPLAY_LENGTH = 15;

export function extractDigits(value: string): string {
	return value.replaceAll(/\D/g, "");
}

export function formatBrazilianPhone(digits: string): string {
	const d = digits.slice(0, BRAZIL_PHONE_MAX_DIGITS);
	if (d.length === 0) return "";
	if (d.length <= 2) return `(${d}`;
	if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
	if (d.length <= 10)
		return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
	return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}
