import { useForm } from "@tanstack/react-form";
import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import { CalendarIcon, ChevronDownIcon, LoaderCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { PageHeader } from "@/components/dashboard/page-header";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "@/components/ui/popover";
import { Textarea } from "@/components/ui/textarea";
import { ApiError } from "@/lib/api";
import { formatIsoDateLocal, parseIsoDateLocal } from "@/lib/date";
import {
	combineDateTime,
	formatCurrency,
	formatDate,
	toIsoDate,
} from "@/lib/formatters";
import { PRICING_POLICY_LABELS } from "@/lib/labels";
import { useBookAppointment } from "@/lib/queries/appointments";
import { availableSlotsOptions } from "@/lib/queries/availability";
import {
	businessesListOptions,
	businessServicesOptions,
} from "@/lib/queries/businesses";
import type { BookAppointmentRequest } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type BookAppointmentFormValues,
	bookAppointmentSchema,
} from "@/lib/validator/customer-booking.schema";

export const Route = createFileRoute("/customer/book/$businessId/$serviceId")({
	staticData: { crumb: "Agendar" },
	parseParams: ({ businessId, serviceId }) => ({
		businessId: Number(businessId),
		serviceId: Number(serviceId),
	}),
	loader: ({ context, params }) =>
		Promise.all([
			context.queryClient.ensureQueryData(
				businessServicesOptions(params.businessId),
			),
			context.queryClient.ensureQueryData(businessesListOptions()),
		]),
	component: BookingPage,
});

function shiftPastSunday(d: Date): Date {
	const copy = new Date(d);
	if (copy.getDay() === 0) copy.setDate(copy.getDate() + 1);
	return copy;
}

function BookingPage() {
	const { businessId, serviceId } = Route.useParams();
	const navigate = useNavigate();
	const book = useBookAppointment();

	const servicesQ = useQuery(businessServicesOptions(businessId));
	const businessesQ = useQuery(businessesListOptions());

	const initialDate = useMemo(() => {
		const tomorrow = new Date();
		tomorrow.setDate(tomorrow.getDate() + 1);
		return toIsoDate(shiftPastSunday(tomorrow));
	}, []);

	const form = useForm({
		defaultValues: {
			date: initialDate,
			timeSlot: "",
			notes: "",
		} satisfies BookAppointmentFormValues,
		validators: {
			onMount: bookAppointmentSchema,
			onChange: bookAppointmentSchema,
			onSubmit: bookAppointmentSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: BookAppointmentRequest = {
					businessId,
					serviceId,
					scheduledAt: combineDateTime(value.date, value.timeSlot),
					notes: value.notes === "" ? null : value.notes,
				};
				await book.mutateAsync(payload);
				toast.success("Agendamento criado com sucesso.");
				void navigate({ to: "/customer/me" });
			} catch (err) {
				toast.error(
					err instanceof ApiError
						? err.body.error
						: err instanceof Error && err.message
							? err.message
							: "Não foi possível agendar. Tente novamente.",
				);
			}
		},
	});

	const service = servicesQ.data?.find((s) => s.id === serviceId);
	const biz = businessesQ.data?.find((b) => b.id === businessId);

	if (servicesQ.isLoading || businessesQ.isLoading) {
		return (
			<div className="flex items-center justify-center py-12">
				<LoaderCircle
					aria-label="Carregando dados do estabelecimento"
					className="size-6 animate-spin text-muted-foreground"
				/>
			</div>
		);
	}

	if (!service || !biz) {
		return (
			<>
				<PageHeader title="Agendar" />
				<p className="rounded-lg border border-dashed p-6 text-center text-sm text-muted-foreground">
					Serviço ou estabelecimento não encontrado.
				</p>
			</>
		);
	}

	return (
		<>
			<PageHeader
				title={service.name}
				description={
					<>
						<span className="block">
							{biz.tradeName} · {service.durationMinutes} min ·{" "}
							{PRICING_POLICY_LABELS[service.pricingPolicyType]} ·{" "}
							{formatCurrency(service.basePrice)}
						</span>
						{service.description ? (
							<span className="mt-1 block">{service.description}</span>
						) : null}
					</>
				}
			/>

			<form
				id="appointment-booking-form"
				className="space-y-5"
				noValidate
				onSubmit={(e) => {
					e.preventDefault();
					form.handleSubmit();
				}}
			>
				<FieldGroup>
					<form.Field name="date">
						{(field) => (
							<DateField
								field={field}
								onChange={() => form.setFieldValue("timeSlot", "")}
							/>
						)}
					</form.Field>

					<form.Subscribe selector={(s) => s.values.date}>
						{(date) => (
							<form.Field name="timeSlot">
								{(field) => (
									<SlotField
										businessId={businessId}
										serviceId={serviceId}
										date={date}
										field={field}
									/>
								)}
							</form.Field>
						)}
					</form.Subscribe>

					<form.Field name="notes">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor={field.name}>
										Observações{" "}
										<span className="text-muted-foreground font-normal">
											(opcional)
										</span>
									</FieldLabel>
									<Textarea
										id={field.name}
										name={field.name}
										value={field.state.value}
										onBlur={field.handleBlur}
										onChange={(e) => field.handleChange(e.target.value)}
										aria-invalid={isInvalid}
										rows={3}
										maxLength={500}
										placeholder="Ex: preferência por tesoura"
									/>
									<FieldDescription>Até 500 caracteres.</FieldDescription>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>

					<form.Subscribe
						selector={(state) =>
							[
								state.isValid,
								state.isTouched,
								state.isSubmitting,
								state.values.date,
								state.values.timeSlot,
							] as const
						}
					>
						{([isValid, isTouched, isSubmitting, date, timeSlot]) => {
							const disableSubmit = !isValid || !isTouched || isSubmitting;
							return (
								<div className="flex flex-col items-end gap-3 pt-2 sm:flex-row sm:items-center sm:justify-between">
									<p className="text-sm text-muted-foreground">
										{timeSlot ? (
											<>
												Confirmar em{" "}
												<strong className="text-foreground">
													{formatDate(date)} às {timeSlot.slice(0, 5)}
												</strong>
											</>
										) : (
											"Selecione um horário acima."
										)}
									</p>
									<div className="flex items-center gap-3">
										<Button
											type="button"
											variant="ghost"
											disabled={isSubmitting}
											onClick={() => void navigate({ to: "/customer/me" })}
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
											Confirmar agendamento
										</Button>
									</div>
								</div>
							);
						}}
					</form.Subscribe>
				</FieldGroup>
			</form>
		</>
	);
}

