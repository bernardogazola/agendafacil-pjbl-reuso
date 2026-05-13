import type { Column } from "@tanstack/react-table";
import { Check, CirclePlus, X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuCheckboxItem,
	DropdownMenuContent,
	DropdownMenuGroup,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

export interface FilterOption {
	value: string;
	label: string;
	icon?: React.ComponentType<{ className?: string }>;
}

interface DataTableFilterProps<TData, TValue> {
	column?: Column<TData, TValue>;
	title: string;
	options: FilterOption[];
}

export function DataTableFilter<TData, TValue>({
	column,
	title,
	options,
}: Readonly<DataTableFilterProps<TData, TValue>>) {
	const selectedValues = new Set((column?.getFilterValue() as string[]) ?? []);

	return (
		<DropdownMenu>
			<DropdownMenuTrigger
				render={
					<Button variant="outline" size="sm" className="h-8 border-dashed">
						<CirclePlus className="size-3.5" />
						{title}
						{selectedValues.size > 0 ? (
							<>
								<span className="mx-1 h-4 w-px bg-border" aria-hidden />
								<Badge
									variant="secondary"
									className="rounded-sm px-1 font-normal"
								>
									{selectedValues.size}
								</Badge>
								<div className="hidden gap-1 lg:flex">
									{options
										.filter((o) => selectedValues.has(o.value))
										.slice(0, 2)
										.map((o) => (
											<Badge
												key={o.value}
												variant="secondary"
												className="rounded-sm px-1 font-normal"
											>
												{o.label}
											</Badge>
										))}
									{selectedValues.size > 2 ? (
										<Badge
											variant="secondary"
											className="rounded-sm px-1 font-normal"
										>
											+{selectedValues.size - 2}
										</Badge>
									) : null}
								</div>
							</>
						) : null}
					</Button>
				}
			/>
			<DropdownMenuContent align="start" className="w-[220px]">
				<DropdownMenuGroup>
					<DropdownMenuLabel>{title}</DropdownMenuLabel>
					<DropdownMenuSeparator />
					{options.map((option) => {
						const checked = selectedValues.has(option.value);
						return (
							<DropdownMenuCheckboxItem
								key={option.value}
								checked={checked}
								onCheckedChange={() => {
									if (checked) selectedValues.delete(option.value);
									else selectedValues.add(option.value);
									const next = Array.from(selectedValues);
									column?.setFilterValue(next.length ? next : undefined);
								}}
							>
								{option.icon ? (
									<option.icon
										className={cn("mr-2 size-4 text-muted-foreground")}
									/>
								) : null}
								<span>{option.label}</span>
								{checked ? (
									<Check className="ml-auto size-3.5 text-muted-foreground" />
								) : null}
							</DropdownMenuCheckboxItem>
						);
					})}
					{selectedValues.size > 0 ? (
						<>
							<DropdownMenuSeparator />
							<DropdownMenuCheckboxItem
								checked={false}
								onCheckedChange={() => column?.setFilterValue(undefined)}
							>
								<X className="mr-2 size-3.5 text-muted-foreground" />
								Limpar filtro
							</DropdownMenuCheckboxItem>
						</>
					) : null}
				</DropdownMenuGroup>
			</DropdownMenuContent>
		</DropdownMenu>
	);
}
