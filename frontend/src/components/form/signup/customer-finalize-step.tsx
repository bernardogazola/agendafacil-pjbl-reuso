import type { AnyFieldApi } from "@tanstack/react-form";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import { ChevronDownIcon } from "lucide-react";
import {
	type ChangeEvent,
	type KeyboardEvent,
	useMemo,
	useRef,
	useState,
} from "react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldTitle,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "@/components/ui/popover";
import type { SignupFormApi } from "@/lib/config/signup-form.config";
import {
	formatIsoDateLocal,
	MAX_BIRTHDATE_AGE_YEARS,
	parseIsoDateLocal,
} from "@/lib/date";
import {
	BRAZIL_PHONE_MAX_DIGITS,
	BRAZIL_PHONE_MAX_DISPLAY_LENGTH,
	extractDigits,
	formatBrazilianPhone,
} from "@/lib/phone";
import { birthDateField, phoneField } from "@/lib/validator/signup.schema";

interface CustomerFinalizeStepProps {
	form: SignupFormApi;
}

export function CustomerFinalizeStep({
	form,
}: Readonly<CustomerFinalizeStepProps>) {
	const phoneInputRef = useRef<HTMLInputElement>(null);

	return (
		<div className="space-y-4">
			<div className="space-y-1">
				<FieldTitle className="text-base">Quase lá</FieldTitle>
				<FieldDescription>
					Estes dados são opcionais e ajudam negócios a atender você melhor.
					Você pode adicioná-los depois a qualquer momento.
				</FieldDescription>
			</div>

			<FieldGroup>
				<form.Field name="phone" validators={{ onChange: phoneField }}>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						const displayValue = formatBrazilianPhone(field.state.value);

						const caretAfterNDigits = (formatted: string, n: number) => {
							let pos = 0;
							let seen = 0;
							while (pos < formatted.length && seen < n) {
								if (/\d/.test(formatted[pos])) seen++;
								pos++;
							}
							return pos;
						};

						const applyDigits = (nextDigits: string, caret: number) => {
							field.handleChange(nextDigits);
							requestAnimationFrame(() => {
								phoneInputRef.current?.setSelectionRange(caret, caret);
							});
						};

						const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
							const raw = e.target.value;
							const caret = e.target.selectionStart ?? raw.length;
							const digitsBefore = extractDigits(raw.slice(0, caret)).length;
							const nextDigits = extractDigits(raw).slice(
								0,
								BRAZIL_PHONE_MAX_DIGITS,
							);
							const nextFormatted = formatBrazilianPhone(nextDigits);
							const newCaret = caretAfterNDigits(
								nextFormatted,
								Math.min(digitsBefore, nextDigits.length),
							);
							applyDigits(nextDigits, newCaret);
						};

						const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
							const el = e.currentTarget;
							const { selectionStart, selectionEnd, value } = el;
							if (selectionStart === null || selectionEnd === null) return;
							if (selectionStart !== selectionEnd) return;

							if (e.key === "Backspace" && selectionStart > 0) {
								if (!/\d/.test(value[selectionStart - 1])) {
									e.preventDefault();
									let deletePos = selectionStart - 1;
									while (deletePos > 0 && !/\d/.test(value[deletePos - 1]))
										deletePos--;
									if (deletePos === 0) return;
									const patched =
										value.slice(0, deletePos - 1) + value.slice(deletePos);
									const digitsBefore = extractDigits(
										patched.slice(0, deletePos - 1),
									).length;
									const nextDigits = extractDigits(patched).slice(
										0,
										BRAZIL_PHONE_MAX_DIGITS,
									);
									applyDigits(
										nextDigits,
										caretAfterNDigits(
											formatBrazilianPhone(nextDigits),
											digitsBefore,
										),
									);
								}
							} else if (e.key === "Delete" && selectionStart < value.length) {
								if (!/\d/.test(value[selectionStart])) {
									e.preventDefault();
									let deletePos = selectionStart;
									while (
										deletePos < value.length &&
										!/\d/.test(value[deletePos])
									)
										deletePos++;
									if (deletePos >= value.length) return;
									const patched =
										value.slice(0, deletePos) + value.slice(deletePos + 1);
									const digitsBefore = extractDigits(
										patched.slice(0, deletePos),
									).length;
									const nextDigits = extractDigits(patched).slice(
										0,
										BRAZIL_PHONE_MAX_DIGITS,
									);
									applyDigits(
										nextDigits,
										caretAfterNDigits(
											formatBrazilianPhone(nextDigits),
											digitsBefore,
										),
									);
								}
							}
						};

						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>
									Telefone{" "}
									<span className="text-muted-foreground font-normal">
										(opcional)
									</span>
								</FieldLabel>
								<Input
									ref={phoneInputRef}
									id={field.name}
									name={field.name}
									value={displayValue}
									onBlur={field.handleBlur}
									onChange={handleChange}
									onKeyDown={handleKeyDown}
									aria-invalid={isInvalid}
									placeholder="(11) 99999-9999"
									type="tel"
									inputMode="numeric"
									autoComplete="tel"
									maxLength={BRAZIL_PHONE_MAX_DISPLAY_LENGTH}
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="birthDate" validators={{ onChange: birthDateField }}>
					{(field) => <BirthDateField field={field} />}
				</form.Field>
			</FieldGroup>
		</div>
	);
}