interface DateFieldProps {
	field: any;
	onChange: () => void;
}

function DateField({ field, onChange }: Readonly<DateFieldProps>) {
	const [open, setOpen] = useState(false);
	const value = field.state.value as string;
	const isInvalid = field.state.meta.isTouched && !field.state.meta.isValid;

	const selected = useMemo(() => parseIsoDateLocal(value), [value]);

	const today = useMemo(() => {
		const t = new Date();
		t.setHours(0, 0, 0, 0);
		return t;
	}, []);

	const handleSelect = (date: Date | undefined) => {
		if (!date) return;
		field.handleChange(formatIsoDateLocal(date));
		onChange();
		setOpen(false);
		field.handleBlur();
	};

	return (
		<Field data-invalid={isInvalid} className="space-y-3">
			<FieldLabel htmlFor={field.name}>Data</FieldLabel>
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
							<span className="inline-flex items-center gap-2">
								<CalendarIcon className="size-4" />
								{selected
									? format(selected, "PPP", { locale: ptBR })
									: "Selecione a data"}
							</span>
							<ChevronDownIcon className="size-4" />
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
						defaultMonth={selected ?? today}
						disabled={{ before: today }}
						fixedWeeks
						autoFocus
					/>
				</PopoverContent>
			</Popover>
			{isInvalid && <FieldError errors={field.state.meta.errors} />}
		</Field>
	);
}

interface SlotFieldProps {
	businessId: number;
	serviceId: number;
	date: string;
	field: any;
}

function SlotField({
	businessId,
	serviceId,
	date,
	field,
}: Readonly<SlotFieldProps>) {
	const slotsQ = useQuery(availableSlotsOptions(businessId, serviceId, date));
	const slots = slotsQ.data?.slots ?? [];
	const isInvalid = field.state.meta.isTouched && !field.state.meta.isValid;

	useEffect(() => {
		const current = field.state.value as string;
		if (current && slots.length > 0 && !slots.includes(current)) {
			field.handleChange("");
		}
	}, [field, slots]);

	return (
		<Field data-invalid={isInvalid} className="space-y-3">
			<FieldLabel htmlFor={field.name}>
				Horários disponíveis em {formatDate(date)}
			</FieldLabel>
			{slotsQ.isLoading ? (
				<div className="flex items-center justify-center py-6">
					<LoaderCircle
						aria-label="Buscando horários"
						className="size-5 animate-spin text-muted-foreground"
					/>
				</div>
			) : slots.length === 0 ? (
				<p className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
					Sem horários disponíveis nesta data — tente outro dia.
				</p>
			) : (
				<div id={field.name} className="grid grid-cols-3 gap-2 sm:grid-cols-6">
					{slots.map((slot) => {
						const active = field.state.value === slot;
						return (
							<button
								key={slot}
								type="button"
								aria-pressed={active}
								aria-label={`Horário ${slot.slice(0, 5)}`}
								onBlur={field.handleBlur}
								onClick={() => field.handleChange(slot)}
								className={cn(
									"rounded-md border px-2 py-2 text-sm font-medium transition-colors cursor-pointer",
									active
										? "border-primary bg-primary text-primary-foreground"
										: "border-input bg-background hover:border-primary",
								)}
							>
								{slot.slice(0, 5)}
							</button>
						);
					})}
				</div>
			)}
			{isInvalid && <FieldError errors={field.state.meta.errors} />}
		</Field>
	);
}
