import type { Table } from "@tanstack/react-table";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { useEffect, useState } from "react";

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
import { Input } from "@/components/ui/input";

interface DataTableToolbarProps<TData> {
	table: Table<TData>;
	searchPlaceholder?: string;
	children?: React.ReactNode;
}

export function DataTableToolbar<TData>({
	table,
	searchPlaceholder = "Buscar…",
	children,
}: Readonly<DataTableToolbarProps<TData>>) {
	const [value, setValue] = useState<string>(
		(table.getState().globalFilter as string | undefined) ?? "",
	);

	useEffect(() => {
		const t = setTimeout(() => table.setGlobalFilter(value || ""), 200);
		return () => clearTimeout(t);
	}, [value, table]);

	const isFiltered =
		table.getState().columnFilters.length > 0 ||
		Boolean(table.getState().globalFilter);

	const hideableColumns = table.getAllColumns().filter((c) => c.getCanHide());

	return (
		<div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
			<div className="flex flex-1 flex-wrap items-center gap-2">
				<div className="relative w-full sm:w-64">
					<Search className="absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
					<Input
						value={value}
						onChange={(e) => setValue(e.target.value)}
						placeholder={searchPlaceholder}
						className="h-8 pl-8"
					/>
				</div>
				{children}
				{isFiltered ? (
					<Button
						variant="ghost"
						size="sm"
						onClick={() => {
							table.resetColumnFilters();
							table.setGlobalFilter("");
							setValue("");
						}}
						className="h-8"
					>
						Limpar
						<X className="size-3.5" />
					</Button>
				) : null}
			</div>

			{hideableColumns.length > 0 ? (
				<DropdownMenu>
					<DropdownMenuTrigger
						render={
							<Button variant="outline" size="sm" className="h-8">
								<SlidersHorizontal className="size-3.5" />
								Colunas
							</Button>
						}
					/>
					<DropdownMenuContent align="end">
						<DropdownMenuGroup>
							<DropdownMenuLabel>Exibir colunas</DropdownMenuLabel>
							<DropdownMenuSeparator />
							{hideableColumns.map((col) => (
								<DropdownMenuCheckboxItem
									key={col.id}
									checked={col.getIsVisible()}
									onCheckedChange={(v) => col.toggleVisibility(!!v)}
								>
									{(col.columnDef.meta as { label?: string } | undefined)
										?.label ?? col.id}
								</DropdownMenuCheckboxItem>
							))}
						</DropdownMenuGroup>
					</DropdownMenuContent>
				</DropdownMenu>
			) : null}
		</div>
	);
}
