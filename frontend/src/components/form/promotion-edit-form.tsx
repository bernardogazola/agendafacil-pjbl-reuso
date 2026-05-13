import { useForm } from "@tanstack/react-form";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import type { ComponentProps } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { Textarea } from "@/components/ui/textarea";
import { businessServicesOptions } from "@/lib/queries/businesses";
import { useUpdatePromotion } from "@/lib/queries/promotions";
import type { PromotionResponse, UpdatePromotionRequest } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type DiscountKind,
	type UpdatePromotionFormValues,
	updatePromotionSchema,
} from "@/lib/validator/owner-promotion.schema";

interface PromotionEditFormProps extends ComponentProps<"form"> {
	businessId: number;
	promotion: PromotionResponse;
	onSaved?: () => void;
	onCancel?: () => void;
}

export function PromotionEditForm({
	businessId,
	promotion,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<PromotionEditFormProps>) {
	const navigate = useNavigate();
	const update = useUpdatePromotion(businessId, promotion.id);
	const servicesQ = useQuery(businessServicesOptions(businessId));

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/owner/promotions" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/owner/promotions" });
	};

	const form = useForm({
		defaultValues: {
			name: promotion.name,
			description: promotion.description ?? "",
			discountKind: (promotion.discountPercentage == null
				? "AMT"
				: "PCT") as DiscountKind,
			discountPercentage:
				promotion.discountPercentage == null
					? ""
					: String(promotion.discountPercentage),
			discountAmount:
				promotion.discountAmount == null
					? ""
					: String(promotion.discountAmount),
			validFrom: promotion.validFrom,
			validTo: promotion.validTo,
			eligibleServiceIds: [...promotion.eligibleServiceIds],
			active: promotion.active,
		} satisfies UpdatePromotionFormValues,
		validators: {
			onMount: updatePromotionSchema,
			onChange: updatePromotionSchema,
			onSubmit: updatePromotionSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: UpdatePromotionRequest = {
					name: value.name,
					description: value.description === "" ? null : value.description,
					discountPercentage:
						value.discountKind === "PCT"
							? Number(value.discountPercentage)
							: null,
					discountAmount:
						value.discountKind === "AMT" ? Number(value.discountAmount) : null,
					validFrom: value.validFrom,
					validTo: value.validTo,
					eligibleServiceIds: value.eligibleServiceIds,
					active: value.active,
				};
				const result = await update.mutateAsync(payload);
				toast.success(`Promoção ${result.name} atualizada.`);
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

	const services = servicesQ.data ?? [];

	return (
		<form
			id="promotion-edit-form"
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
								<FieldLabel htmlFor={field.name}>Nome</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									type="text"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

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
									rows={3}
									maxLength={500}
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="discountKind">
					{(field) => (
						<FieldSet>
							<FieldLegend>Tipo de desconto</FieldLegend>
							<FieldDescription>
								Use percentual OU valor fixo, nunca os dois.
							</FieldDescription>
							<FieldGroup data-slot="radio-group" className="gap-3">
								<Field orientation="horizontal" className="gap-2">
									<input
										type="radio"
										id={`${field.name}-PCT`}
										name={field.name}
										checked={field.state.value === "PCT"}
										onChange={() => field.handleChange("PCT")}
										className="size-4 cursor-pointer accent-primary"
									/>
									<FieldLabel
										htmlFor={`${field.name}-PCT`}
										className="font-normal"
									>
										Percentual (%)
									</FieldLabel>
								</Field>
								<Field orientation="horizontal" className="gap-2">
									<input
										type="radio"
										id={`${field.name}-AMT`}
										name={field.name}
										checked={field.state.value === "AMT"}
										onChange={() => field.handleChange("AMT")}
										className="size-4 cursor-pointer accent-primary"
									/>
									<FieldLabel
										htmlFor={`${field.name}-AMT`}
										className="font-normal"
									>
										Valor fixo (R$)
									</FieldLabel>
								</Field>
							</FieldGroup>
						</FieldSet>
					)}
				</form.Field>

				<form.Subscribe selector={(s) => s.values.discountKind}>
					{(discountKind) =>
						discountKind === "PCT" ? (
							<form.Field name="discountPercentage">
								{(field) => {
									const isInvalid =
										field.state.meta.isTouched && !field.state.meta.isValid;
									return (
										<Field data-invalid={isInvalid} className="space-y-3">
											<FieldLabel htmlFor={field.name}>Desconto (%)</FieldLabel>
											<Input
												id={field.name}
												name={field.name}
												value={field.state.value}
												onBlur={field.handleBlur}
												onChange={(e) => field.handleChange(e.target.value)}
												aria-invalid={isInvalid}
												type="number"
												min="0.01"
												max="100"
												step="0.01"
												inputMode="decimal"
											/>
											{isInvalid && (
												<FieldError errors={field.state.meta.errors} />
											)}
										</Field>
									);
								}}
							</form.Field>
						) : (
							<form.Field name="discountAmount">
								{(field) => {
									const isInvalid =
										field.state.meta.isTouched && !field.state.meta.isValid;
									return (
										<Field data-invalid={isInvalid} className="space-y-3">
											<FieldLabel htmlFor={field.name}>
												Desconto (R$)
											</FieldLabel>
											<Input
												id={field.name}
												name={field.name}
												value={field.state.value}
												onBlur={field.handleBlur}
												onChange={(e) => field.handleChange(e.target.value)}
												aria-invalid={isInvalid}
												type="number"
												min="0.01"
												step="0.01"
												inputMode="decimal"
											/>
											{isInvalid && (
												<FieldError errors={field.state.meta.errors} />
											)}
										</Field>
									);
								}}
							</form.Field>
						)
					}
				</form.Subscribe>

				<div className="grid gap-5 sm:grid-cols-2">
					<form.Field name="validFrom">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor={field.name}>Vigência início</FieldLabel>
									<Input
										id={field.name}
										name={field.name}
										value={field.state.value}
										onBlur={field.handleBlur}
										onChange={(e) => field.handleChange(e.target.value)}
										aria-invalid={isInvalid}
										type="date"
									/>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>

					<form.Field name="validTo">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor={field.name}>Vigência fim</FieldLabel>
									<Input
										id={field.name}
										name={field.name}
										value={field.state.value}
										onBlur={field.handleBlur}
										onChange={(e) => field.handleChange(e.target.value)}
										aria-invalid={isInvalid}
										type="date"
									/>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>
				</div>

				<form.Field name="eligibleServiceIds" mode="array">
					{(field) => {
						const value = field.state.value as number[];
						const toggle = (id: number, checked: boolean) => {
							const next = checked
								? Array.from(new Set([...value, id]))
								: value.filter((x) => x !== id);
							field.handleChange(next);
							field.handleBlur();
						};
						return (
							<FieldSet>
								<FieldLegend>Serviços elegíveis</FieldLegend>
								<FieldDescription>
									Deixe tudo desmarcado para aplicar a todos os serviços.
								</FieldDescription>
								<FieldGroup data-slot="checkbox-group" className="gap-3">
									{servicesQ.isLoading ? (
										<p className="text-sm text-muted-foreground">
											Carregando serviços…
										</p>
									) : services.length === 0 ? (
										<p className="text-sm text-muted-foreground">
											Nenhum serviço cadastrado.
										</p>
									) : (
										services.map((svc) => {
											const id = `${field.name}-${svc.id}`;
											const checked = value.includes(svc.id);
											return (
												<Field
													key={svc.id}
													orientation="horizontal"
													className="gap-2"
												>
													<Checkbox
														id={id}
														checked={checked}
														onCheckedChange={(next) =>
															toggle(svc.id, Boolean(next))
														}
													/>
													<FieldLabel htmlFor={id} className="font-normal">
														{svc.name}
													</FieldLabel>
												</Field>
											);
										})
									)}
								</FieldGroup>
							</FieldSet>
						);
					}}
				</form.Field>

				<form.Field name="active">
					{(field) => (
						<Field orientation="horizontal" className="gap-2">
							<Checkbox
								id={field.name}
								checked={field.state.value}
								onCheckedChange={(next) => field.handleChange(Boolean(next))}
							/>
							<FieldLabel htmlFor={field.name} className="font-normal">
								Promoção ativa
							</FieldLabel>
						</Field>
					)}
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
