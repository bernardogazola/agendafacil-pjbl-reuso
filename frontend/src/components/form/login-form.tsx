import { useForm } from "@tanstack/react-form";
import { EyeIcon, EyeOffIcon, LoaderCircle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
	Field,
	FieldError,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	InputGroup,
	InputGroupAddon,
	InputGroupInput,
} from "@/components/ui/input-group";
import {
	Tooltip,
	TooltipContent,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";

const formSchema = z.object({
	email: z
		.string()
		.min(1, "E-mail é obrigatório")
		.max(32, "E-mail inválido")
		.pipe(z.email({ error: "E-mail inválido" })),
	password: z.string().min(1, "Senha é obrigatória").max(32, "Senha inválida"),
});

export function LoginForm({
	className,
	...props
}: React.ComponentProps<"div">) {
	const [showPassword, setShowPassword] = useState(false);
	const form = useForm({
		defaultValues: {
			email: "",
			password: "",
		},
		validators: {
			onMount: formSchema,
			onSubmit: formSchema,
			onChange: formSchema,
		},
		onSubmit: async ({ value }) => {
			toast.success("Debug: valores enviados", {
				description: (
					<pre className="mt-2 w-[320px] overflow-x-auto rounded-md bg-code p-4 text-code-foreground">
						<code>{JSON.stringify(value, null, 2)}</code>
					</pre>
				),
			});
		},
	});
	return (
		<Card className={cn("mt-6 p-8", className)} {...props}>
			<form
				id="login-form"
				className="space-y-5"
				onSubmit={(e) => {
					e.preventDefault();
					form.handleSubmit();
				}}
			>
				<FieldGroup>
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
										placeholder="Digite seu e-mail"
										type="email"
										autoComplete="email"
									/>
									{isInvalid && <FieldError errors={field.state.meta.errors} />}
								</Field>
							);
						}}
					</form.Field>

					<form.Field name="password">
						{(field) => {
							const isInvalid =
								field.state.meta.isTouched && !field.state.meta.isValid;
							return (
								<Field data-invalid={isInvalid} className="space-y-3">
									<FieldLabel htmlFor="password">Senha</FieldLabel>
									<InputGroup>
										<InputGroupInput
											id="password"
											name="password"
											aria-label="Senha com toggle de visibilidade"
											placeholder="Digite sua senha"
											type={showPassword ? "text" : "password"}
											value={field.state.value}
											onBlur={field.handleBlur}
											onChange={(e) => field.handleChange(e.target.value)}
											aria-invalid={isInvalid}
											autoComplete="current-password"
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
								<Button
									disabled={disableSubmit}
									type="submit"
									className={cn(
										"w-full",
										disableSubmit ? "cursor-not-allowed" : "cursor-pointer",
									)}
								>
									{isSubmitting && (
										<LoaderCircle className="mr-2 h-4 w-4 animate-spin" />
									)}
									Entrar
								</Button>
							);
						}}
					</form.Subscribe>
				</FieldGroup>
			</form>
		</Card>
	);
}
