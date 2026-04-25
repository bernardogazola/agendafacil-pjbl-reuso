import { CheckIcon, EyeIcon, EyeOffIcon, XIcon } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
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
	InputGroup,
	InputGroupAddon,
	InputGroupInput,
} from "@/components/ui/input-group";
import { Progress } from "@/components/ui/progress";
import {
	Tooltip,
	TooltipContent,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import type { SignupFormApi } from "@/lib/config/signup-form.config";
import type { UserRole } from "@/lib/types";
import { cn } from "@/lib/utils";
import {
	confirmPasswordField,
	emailField,
	getPasswordStrengthLabel,
	nameField,
	PASSWORD_REQUIREMENTS,
	passwordField,
} from "@/lib/validator/auth.schema";

interface CredentialsStepProps {
	form: SignupFormApi;
	role: UserRole | "";
}

export function CredentialsStep({
	form,
	role,
}: Readonly<CredentialsStepProps>) {
	const [showPassword, setShowPassword] = useState(false);
	const [showConfirm, setShowConfirm] = useState(false);

	const nameLabel = role === "owner" ? "Seu nome" : "Nome completo";
	const namePlaceholder =
		role === "owner" ? "Como podemos te chamar?" : "Digite seu nome completo";

	return (
		<div className="space-y-4">
			<div className="space-y-1">
				<FieldTitle className="text-base">Suas credenciais</FieldTitle>
				<FieldDescription>
					{role === "owner"
						? "Estes dados serão usados para acessar o painel do seu negócio."
						: "Use um e-mail que você acessa com frequência."}
				</FieldDescription>
			</div>

			<FieldGroup>
				<form.Field name="name" validators={{ onChange: nameField }}>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor={field.name}>{nameLabel}</FieldLabel>
								<Input
									id={field.name}
									name={field.name}
									value={field.state.value}
									onBlur={field.handleBlur}
									onChange={(e) => field.handleChange(e.target.value)}
									aria-invalid={isInvalid}
									placeholder={namePlaceholder}
									autoComplete="name"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="email" validators={{ onChange: emailField }}>
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
									placeholder="voce@exemplo.com"
									type="email"
									autoComplete="email"
								/>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>

				<form.Field name="password" validators={{ onChange: passwordField }}>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						const hasValue = field.state.value.length > 0;
						const strength = PASSWORD_REQUIREMENTS.map(({ schema, text }) => ({
							met: schema.safeParse(field.state.value).success,
							text,
						}));
						const strengthScore = strength.filter((r) => r.met).length;
						const strengthLabel = getPasswordStrengthLabel(
							strengthScore,
							PASSWORD_REQUIREMENTS.length,
						);
						const descriptionId = "password-description";

						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor="password">Senha</FieldLabel>
								<InputGroup>
									<InputGroupInput
										id="password"
										name="password"
										aria-label="Senha com toggle de visibilidade"
										aria-describedby={hasValue ? descriptionId : undefined}
										aria-invalid={isInvalid}
										placeholder="Digite sua senha"
										type={showPassword ? "text" : "password"}
										value={field.state.value}
										onBlur={field.handleBlur}
										onChange={(e) => field.handleChange(e.target.value)}
										autoComplete="new-password"
									/>
									<InputGroupAddon align="inline-end">
										<Tooltip>
											<TooltipTrigger
												render={
													<Button
														aria-label={
															showPassword ? "Ocultar senha" : "Mostrar senha"
														}
														onClick={() => setShowPassword(!showPassword)}
														size="icon-xs"
														variant="ghost"
														className="cursor-pointer"
														type="button"
													/>
												}
											>
												{showPassword ? <EyeOffIcon /> : <EyeIcon />}
											</TooltipTrigger>
											<TooltipContent>
												{showPassword ? "Ocultar senha" : "Mostrar senha"}
											</TooltipContent>
										</Tooltip>
									</InputGroupAddon>
								</InputGroup>

								<div
									aria-hidden={!hasValue}
									className={cn(
										"grid transition-[grid-template-rows,opacity] duration-300 ease-out motion-reduce:transition-none",
										hasValue
											? "grid-rows-[1fr] opacity-100"
											: "pointer-events-none grid-rows-[0fr] opacity-0",
									)}
								>
									<div className="min-h-0 space-y-3">
										<Progress
											value={
												(strengthScore / PASSWORD_REQUIREMENTS.length) * 100
											}
											aria-label="Força da senha"
											className={cn(
												"mt-1 gap-0",
												strengthScore === 1 &&
													"**:data-[slot=progress-indicator]:bg-red-500",
												strengthScore === 2 &&
													"**:data-[slot=progress-indicator]:bg-orange-500",
												strengthScore === 3 &&
													"**:data-[slot=progress-indicator]:bg-amber-500",
												strengthScore === 4 &&
													"**:data-[slot=progress-indicator]:bg-emerald-500",
												"**:data-[slot=progress-indicator]:transition-all **:data-[slot=progress-indicator]:duration-500",
											)}
										/>

										<p
											className="font-medium text-foreground text-sm"
											id={descriptionId}
										>
											{strengthLabel}. Deve conter:
										</p>

										<ul
											aria-label="Requisitos da senha"
											className="space-y-1.5"
										>
											{strength.map((req) => (
												<li className="flex items-center gap-2" key={req.text}>
													{req.met ? (
														<CheckIcon
															aria-hidden="true"
															className="text-emerald-500"
															size={16}
														/>
													) : (
														<XIcon
															aria-hidden="true"
															className="text-muted-foreground/80"
															size={16}
														/>
													)}
													<span
														className={cn(
															"text-xs",
															req.met
																? "text-emerald-600"
																: "text-muted-foreground",
														)}
													>
														{req.text}
														<span className="sr-only">
															{req.met
																? " - Requisito atendido"
																: " - Requisito não atendido"}
														</span>
													</span>
												</li>
											))}
										</ul>
									</div>
								</div>
							</Field>
						);
					}}
				</form.Field>

				<form.Field
					name="confirmPassword"
					validators={{ onChange: confirmPasswordField }}
				>
					{(field) => {
						const isInvalid =
							field.state.meta.isTouched && !field.state.meta.isValid;
						return (
							<Field data-invalid={isInvalid} className="space-y-3">
								<FieldLabel htmlFor="confirmPassword">
									Confirmar senha
								</FieldLabel>
								<InputGroup>
									<InputGroupInput
										id="confirmPassword"
										name="confirmPassword"
										aria-label="Confirmação de senha com toggle de visibilidade"
										placeholder="Repita sua senha"
										type={showConfirm ? "text" : "password"}
										value={field.state.value}
										onBlur={field.handleBlur}
										onChange={(e) => field.handleChange(e.target.value)}
										aria-invalid={isInvalid}
										autoComplete="new-password"
									/>
									<InputGroupAddon align="inline-end">
										<Tooltip>
											<TooltipTrigger
												render={
													<Button
														aria-label={
															showConfirm
																? "Ocultar confirmação"
																: "Mostrar confirmação"
														}
														onClick={() => setShowConfirm(!showConfirm)}
														size="icon-xs"
														variant="ghost"
														className="cursor-pointer"
														type="button"
													/>
												}
											>
												{showConfirm ? <EyeOffIcon /> : <EyeIcon />}
											</TooltipTrigger>
											<TooltipContent>
												{showConfirm
													? "Ocultar confirmação"
													: "Mostrar confirmação"}
											</TooltipContent>
										</Tooltip>
									</InputGroupAddon>
								</InputGroup>
								{isInvalid && <FieldError errors={field.state.meta.errors} />}
							</Field>
						);
					}}
				</form.Field>
			</FieldGroup>
		</div>
	);
}
