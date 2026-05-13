import {
	type ColumnDef,
	type ColumnFiltersState,
	flexRender,
	getCoreRowModel,
	getFilteredRowModel,
	getPaginationRowModel,
	getSortedRowModel,
	type SortingState,
	type Table as TableInstance,
	useReactTable,
	type VisibilityState,
} from "@tanstack/react-table";
import { Inbox, Loader2 } from "lucide-react";
import { useMemo, useState } from "react";

import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { cn } from "@/lib/utils";

import { DataTablePagination } from "./data-table-pagination.tsx";
import { DataTableToolbar } from "./data-table-toolbar.tsx";
import { EmptyState } from "./empty-state.tsx";

export interface DataTableProps<TData, TValue> {
	columns: ColumnDef<TData, TValue>[];
	data: TData[];
	isLoading?: boolean;
	emptyTitle?: string;
	emptyDescription?: string;
	searchPlaceholder?: string;
	filters?:
		| React.ReactNode
		| ((table: TableInstance<TData>) => React.ReactNode);
	toolbarRight?: React.ReactNode;
	initialPageSize?: number;
	hideToolbar?: boolean;
	hidePagination?: boolean;
	className?: string;
}

export function DataTable<TData, TValue>({
	columns,
	data,
	isLoading = false,
	emptyTitle = "Nada por aqui",
	emptyDescription = "Nenhum registro encontrado.",
	searchPlaceholder,
	filters,
	toolbarRight,
	initialPageSize = 10,
	hideToolbar = false,
	hidePagination = false,
	className,
}: Readonly<DataTableProps<TData, TValue>>) {
	const [sorting, setSorting] = useState<SortingState>([]);
	const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
	const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
	const [globalFilter, setGlobalFilter] = useState<string>("");

	const initialPagination = useMemo(
		() => ({ pageIndex: 0, pageSize: initialPageSize }),
		[initialPageSize],
	);
	const [pagination, setPagination] = useState(initialPagination);

	const table = useReactTable({
		data,
		columns,
		state: {
			sorting,
			columnFilters,
			columnVisibility,
			globalFilter,
			pagination,
		},
		onSortingChange: setSorting,
		onColumnFiltersChange: setColumnFilters,
		onColumnVisibilityChange: setColumnVisibility,
		onGlobalFilterChange: setGlobalFilter,
		onPaginationChange: setPagination,
		getCoreRowModel: getCoreRowModel(),
		getSortedRowModel: getSortedRowModel(),
		getFilteredRowModel: getFilteredRowModel(),
		getPaginationRowModel: getPaginationRowModel(),
		globalFilterFn: "includesString",
	});

	const rows = table.getRowModel().rows;

	return (
		<div className={cn("flex flex-col gap-3", className)}>
			{hideToolbar ? null : (
				<div className="flex flex-wrap items-center justify-between gap-2">
					<div className="flex-1">
						<DataTableToolbar
							table={table}
							searchPlaceholder={searchPlaceholder}
						>
							{typeof filters === "function" ? filters(table) : filters}
						</DataTableToolbar>
					</div>
					{toolbarRight ? (
						<div className="flex shrink-0 items-center gap-2">
							{toolbarRight}
						</div>
					) : null}
				</div>
			)}

			<div className="overflow-hidden rounded-lg border bg-card">
				<Table>
					<TableHeader>
						{table.getHeaderGroups().map((headerGroup) => (
							<TableRow key={headerGroup.id}>
								{headerGroup.headers.map((header) => (
									<TableHead key={header.id} className="whitespace-nowrap">
										{header.isPlaceholder
											? null
											: flexRender(
													header.column.columnDef.header,
													header.getContext(),
												)}
									</TableHead>
								))}
							</TableRow>
						))}
					</TableHeader>
					<TableBody>
						{isLoading ? (
							<TableRow>
								<TableCell
									colSpan={columns.length}
									className="h-24 text-center"
								>
									<span className="inline-flex items-center gap-2 text-sm text-muted-foreground">
										<Loader2 className="size-4 animate-spin" />
										Carregando…
									</span>
								</TableCell>
							</TableRow>
						) : rows.length === 0 ? (
							<TableRow>
								<TableCell colSpan={columns.length} className="p-0">
									<EmptyState
										icon={Inbox}
										title={emptyTitle}
										description={emptyDescription}
										className="border-0"
									/>
								</TableCell>
							</TableRow>
						) : (
							rows.map((row) => (
								<TableRow
									key={row.id}
									data-state={row.getIsSelected() ? "selected" : undefined}
								>
									{row.getVisibleCells().map((cell) => (
										<TableCell key={cell.id} className="align-middle">
											{flexRender(
												cell.column.columnDef.cell,
												cell.getContext(),
											)}
										</TableCell>
									))}
								</TableRow>
							))
						)}
					</TableBody>
				</Table>
			</div>

			{!hidePagination ? <DataTablePagination table={table} /> : null}
		</div>
	);
}

export { DataTableColumnHeader } from "./data-table-column-header.tsx";
export { DataTableFilter } from "./data-table-filter.tsx";
export { DataTableRowActions } from "./data-table-row-actions.tsx";
