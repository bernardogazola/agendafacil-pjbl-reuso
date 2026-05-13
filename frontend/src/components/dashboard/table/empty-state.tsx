import type { LucideIcon } from "lucide-react";

import { cn } from "@/lib/utils";

export interface EmptyStateProps {
	icon?: LucideIcon;
	title: string;
	description?: string;
	action?: React.ReactNode;
	className?: string;
}

export function EmptyState({
	icon: Icon,
	title,
	description,
	action,
	className,
}: Readonly<EmptyStateProps>) {
	return (
		<div
			className={cn(
				"flex flex-col items-center justify-center gap-3 rounded-lg border border-dashed bg-card px-6 py-12 text-center",
				className,
			)}
		>
			{Icon ? (
				<div className="flex size-12 items-center justify-center rounded-full bg-muted text-muted-foreground">
					<Icon className="size-6" aria-hidden />
				</div>
			) : null}
			<div className="space-y-1">
				<p className="text-base font-medium leading-none">{title}</p>
				{description ? (
					<p className="text-sm text-muted-foreground">{description}</p>
				) : null}
			</div>
			{action ? <div className="mt-2">{action}</div> : null}
		</div>
	);
}
