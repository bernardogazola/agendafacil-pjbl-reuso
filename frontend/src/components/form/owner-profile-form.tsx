import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import {
	type ChangeEvent,
	type ComponentProps,
	type KeyboardEvent,
	useRef,
} from "react";
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
import {
	BUSINESS_CATEGORY_HINTS,
	BUSINESS_CATEGORY_LABELS,
	CANCELLATION_POLICY_LABELS,
	PLAN_HINTS,
	PLAN_LABELS,
} from "@/lib/labels";
import {
	BRAZIL_PHONE_MAX_DIGITS,
	BRAZIL_PHONE_MAX_DISPLAY_LENGTH,
	extractDigits,
	formatBrazilianPhone,
} from "@/lib/phone";
import { useUpdateBusiness } from "@/lib/queries/businesses";
import type {
	BusinessCategory,
	BusinessPlan,
	BusinessResponse,
	CancellationPolicyType,
	UpdateBusinessRequest,
} from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type UpdateBusinessFormValues,
	updateBusinessSchema,
} from "@/lib/validator/admin-business.schema";

const CATEGORY_KEYS = Object.keys(
	BUSINESS_CATEGORY_LABELS,
) as BusinessCategory[];
const PLAN_KEYS = Object.keys(PLAN_LABELS) as BusinessPlan[];
const CANCELLATION_KEYS = Object.keys(
	CANCELLATION_POLICY_LABELS,
) as CancellationPolicyType[];

interface OwnerProfileFormProps extends ComponentProps<"form"> {
	business: BusinessResponse;
	/**
	 * Disparado após a atualização ser confirmada pelo backend. Quando informado,
	 * substitui o comportamento padrão de navegar para `/owner/profile`.
	 */
	onSaved?: () => void;
	/**
	 * Disparado ao clicar em "Cancelar". Quando informado, substitui o
	 * comportamento padrão de navegar para `/owner/profile`.
	 */
	onCancel?: () => void;
}

export function OwnerProfileForm({
	business,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<OwnerProfileFormProps>) {
	const navigate = useNavigate();
	const update = useUpdateBusiness(business.id);
	const phoneInputRef = useRef<HTMLInputElement>(null);

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/owner/profile" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/owner/profile" });
	};

	const form = useForm({
		defaultValues: {
			tradeName: business.tradeName,
			email: business.email,
			phone: business.phone ?? "",
			category: business.category,
			plan: business.plan,
			cancellationPolicyType: business.cancellationPolicyType,
		} satisfies UpdateBusinessFormValues,
		validators: {
			onMount: updateBusinessSchema,
			onChange: updateBusinessSchema,
			onSubmit: updateBusinessSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: UpdateBusinessRequest = {
					tradeName: value.tradeName,
					email: value.email,
					phone: value.phone === "" ? null : value.phone,
					category: value.category,
					plan: value.plan,
					cancellationPolicyType: value.cancellationPolicyType,
				};
				const result = await update.mutateAsync(payload);
				toast.success(`Estabelecimento ${result.tradeName} atualizado.`);
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
			id="owner-profile-form"
			className={cn("space-y-5", className)}
			noValidate
			onSubmit={(e) => {
				e.preventDefault();
				form.handleSubmit();
			}}
			{...props}
		>
			<FieldGroup>
				<form.Field name="tradeName">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nome fantasia</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="Ex.: Barbearia Central"
									type="text"
									autoComplete="organization"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="email">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>E-mail</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="contato@seunegocio.com"
									type="email"
									autoComplete="email"
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

				<form.Field name="category">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Categoria</FieldLabel>
								<Select
									items={BUSINESS_CATEGORY_LABELS}
									value={field.state.value}
									onValueChange={(value) =>
										field.handleChange(value as BusinessCategory)
									}
								>
									<SelectTrigger
										id={field.name}
										aria-invalid={isInvalid}
										className="w-full cursor-pointer"
									>
										<SelectValue placeholder="Selecione uma categoria" />
									</SelectTrigger>
									<SelectContent>
										{CATEGORY_KEYS.map((category) => (
											<SelectItem
												key={category}
												value={category}
												className="cursor-pointer"
											>
												<span>{BUSINESS_CATEGORY_LABELS[category]}</span>
												<SelectItemDescription>
													{BUSINESS_CATEGORY_HINTS[category]}
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

				<form.Field name="plan">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Plano</FieldLabel>
								<Select
									items={PLAN_LABELS}
									value={field.state.value}
									onValueChange={(value) =>
										field.handleChange(value as BusinessPlan)
									}
								>
									<SelectTrigger
										id={field.name}
										aria-invalid={isInvalid}
										className="w-full cursor-pointer"
									>
										<SelectValue placeholder="Selecione um plano" />
									</SelectTrigger>
									<SelectContent>
										{PLAN_KEYS.map((plan) => (
											<SelectItem
												key={plan}
												value={plan}
												className="cursor-pointer"
											>
												<span>{PLAN_LABELS[plan]}</span>
												<SelectItemDescription>
													{PLAN_HINTS[plan]}
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

				<form.Field name="cancellationPolicyType">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>
									Política de cancelamento
								</FieldLabel>
								<Select
									items={CANCELLATION_POLICY_LABELS}
									value={field.state.value}
									onValueChange={(value) =>
										field.handleChange(value as CancellationPolicyType)
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
										{CANCELLATION_KEYS.map((policy) => (
											<SelectItem
												key={policy}
												value={policy}
												className="cursor-pointer"
											>
												<span>{CANCELLATION_POLICY_LABELS[policy]}</span>
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
