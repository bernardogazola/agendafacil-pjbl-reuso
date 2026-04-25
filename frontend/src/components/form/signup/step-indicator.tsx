import type { StepId } from "@/lib/config/signup-form.config";
import { STEP_TITLES } from "@/lib/config/signup-form.config";
import { cn } from "@/lib/utils";

interface StepIndicatorProps {
	steps: readonly StepId[];
	currentIndex: number;
	className?: string;
}

export function StepIndicator({
	steps,
	currentIndex,
	className,
}: StepIndicatorProps) {
	const total = steps.length;
	const currentTitle = STEP_TITLES[steps[currentIndex] ?? steps[0]];

	return (
		<div className={cn("space-y-2", className)}>
			<div className="flex items-center justify-between text-xs">
				<span className="text-muted-foreground font-medium">
					Passo {currentIndex + 1} de {total}
				</span>
				<span className="text-foreground font-medium">{currentTitle}</span>
			</div>
			<ol className="flex gap-1.5" aria-label="Progresso do cadastro">
				{steps.map((step, index) => {
					const isActive = index <= currentIndex;
					const isCurrent = index === currentIndex;
					return (
						<li
							key={step}
							aria-current={isCurrent ? "step" : undefined}
							aria-label={`Passo ${index + 1}: ${STEP_TITLES[step]}`}
							className={cn(
								"h-1.5 flex-1 rounded-full transition-colors duration-300",
								isActive ? "bg-primary" : "bg-muted",
							)}
						/>
					);
				})}
			</ol>
		</div>
	);
}
