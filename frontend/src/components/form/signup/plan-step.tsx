import { CheckIcon, SparklesIcon } from "lucide-react";
import {
	Field,
	FieldDescription,
	FieldError,
	FieldLabel,
	FieldTitle,
} from "@/components/ui/field";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import type { SignupFormApi } from "@/lib/config/signup-form.config";
import {
	PLAN_FEATURES,
	PLAN_HINTS,
	PLAN_LABELS,
	PLAN_PRICES,
} from "@/lib/labels";
import type { BusinessPlan } from "@/lib/types";
import { cn } from "@/lib/utils";
import { PLAN_OPTIONS, planField } from "@/lib/validator/signup.schema";

interface PlanStepProps {
	form: SignupFormApi;
}

const HIGHLIGHTED_PLAN: BusinessPlan = "PROFESSIONAL";

export function PlanStep({ form }: Readonly<PlanStepProps>) {
	return (
		<form.Field name="plan" validators={{ onChange: planField }}>
			{(field) => {
				const isInvalid =
					field.state.meta.isTouched && !field.state.meta.isValid;
				const selected = field.state.value as BusinessPlan | "";
				return (
					<Field data-invalid={isInvalid} className="gap-4">
						<div className="space-y-1">
							<FieldTitle className="text-base">Escolha seu plano</FieldTitle>
							<FieldDescription>
								Você pode mudar de plano a qualquer momento. Sem compromisso.
							</FieldDescription>
						</div>

						<RadioGroup
							name={field.name}
							value={selected}
							onValueChange={(value) =>
								field.handleChange(value as BusinessPlan)
							}
							className="@container grid gap-4 pt-2 @md:grid-cols-2 @xl:grid-cols-3"
						>
							{PLAN_OPTIONS.map((plan) => {
								const isChecked = selected === plan;
								const isHighlighted = plan === HIGHLIGHTED_PLAN;
								const featuresId = `plan-${plan}-features`;
								return (
									<FieldLabel
										key={plan}
										htmlFor={`plan-${plan}`}
										data-highlighted={isHighlighted || undefined}
										className={cn(
											"group/plan relative flex w-full flex-col items-stretch cursor-pointer overflow-visible",
											"rounded-xl border border-input bg-card p-5 shadow-xs",
											"transition-[transform,box-shadow,border-color,background-color] duration-200 ease-out",
											"motion-reduce:transition-none",
											"hover:-translate-y-0.5 hover:border-primary/40 hover:bg-muted/30 hover:shadow-sm",
											"motion-reduce:hover:translate-y-0",
											"has-data-checked:border-primary has-data-checked:bg-primary/5",
											"has-data-checked:shadow-sm has-data-checked:ring-2 has-data-checked:ring-primary/15",
											"has-data-checked:cursor-default has-data-checked:hover:translate-y-0",
											"data-highlighted:ring-1 data-highlighted:ring-primary/20",
											"animate-in fade-in slide-in-from-bottom-1 duration-300 motion-reduce:animate-none",
										)}
									>
										{isHighlighted && (
											<span
												aria-hidden
												className={cn(
													"absolute -top-2.5 left-1/2 -translate-x-1/2",
													"inline-flex items-center gap-1 rounded-full",
													"border border-primary/30 bg-primary px-2.5 py-0.5",
													"text-[0.65rem] font-medium text-primary-foreground shadow-xs",
												)}
											>
												<SparklesIcon className="size-3" />
												Mais popular
											</span>
										)}

										<div className="flex flex-col gap-4">
											<div className="flex items-start justify-between gap-3">
												<div className="min-w-0 space-y-1">
													<div className="font-serif text-lg leading-none font-medium text-foreground">
														{PLAN_LABELS[plan]}
													</div>
													<p className="text-xs text-muted-foreground">
														{PLAN_HINTS[plan]}
													</p>
												</div>
												<RadioGroupItem
													id={`plan-${plan}`}
													value={plan}
													aria-describedby={featuresId}
													className="mt-0.5 after:absolute after:inset-0"
												/>
											</div>

											<div className="flex items-baseline gap-1">
												<span className="font-serif text-4xl font-medium tracking-tight text-foreground">
													{PLAN_PRICES[plan].amount}
												</span>
												<span className="text-sm text-muted-foreground">
													{PLAN_PRICES[plan].period}
												</span>
											</div>

											<div className="h-px bg-border" aria-hidden />

											<ul id={featuresId} className="space-y-2 text-sm">
												{PLAN_FEATURES[plan].map((feature) => (
													<li
														key={feature}
														className="flex items-start gap-2 text-muted-foreground"
													>
														<span
															aria-hidden
															className={cn(
																"mt-0.5 inline-flex size-4 shrink-0 items-center justify-center rounded-full",
																"transition-colors duration-200 motion-reduce:transition-none",
																isChecked
																	? "bg-primary/15 text-primary"
																	: "bg-muted text-foreground/70",
															)}
														>
															<CheckIcon className="size-3" strokeWidth={3} />
														</span>
														<span className="leading-snug">{feature}</span>
													</li>
												))}
											</ul>
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
