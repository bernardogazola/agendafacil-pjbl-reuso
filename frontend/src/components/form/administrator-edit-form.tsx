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
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { ACCESS_LEVEL_LABELS } from "@/lib/labels";
import {
	BRAZIL_PHONE_MAX_DIGITS,
	BRAZIL_PHONE_MAX_DISPLAY_LENGTH,
	extractDigits,
	formatBrazilianPhone,
} from "@/lib/phone";
import { useUpdateAdministrator } from "@/lib/queries/admin/administrators";
import type {
	AccessLevel,
	AdministratorResponse,
	UpdateAdministratorRequest,
} from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	type UpdateAdministratorFormValues,
	updateAdministratorSchema,
} from "@/lib/validator/admin-administrator.schema";

const ACCESS_LEVEL_KEYS = Object.keys(ACCESS_LEVEL_LABELS) as AccessLevel[];

interface AdministratorEditFormProps extends ComponentProps<"form"> {
	administrator: AdministratorResponse;
	/**
	 * Disparado após a atualização ser confirmada pelo backend. Quando informado,
	 * substitui o comportamento padrão de navegar para `/admin/administrators`.
	 * Útil para fechar um `Sheet`/`Dialog` no componente pai.
	 */
	onSaved?: () => void;
	/**
	 * Disparado ao clicar em "Cancelar". Quando informado, substitui o
	 * comportamento padrão de navegar para `/admin/administrators`.
	 */
	onCancel?: () => void;
}

export function AdministratorEditForm({
	administrator,
	onSaved,
	onCancel,
	className,
	...props
}: Readonly<AdministratorEditFormProps>) {
	const navigate = useNavigate();
	const update = useUpdateAdministrator(administrator.id);
	const phoneInputRef = useRef<HTMLInputElement>(null);

	const handleSaved = () => {
		if (onSaved) {
			onSaved();
			return;
		}
		void navigate({ to: "/admin/administrators" });
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
			return;
		}
		void navigate({ to: "/admin/administrators" });
	};

	const form = useForm({
		defaultValues: {
			name: administrator.name,
			phone: administrator.phone ?? "",
			accessLevel: administrator.accessLevel,
			newPassword: "",
		} satisfies UpdateAdministratorFormValues,
		validators: {
			onMount: updateAdministratorSchema,
			onChange: updateAdministratorSchema,
			onSubmit: updateAdministratorSchema,
		},
		onSubmit: async ({ value }) => {
			try {
				const payload: UpdateAdministratorRequest = {
					name: value.name,
					phone: value.phone === "" ? null : value.phone,
					accessLevel: value.accessLevel,
					newPassword: value.newPassword === "" ? null : value.newPassword,
				};
				const result = await update.mutateAsync(payload);
				toast.success(`Administrador ${result.name} atualizado.`);
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
			id="administrator-edit-form"
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
					<FieldLabel htmlFor="administrator-email">E-mail</FieldLabel>
					<Input
						id="administrator-email"
						name="email"
						value={administrator.email}
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
									placeholder="Nome completo do administrador"
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

				<form.Field name="accessLevel">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>Nível de acesso</FieldLabel>
								<Select
									items={ACCESS_LEVEL_LABELS}
									value={field.state.value}
									onValueChange={(value) =>
										field.handleChange(value as AccessLevel)
									}
								>
									<SelectTrigger
										id={field.name}
										aria-invalid={isInvalid}
										className="w-full cursor-pointer"
									>
										<SelectValue placeholder="Selecione um nível de acesso" />
									</SelectTrigger>
									<SelectContent>
										{ACCESS_LEVEL_KEYS.map((level) => (
											<SelectItem
												key={level}
												value={level}
												className="cursor-pointer"
											>
												<span>{ACCESS_LEVEL_LABELS[level]}</span>
											</SelectItem>
										))}
									</SelectContent>
								</Select>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="newPassword">
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>
									Nova senha{" "}
									<span className="text-muted-foreground font-normal">
										(opcional)
									</span>
								</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder="Deixe em branco para manter a senha atual"
									type="password"
									autoComplete="new-password"
								/>
								<FieldDescription>
									Mín. 8 caracteres, com letra maiúscula, minúscula e número.
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
