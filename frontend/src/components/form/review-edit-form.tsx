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
import { useUpdateReview } from "@/lib/queries/reviews";
import type { ReviewResponse, UpdateReviewRequest } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type UpdateReviewFormValues,
	updateReviewSchema,
} from "@/lib/validator/customer-review.schema";

interface ReviewEditFormProps extends ComponentProps<"form"> {
	review: ReviewResponse;
	/**
	 * Disparado após a atualização ser confirmada pelo backend. Quando informado,
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

export function ReviewEditForm({
	review,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<ReviewEditFormProps>) {
	const navigate = useNavigate();
	const update = useUpdateReview(review.id);

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
			rating: review.rating,
			comment: review.comment ?? "",
		} satisfies UpdateReviewFormValues,
		validators: {
			onMount: updateReviewSchema,
			onChange: updateReviewSchema,
			onSubmit: updateReviewSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: UpdateReviewRequest = {
					rating: value.rating,
					comment: value.comment === "" ? null : value.comment,
				};
				const result = await update.mutateAsync(payload);
				toast.success(`Avaliação atualizada para ${result.businessName}.`);
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
			id="review-edit-form"
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
					<p className="font-medium text-foreground">{review.businessName}</p>
					<p className="text-muted-foreground">{review.serviceName}</p>
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
									Até 1000 caracteres. Deixe em branco para remover o
									comentário.
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
