import { useForm } from "@tanstack/react-form";
import { useNavigate } from "@tanstack/react-router";
import { ArrowLeft, ArrowRight, LoaderCircle } from "lucide-react";
import type { ComponentProps } from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
	CUSTOMER_STEPS,
	OWNER_STEPS,
	type SignupFormApi,
	type SignupFormValues,
	STEP_FIELDS,
	STEP_TITLES,
	type StepFieldName,
	type StepId,
	signupDefaultValues,
} from "@/lib/config/signup-form.config";
import { useSignupCustomer, useSignupOwner } from "@/lib/queries/auth";
import type {
	SignupCustomerRequest,
	SignupOwnerRequest,
	UserRole,
} from "@/lib/types";
import { cn } from "@/lib/utils";
import { credentialsSchema } from "@/lib/validator/auth.schema";
import {
	signupCustomerSchema,
	signupOwnerSchema,
} from "@/lib/validator/signup.schema";
import { AccountTypeStep } from "./signup/account-type-step";
import { BusinessStep } from "./signup/business-step";
import { CredentialsStep } from "./signup/credentials-step";
import { CustomerFinalizeStep } from "./signup/customer-finalize-step";
import { PlanStep } from "./signup/plan-step";
import { StepIndicator } from "./signup/step-indicator";

function buildCustomerPayload(value: SignupFormValues): SignupCustomerRequest {
	return {
		name: value.name,
		email: value.email,
		password: value.password,
		birthDate: value.birthDate ? value.birthDate : null,
		phone: value.phone ? value.phone : null,
	};
}

function buildOwnerPayload(value: SignupFormValues): SignupOwnerRequest {
	if (value.category === "" || value.plan === "") {
		throw new Error("Owner signup precisa de categoria e plano");
	}
	return {
		ownerName: value.name,
		ownerEmail: value.email,
		ownerPassword: value.password,
		businessTradeName: value.businessTradeName,
		businessEmail: value.businessEmail,
		category: value.category,
		plan: value.plan,
	};
}

