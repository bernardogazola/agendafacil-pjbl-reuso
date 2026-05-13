import type { AnyFieldApi } from "@tanstack/react-form";
import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import { ChevronDownIcon, LoaderCircle } from "lucide-react";
import {
	type ChangeEvent,
	type ComponentProps,
	type KeyboardEvent,
	useMemo,
	useRef,
	useState,
} from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Checkbox } from "@/components/ui/checkbox";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "@/components/ui/popover";
import {
	formatIsoDateLocal,
	MAX_BIRTHDATE_AGE_YEARS,
	parseIsoDateLocal,
} from "@/lib/date";
import { NOTIFICATION_CHANNEL_LABELS } from "@/lib/labels";
import {
	BRAZIL_PHONE_MAX_DIGITS,
	BRAZIL_PHONE_MAX_DISPLAY_LENGTH,
	extractDigits,
	formatBrazilianPhone,
} from "@/lib/phone";
import { useUpdateCustomer } from "@/lib/queries/admin/customers";
import type {
	CustomerResponse,
	NotificationChannel,
	UpdateCustomerRequest,
} from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type UpdateCustomerFormValues,
	updateCustomerSchema,
} from "@/lib/validator/admin-customer.schema";

const ALL_CHANNELS = ["EMAIL", "SMS", "WHATSAPP"] as const satisfies readonly [
	NotificationChannel,
	NotificationChannel,
	NotificationChannel,
];

interface CustomerEditFormProps extends ComponentProps<"form"> {
	customer: CustomerResponse;
	/**
	 * Disparado após a atualização ser confirmada pelo backend. Quando informado,
	 * substitui o comportamento padrão de navegar para `/admin/customers`. Útil
	 * para fechar um `Sheet`/`Dialog` no componente pai.
	 */
	onSaved?: () => void;
	/**
	 * Disparado ao clicar em "Cancelar". Quando informado, substitui o
	 * comportamento padrão de navegar para `/admin/customers`.
	 */
	onCancel?: () => void;
}

export function CustomerEditForm({
	customer,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<CustomerEditFormProps>) {
	const navigate = useNavigate();
	const update = useUpdateCustomer(customer.id);
	const phoneInputRef = useRef<HTMLInputElement>(null);

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/admin/customers" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/admin/customers" });
	};

	const form = useForm({
		defaultValues: {
			name: customer.name,
			phone: customer.phone ?? "",
			birthDate: customer.birthDate ?? "",
			notificationPreferences: customer.notificationPreferences ?? [],
		} satisfies UpdateCustomerFormValues,
		validators: {
			onMount: updateCustomerSchema,
			onChange: updateCustomerSchema,
			onSubmit: updateCustomerSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: UpdateCustomerRequest = {
					name: value.name,
					phone: value.phone === "" ? null : value.phone,
					birthDate: value.birthDate === "" ? null : value.birthDate,
					notificationPreferences: value.notificationPreferences,
				};
				const result = await update.mutateAsync(payload);
				toast.success(`Cliente ${result.name} atualizado.`);
				handleSaved();
			} catch (err) {
				toast.error(
					err instanceof Error && err.message
						? err.message
						: "Não foi possível salvar as alterações. Tente novamente.",
				);
			}
		},
	});

	return (
		<form
			id="customer-edit-form"
			className={cn("space-y-5", className)}
			noValidate
			onSubmit={(e) => {
				e.preventDefault();
				form.handleSubmit();
			}}
			{...props}
		>
			<FieldGroup>
				<Field className="space-y-3">
					<FieldLabel htmlFor="customer-email">E-mail</FieldLabel>
					<Input
						id="customer-email"
						name="email"
						value={customer.email}
						readOnly
						disabled
						type="email"
						autoComplete="email"
					/>
					<FieldDescription>O e-mail não pode ser alterado.</FieldDescription>
				</Field>

				<form.Field name="name">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nome</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="Nome completo do cliente"
									type="text"
									autoComplete="name"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="phone">
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

				<form.Field name="birthDate">
					{(field) => <BirthDateField field={field} />}
				</form.Field>

				<form.Field name="notificationPreferences" mode="array">
					{(field) => {
						const value = field.state.value as NotificationChannel[];
						const toggle = (channel: NotificationChannel, checked: boolean) => {
							const next = checked
								? Array.from(new Set([...value, channel]))
								: value.filter((c) => c !== channel);
							field.handleChange(next);
							field.handleBlur();
						};
						return (
							<FieldSet>
								<FieldLegend>Preferências de notificação</FieldLegend>
								<FieldDescription>
									Canais pelos quais o cliente quer receber lembretes e
									confirmações.
								</FieldDescription>
								<FieldGroup data-slot="checkbox-group" className="gap-3">
									{ALL_CHANNELS.map((channel) => {
										const id = `${field.name}-${channel}`;
										const checked = value.includes(channel);
										return (
											<Field
												key={channel}
												orientation="horizontal"
												className="gap-2"
											>
												<Checkbox
													id={id}
													checked={checked}
													onCheckedChange={(next) =>
														toggle(channel, Boolean(next))
													}
												/>
												<FieldLabel htmlFor={id} className="font-normal">
													{NOTIFICATION_CHANNEL_LABELS[channel]}
												</FieldLabel>
											</Field>
										);
									})}
								</FieldGroup>
							</FieldSet>
						);
					}}
				</form.Field>

				<form.Subscribe
					selector={(state) =>
						[state.isValid, state.isTouched, state.isSubmitting] as const
					}
				>
					{([isValid, isTouched, isSubmitting]) => {
						const disableSubmit = !isValid || !isTouched || isSubmitting;
						return (
							<div className="flex items-center justify-end gap-3 pt-2">
								<Button
									type="button"
									variant="ghost"
									disabled={isSubmitting}
									onClick={handleCancel}
									className="cursor-pointer"
								>
									Cancelar
								</Button>
								<Button
									type="submit"
									disabled={disableSubmit}
									aria-disabled={disableSubmit}
									className={cn(
										disableSubmit ? "cursor-not-allowed" : "cursor-pointer",
									)}
								>
									{isSubmitting && (
										<LoaderCircle className="mr-2 size-4 animate-spin" />
									)}
									Salvar
								</Button>
							</div>
						);
					}}
				</form.Subscribe>
			</FieldGroup>
		</form>
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
								: "Selecione a data de nascimento"}
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
