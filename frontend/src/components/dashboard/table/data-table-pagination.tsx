import type { Table } from "@tanstack/react-table";
import {
	ChevronLeft,
	ChevronRight,
	ChevronsLeft,
	ChevronsRight,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";

interface DataTablePaginationProps<TData> {
	table: Table<TData>;
	pageSizes?: number[];
}

export function DataTablePagination<TData>({
	table,
	pageSizes = [10, 20, 30, 50, 100],
}: Readonly<DataTablePaginationProps<TData>>) {
	const pageIndex = table.getState().pagination.pageIndex;
	const pageCount = table.getPageCount();
	const totalRows = table.getFilteredRowModel().rows.length;

	return (
		<div className="flex flex-col items-center gap-3 px-2 py-2 sm:flex-row sm:justify-between">
			<p className="text-sm text-muted-foreground">
				{totalRows === 0
					? "Nenhum registro"
					: `${totalRows} ${totalRows === 1 ? "registro" : "registros"}`}
			</p>

			<div className="flex flex-col items-center gap-3 sm:flex-row sm:gap-6">
				<div className="flex items-center gap-2">
					<p className="text-sm font-medium">Por página</p>
					<Select
						value={`${table.getState().pagination.pageSize}`}
						onValueChange={(v) => table.setPageSize(Number(v))}
					>
						<SelectTrigger className="h-8 w-[72px]">
							<SelectValue />
						</SelectTrigger>
						<SelectContent side="top">
							{pageSizes.map((size) => (
								<SelectItem key={size} value={`${size}`}>
									{size}
								</SelectItem>
							))}
						</SelectContent>
					</Select>
				</div>

				<p className="text-sm font-medium">
					Página {pageCount === 0 ? 0 : pageIndex + 1} de {pageCount}
				</p>

				<div className="flex items-center gap-1">
					<Button
						variant="outline"
						size="icon-sm"
						onClick={() => table.setPageIndex(0)}
						disabled={!table.getCanPreviousPage()}
						aria-label="Primeira página"
					>
						<ChevronsLeft className="size-4" />
					</Button>
					<Button
						variant="outline"
						size="icon-sm"
						onClick={() => table.previousPage()}
						disabled={!table.getCanPreviousPage()}
						aria-label="Página anterior"
					>
						<ChevronLeft className="size-4" />
					</Button>
					<Button
						variant="outline"
						size="icon-sm"
						onClick={() => table.nextPage()}
						disabled={!table.getCanNextPage()}
						aria-label="Próxima página"
					>
						<ChevronRight className="size-4" />
					</Button>
					<Button
						variant="outline"
						size="icon-sm"
						onClick={() => table.setPageIndex(Math.max(0, pageCount - 1))}
						disabled={!table.getCanNextPage()}
						aria-label="Última página"
					>
						<ChevronsRight className="size-4" />
					</Button>
				</div>
			</div>
		</div>
	);
}
