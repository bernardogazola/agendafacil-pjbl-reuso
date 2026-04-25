import { Store, UserRound } from "lucide-react";
import { type ComponentType, useId } from "react";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldLabel,
	FieldTitle,
} from "@/components/ui/field";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import type { SignupFormApi } from "@/lib/config/signup-form.config";
import type { UserRole } from "@/lib/types";
import { cn } from "@/lib/utils";
import { roleField } from "@/lib/validator/signup.schema";

interface RoleOption {
	value: UserRole;
	icon: ComponentType<{ className?: string; "aria-hidden"?: boolean }>;
	title: string;
	description: string;
}

const ROLE_OPTIONS_UI: RoleOption[] = [
	{
		value: "customer",
		icon: UserRound,
		title: "Sou cliente",
		description: "Quero agendar serviços de forma simples.",
	},
	{
		value: "owner",
		icon: Store,
		title: "Tenho um negócio",
		description: "Quero oferecer serviços e gerenciar minha agenda.",
	},
];

interface AccountTypeStepProps {
	form: SignupFormApi;
	onRoleChange: (role: UserRole) => void;
}

export function AccountTypeStep({
	form,
	onRoleChange,
}: Readonly<AccountTypeStepProps>) {
	const id = useId();
	return (
		<form.Field name="role" validators={{ onChange: roleField }}>
			{(field) => {
				const isInvalid =
					field.state.meta.isTouched && !field.state.meta.isValid;
				return (
					<Field data-invalid={isInvalid} className="gap-4">
						<div className="space-y-1">
							<FieldTitle className="text-base">
								Primeiro, o essencial
							</FieldTitle>
							<FieldDescription>
								Vai agendar serviços ou oferecê-los? A partir daqui, o cadastro
								se adapta a você.
							</FieldDescription>
						</div>
						<RadioGroup
							name={field.name}
							value={field.state.value}
							onValueChange={(value) => {
								const next = value as UserRole;
								field.handleChange(next);
								onRoleChange(next);
							}}
							className="grid gap-3 sm:grid-cols-2"
						>
							{ROLE_OPTIONS_UI.map((option) => {
								const Icon = option.icon;
								const inputId = `${id}-${option.value}`;
								const descriptionId = `${inputId}-description`;
								return (
									<FieldLabel
										key={option.value}
										htmlFor={inputId}
										className={cn(
											"relative flex w-full items-start gap-2 rounded-md border border-input p-4 shadow-xs outline-none transition-colors cursor-pointer",
											"hover:border-primary/40 hover:bg-muted/40",
											"has-data-checked:border-primary/50 has-data-checked:bg-primary/5 has-data-checked:cursor-default",
										)}
									>
										<RadioGroupItem
											id={inputId}
											value={option.value}
											aria-describedby={descriptionId}
											className="order-1 after:absolute after:inset-0"
										/>
										<div className="flex grow items-start gap-3">
											<Icon
												aria-hidden
												className="size-6 shrink-0 text-foreground/80"
											/>
											<div className="grid grow gap-1">
												<Label htmlFor={inputId} className="font-medium">
													{option.title}
												</Label>
												<p
													id={descriptionId}
													className="text-muted-foreground text-xs"
												>
													{option.description}
												</p>
											</div>
										</div>
									</FieldLabel>
								);
							})}
						</RadioGroup>
						{isInvalid && <FieldError errors={field.state.meta.errors} />}
					</Field>
				);
			}}
		</form.Field>
	);
}