function BirthDateField({ field }: Readonly<{ field: AnyFieldApi }>) {
	const [open, setOpen] = useState(false);
	const value = field.state.value as string;
	const isInvalid = field.state.meta.isTouched && !field.state.meta.isValid;

	const selected = useMemo(() => parseIsoDateLocal(value), [value]);

	const { earliest, today, fallbackMonth } = useMemo(() => {
		const todayDate = new Date();
		todayDate.setHours(0, 0, 0, 0);
		const earliestDate = new Date();
		earliestDate.setFullYear(
			earliestDate.getFullYear() - MAX_BIRTHDATE_AGE_YEARS,
		);
		earliestDate.setHours(0, 0, 0, 0);
		const fallback = new Date();
		fallback.setFullYear(fallback.getFullYear() - 20);
		return {
			earliest: earliestDate,
			today: todayDate,
			fallbackMonth: fallback,
		};
	}, []);

	const handleSelect = (date: Date | undefined) => {
		field.handleChange(date ? formatIsoDateLocal(date) : "");
		setOpen(false);
		field.handleBlur();
	};

	return (
		<Field data-invalid={isInvalid} className="space-y-3">
			<FieldLabel htmlFor={field.name}>
				Data de nascimento{" "}
				<span className="text-muted-foreground font-normal">(opcional)</span>
			</FieldLabel>
			<Popover open={open} onOpenChange={setOpen}>
				<PopoverTrigger
					render={
						<Button
							id={field.name}
							type="button"
							variant="outline"
							data-empty={!value}
							aria-invalid={isInvalid}
							onBlur={field.handleBlur}
							className="w-full justify-between font-normal cursor-pointer data-[empty=true]:text-muted-foreground"
						>
							{selected
								? format(selected, "PPP", { locale: ptBR })
								: "Selecione sua data de nascimento"}
							<ChevronDownIcon data-icon="inline-end" />
						</Button>
					}
				/>
				<PopoverContent
					align="start"
					className="w-auto p-0"
					collisionAvoidance={{ side: "none" }}
				>
					<Calendar
						mode="single"
						locale={ptBR}
						selected={selected}
						onSelect={handleSelect}
						defaultMonth={selected ?? fallbackMonth}
						captionLayout="dropdown"
						startMonth={earliest}
						endMonth={today}
						disabled={{ after: today, before: earliest }}
						fixedWeeks
						autoFocus
					/>
				</PopoverContent>
			</Popover>
			{isInvalid && <FieldError errors={field.state.meta.errors} />}
		</Field>
	);
}
