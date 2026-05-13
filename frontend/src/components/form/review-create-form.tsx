import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { LoaderCircle, Star } from "lucide-react";
import type { ComponentProps } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { formatDateTime } from "@/lib/formatters";
import { useCreateReview } from "@/lib/queries/reviews";
import type { AppointmentResponse, CreateReviewRequest } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type CreateReviewFormValues,
	createReviewSchema,
} from "@/lib/validator/customer-review.schema";

interface ReviewCreateFormProps extends ComponentProps<"form"> {
	appointment: AppointmentResponse;
	/**
	 * Disparado após o cadastro ser confirmado pelo backend. Quando informado,
	 * substitui o comportamento padrão de navegar para `/customer/reviews`.
	 * Útil para fechar um `Sheet`/`Dialog` no componente pai.
	 */
	onSaved?: () => void;
	/**
	 * Disparado ao clicar em "Cancelar". Quando informado, substitui o
	 * comportamento padrão de navegar para `/customer/reviews`.
	 */
	onCancel?: () => void;
}

export function ReviewCreateForm({
	appointment,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<ReviewCreateFormProps>) {
	const navigate = useNavigate();
	const create = useCreateReview();

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/customer/reviews" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/customer/reviews" });
	};

	const form = useForm({
		defaultValues: {
			rating: 5,
			comment: "",
		} satisfies CreateReviewFormValues,
		validators: {
			onMount: createReviewSchema,
			onChange: createReviewSchema,
			onSubmit: createReviewSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: CreateReviewRequest = {
					rating: value.rating,
					comment: value.comment === "" ? null : value.comment,
				};
				const result = await create.mutateAsync({
					appointmentId: appointment.id,
					payload,
				});
				toast.success(`Avaliação criada para ${result.businessName}.`);
				handleSaved();
			} catch (err) {
				toast.error(
					err instanceof Error && err.message
						? err.message
						: "Não foi possível enviar a avaliação. Tente novamente.",
				);
			}
		},
	});

	return (
		<form
			id="review-create-form"
			className={cn("space-y-5", className)}
			noValidate
			onSubmit={(e) => {
				e.preventDefault();
				form.handleSubmit();
			}}
			{...props}
		>
			<FieldGroup>
				<div className="rounded-lg border bg-muted/30 p-3 text-sm">
					<p className="font-medium text-foreground">
						{appointment.businessName}
					</p>
					<p className="text-muted-foreground">{appointment.serviceName}</p>
					<p className="mt-1 text-xs text-muted-foreground">
						Atendimento em {formatDateTime(appointment.scheduledAt)}
					</p>
				</div>

				<form.Field name="rating">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nota</FieldLabel>
								<div id={field.name} className="flex gap-1">
									{[1, 2, 3, 4, 5].map((n) => {
										const active = n <= field.state.value;
										return (
											<button
												key={n}
												type="button"
												aria-pressed={n === field.state.value}
												aria-label={`${n} estrela${n > 1 ? "s" : ""}`}
												onBlur={field.handleBlur}
												onClick={() => field.handleChange(n)}
												className={cn(
													"flex size-10 items-center justify-center rounded-md border transition-colors cursor-pointer",
													active
														? "border-amber-400 bg-amber-50 text-amber-500 dark:bg-amber-950/30"
														: "border-input bg-background text-muted-foreground hover:border-amber-300",
												)}
											>
												<Star
													className={cn(
														"size-5",
														active ? "fill-current" : "fill-transparent",
													)}
												/>
											</button>
										);
									})}
								</div>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="comment">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>
									Comentário{" "}
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
									placeholder="Como foi o atendimento?"
									rows={4}
									maxLength={1000}
								/>
								<FieldDescription>
									Até 1000 caracteres. Comentário ajuda outros clientes a
									decidir.
								</FieldDescription>
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
									Criar avaliação
								</Button>
							</div>
						);
					}}
				</form.Subscribe>
			</FieldGroup>
		</form>
	);
}
