import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import type { ComponentProps } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectItemDescription,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { PRICING_POLICY_HINTS, PRICING_POLICY_LABELS } from "@/lib/labels";
import { useCreateService } from "@/lib/queries/services";
import type {
	CreateOfferedServiceRequest,
	PricingPolicyType,
} from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type CreateOfferedServiceFormValues,
	createOfferedServiceSchema,
} from "@/lib/validator/owner-service.schema";

const PRICING_POLICY_KEYS = Object.keys(
	PRICING_POLICY_LABELS,
) as PricingPolicyType[];

interface ServiceCreateFormProps extends ComponentProps<"form"> {
	businessId: number;
	onSaved?: () => void;
	onCancel?: () => void;
}

export function ServiceCreateForm({
	businessId,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<ServiceCreateFormProps>) {
	const navigate = useNavigate();
	const create = useCreateService(businessId);

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/owner/services" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/owner/services" });
	};

	const form = useForm({
		defaultValues: {
			name: "",
			basePrice: 0,
			durationMinutes: 30,
			description: "",
			pricingPolicyType: "FIXED" as PricingPolicyType,
		} satisfies CreateOfferedServiceFormValues,
		validators: {
			onMount: createOfferedServiceSchema,
			onChange: createOfferedServiceSchema,
			onSubmit: createOfferedServiceSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: CreateOfferedServiceRequest = {
					name: value.name,
					basePrice: value.basePrice,
					durationMinutes: value.durationMinutes,
					description: value.description === "" ? null : value.description,
					pricingPolicyType: value.pricingPolicyType,
				};
				const result = await create.mutateAsync(payload);
				toast.success(`Serviço ${result.name} criado.`);
				handleSaved();
			} catch (err) {
				toast.error(
					err instanceof Error && err.message
						? err.message
						: "Não foi possível criar o serviço. Tente novamente.",
				);
			}
		},
	});

	return (
		<form
			id="service-create-form"
			className={cn("space-y-5", className)}
			noValidate
			onSubmit={(e) => {
				e.preventDefault();
				form.handleSubmit();
			}}
			{...props}
		>
			<FieldGroup>
				<form.Field name="name">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nome do serviço</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="Ex.: Corte masculino"
									type="text"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<div className="grid gap-5 sm:grid-cols-2">
					<form.Field name="basePrice">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor={field.name}>Preço base (R$)</FieldLabel>
									<Input
										id={field.name}
										name={field.name}
										value={
											Number.isFinite(field.state.value)
												? field.state.value
												: ""
										}
										onBlur={field.handleBlur}
										onChange={(e) => {
											const n = Number(e.target.value);
											field.handleChange(Number.isFinite(n) ? n : 0);
										}}
										aria-invalid={isInvalid}
										type="number"
										min="0.01"
										step="0.01"
										inputMode="decimal"
									/>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>

					<form.Field name="durationMinutes">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor={field.name}>Duração (min)</FieldLabel>
									<Input
										id={field.name}
										name={field.name}
										value={
											Number.isFinite(field.state.value)
												? field.state.value
												: ""
										}
										onBlur={field.handleBlur}
										onChange={(e) => {
											const n = Number(e.target.value);
											field.handleChange(Number.isFinite(n) ? n : 0);
										}}
										aria-invalid={isInvalid}
										type="number"
										min="1"
										step="1"
										inputMode="numeric"
									/>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>
				</div>

				<form.Field name="description">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>
									Descrição{" "}
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
									placeholder="Detalhes que ajudam o cliente a escolher"
									rows={3}
									maxLength={500}
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="pricingPolicyType">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Política de preço</FieldLabel>
								<Select
									items={PRICING_POLICY_LABELS}
									value={field.state.value}
									onValueChange={(value) =>
										field.handleChange(value as PricingPolicyType)
									}
								>
									<SelectTrigger
										id={field.name}
										aria-invalid={isInvalid}
										className="w-full cursor-pointer"
									>
										<SelectValue placeholder="Selecione uma política" />
									</SelectTrigger>
									<SelectContent>
										{PRICING_POLICY_KEYS.map((policy) => (
											<SelectItem
												key={policy}
												value={policy}
												className="cursor-pointer"
											>
												<span>{PRICING_POLICY_LABELS[policy]}</span>
												<SelectItemDescription>
													{PRICING_POLICY_HINTS[policy]}
												</SelectItemDescription>
											</SelectItem>
										))}
									</SelectContent>
								</Select>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
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
									Criar
								</Button>
							</div>
						);
					}}
				</form.Subscribe>
			</FieldGroup>
		</form>
	);
}
