import type { Column } from "@tanstack/react-table";
import { ArrowDown, ArrowUp, ChevronsUpDown, EyeOff } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

interface DataTableColumnHeaderProps<TData, TValue>
	extends React.HTMLAttributes<HTMLDivElement> {
	column: Column<TData, TValue>;
	title: string;
}

export function DataTableColumnHeader<TData, TValue>({
	column,
	title,
	className,
}: Readonly<DataTableColumnHeaderProps<TData, TValue>>) {
	if (!column.getCanSort()) {
		return (
			<span className={cn("text-sm font-medium", className)}>{title}</span>
		);
	}

	const sorted = column.getIsSorted();
	return (
		<div className={cn("flex items-center gap-1", className)}>
			<DropdownMenu>
				<DropdownMenuTrigger
					render={
						<Button
							variant="ghost"
							size="sm"
							className="-ml-2 h-7 data-[state=open]:bg-muted"
						>
							<span>{title}</span>
							{sorted === "desc" ? (
								<ArrowDown className="size-3.5" />
							) : sorted === "asc" ? (
								<ArrowUp className="size-3.5" />
							) : (
								<ChevronsUpDown className="size-3.5" />
							)}
						</Button>
					}
				/>
				<DropdownMenuContent align="start">
					<DropdownMenuItem onClick={() => column.toggleSorting(false)}>
						<ArrowUp className="size-3.5 text-muted-foreground" />
						Crescente
					</DropdownMenuItem>
					<DropdownMenuItem onClick={() => column.toggleSorting(true)}>
						<ArrowDown className="size-3.5 text-muted-foreground" />
						Decrescente
					</DropdownMenuItem>
					{column.getCanHide() ? (
						<>
							<DropdownMenuSeparator />
							<DropdownMenuItem onClick={() => column.toggleVisibility(false)}>
								<EyeOff className="size-3.5 text-muted-foreground" />
								Ocultar
							</DropdownMenuItem>
						</>
					) : null}
				</DropdownMenuContent>
			</DropdownMenu>
		</div>
	);
}