export function SignupForm({ className, ...props }: ComponentProps<"div">) {
	const [currentIndex, setCurrentIndex] = useState(0);
	const [direction, setDirection] = useState<"forward" | "backward">("forward");
	const isLastStepRef = useRef(false);
	const navigate = useNavigate();
	const signupCustomer = useSignupCustomer();
	const signupOwner = useSignupOwner();

	const form = useForm({
		defaultValues: signupDefaultValues,
		onSubmit: async ({ value }) => {
			if (!isLastStepRef.current) return;
			try {
				if (value.role === "owner") {
					const result = await signupOwner.mutateAsync(
						buildOwnerPayload(value),
					);
					toast.success(`Negócio criado!`, {
						description: `Bem-vindo ${result.name}.`,
					});
					await navigate({ to: `/owner` });
				} else {
					const result = await signupCustomer.mutateAsync(
						buildCustomerPayload(value),
					);
					toast.success(`Conta criada!`, {
						description: `Bem-vindo ${result.name}.`,
					});
					await navigate({ to: `/customer/me` });
				}
			} catch (err) {
				toast.error(
					err instanceof Error && err.message
						? err.message
						: "Não foi possível concluir o cadastro. Tente novamente.",
				);
			}
		},
	});

	const role = form.state.values.role;
	const steps: readonly StepId[] =
		role === "owner" ? OWNER_STEPS : CUSTOMER_STEPS;
	const safeIndex = Math.min(currentIndex, steps.length - 1);
	const currentStep = steps[safeIndex] ?? steps[0];
	const isLastStep = safeIndex === steps.length - 1;
	const isFirstStep = safeIndex === 0;

	useEffect(() => {
		isLastStepRef.current = isLastStep;
	}, [isLastStep]);

	const handleRoleChange = useCallback(
		(next: UserRole) => {
			const current = form.state.values;
			if (current.role === next) return;
			form.reset({
				...current,
				role: next,
				phone: "",
				birthDate: "",
				businessTradeName: "",
				businessEmail: "",
				category: "",
				plan: "",
			});
		},
		[form],
	);

	const markStepFieldsTouched = useCallback(
		(fields: readonly StepFieldName[]) => {
			for (const name of fields) {
				form.setFieldMeta(name, (prev) => ({ ...prev, isTouched: true }));
			}
		},
		[form],
	);

	const validateCurrentStep = useCallback(async (): Promise<boolean> => {
		const fields = STEP_FIELDS[currentStep];
		markStepFieldsTouched(fields);
		await Promise.all(
			fields.map((name) => Promise.resolve(form.validateField(name, "change"))),
		);

		let fieldsValid = fields.every((name) => {
			const meta = form.state.fieldMeta[name];
			return !meta || meta.errors.length === 0;
		});

		if (fieldsValid && currentStep === "credentials") {
			const { name, email, password, confirmPassword } = form.state.values;
			const parsed = credentialsSchema.safeParse({
				name,
				email,
				password,
				confirmPassword,
			});
			if (!parsed.success) {
				for (const issue of parsed.error.issues) {
					const path = issue.path[0];
					if (typeof path === "string") {
						form.setFieldMeta(path as StepFieldName, (prev) => ({
							...prev,
							isTouched: true,
							errorMap: {
								...prev.errorMap,
								onSubmit: { message: issue.message },
							},
						}));
					}
				}
				fieldsValid = false;
			}
		}

		return fieldsValid;
	}, [currentStep, form, markStepFieldsTouched]);

	const handleNext = useCallback(async () => {
		const ok = await validateCurrentStep();
		if (!ok) return;
		setDirection("forward");
		setCurrentIndex((idx) => Math.min(idx + 1, steps.length - 1));
	}, [steps.length, validateCurrentStep]);

	const handleBack = useCallback(() => {
		setDirection("backward");
		setCurrentIndex((idx) => Math.max(idx - 1, 0));
	}, []);

	const handleFinalSubmit = useCallback(async () => {
		if (form.state.isSubmitting) return;
		const ok = await validateCurrentStep();
		if (!ok) return;
		const schema = role === "owner" ? signupOwnerSchema : signupCustomerSchema;
		const parsed = schema.safeParse(form.state.values);
		if (!parsed.success) {
			toast.error("Revise seus dados antes de continuar.");
			return;
		}
		await form.handleSubmit();
	}, [form, role, validateCurrentStep]);

	return (
		<Card className={cn("mt-6 gap-6 p-8", className)} {...props}>
			<StepIndicator steps={steps} currentIndex={safeIndex} />

			<output aria-live="polite" className="sr-only">
				Passo {safeIndex + 1} de {steps.length}: {STEP_TITLES[currentStep]}
			</output>

			<form
				id="signup-form"
				className="space-y-6"
				noValidate
				onSubmit={(e) => {
					e.preventDefault();
					if (form.state.isSubmitting) return;
					if (isLastStepRef.current) {
						void handleFinalSubmit();
					} else {
						void handleNext();
					}
				}}
			>
				<div
					key={`${currentStep}-${direction}`}
					className={cn(
						"animate-in fade-in duration-300 motion-reduce:animate-none",
						direction === "forward"
							? "slide-in-from-right-4"
							: "slide-in-from-left-4",
					)}
				>
					<h2
						ref={(el) => el?.focus()}
						tabIndex={-1}
						className="sr-only outline-none"
					>
						{STEP_TITLES[currentStep]}
					</h2>

					{currentStep === "accountType" && (
						<AccountTypeStep
							form={form as SignupFormApi}
							onRoleChange={handleRoleChange}
						/>
					)}
					{currentStep === "credentials" && (
						<CredentialsStep form={form as SignupFormApi} role={role} />
					)}
					{currentStep === "customerFinalize" && (
						<CustomerFinalizeStep form={form as SignupFormApi} />
					)}
					{currentStep === "business" && (
						<BusinessStep form={form as SignupFormApi} />
					)}
					{currentStep === "plan" && <PlanStep form={form as SignupFormApi} />}
				</div>

				<form.Subscribe
					selector={(state) => ({
						isSubmitting: state.isSubmitting,
						values: state.values,
					})}
				>
					{({ isSubmitting, values }) => {
						const finalSchema =
							values.role === "owner"
								? signupOwnerSchema
								: signupCustomerSchema;
						const canSubmit =
							isLastStep && finalSchema.safeParse(values).success;
						const finalDisabled = isSubmitting || !canSubmit;
						return (
							<div className="flex items-center justify-between gap-3 pt-2">
								{isFirstStep ? (
									<span aria-hidden />
								) : (
									<Button
										type="button"
										variant="ghost"
										onClick={handleBack}
										disabled={isSubmitting}
										className="cursor-pointer"
									>
										<ArrowLeft className="size-4" />
										Voltar
									</Button>
								)}

								{isLastStep ? (
									<Button
										type="button"
										onClick={() => void handleFinalSubmit()}
										disabled={finalDisabled}
										aria-disabled={finalDisabled}
										className={cn(
											"cursor-pointer",
											finalDisabled && "cursor-not-allowed",
										)}
									>
										{isSubmitting && (
											<LoaderCircle className="size-4 animate-spin" />
										)}
										Criar conta
									</Button>
								) : (
									<Button
										type="button"
										onClick={handleNext}
										disabled={isSubmitting}
										className="cursor-pointer"
									>
										Próximo
										<ArrowRight className="size-4" />
									</Button>
								)}
							</div>
						);
					}}
				</form.Subscribe>
			</form>
		</Card>
	);
}
