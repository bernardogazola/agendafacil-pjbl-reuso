import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import type { ColumnDef } from "@tanstack/react-table";
import { LoaderCircle, Pencil, Power, PowerOff } from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import { ActiveBadge } from "@/components/dashboard/status-badge";
import {
	DataTable,
	DataTableColumnHeader,
	DataTableRowActions,
} from "@/components/dashboard/table/data-table";
import {
	DropdownMenuItem,
	DropdownMenuSeparator,
} from "@/components/dashboard/table/data-table-row-actions";
import { CustomerEditForm } from "@/components/form/customer-edit-form";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { NOTIFICATION_CHANNEL_LABELS } from "@/lib/labels";
import {
	adminCustomerDetailOptions,
	adminCustomersListOptions,
	useDeactivateCustomer,
	useReactivateCustomer,
} from "@/lib/queries/admin/customers";
import type { CustomerResponse } from "@/lib/types";

const editSearchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
});

export const Route = createFileRoute("/admin/customers")({
	staticData: { crumb: "Clientes" },
	validateSearch: editSearchSchema,
	loader: ({ context }) =>
		context.queryClient.ensureQueryData(adminCustomersListOptions(false)),
	component: AdminCustomersPage,
});

interface ConfirmTarget {
	id: number;
	name: string;
	activeNow: boolean;
}

function useColumns(
	onToggle: (target: ConfirmTarget) => void,
	onEdit: (customer: CustomerResponse) => void,
): ColumnDef<CustomerResponse>[] {
	return useMemo(
		() => [
			{
				accessorKey: "name",
				meta: { label: "Nome" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Nome" />
				),
				cell: ({ row }) => (
					<span className="font-medium">{row.original.name}</span>
				),
			},
			{
				accessorKey: "email",
				meta: { label: "E-mail" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="E-mail" />
				),
			},
			{
				accessorKey: "phone",
				meta: { label: "Telefone" },
				header: "Telefone",
				cell: ({ row }) => row.original.phone ?? "—",
				enableSorting: false,
			},
			{
				accessorKey: "notificationPreferences",
				meta: { label: "Canais" },
				header: "Canais",
				enableSorting: false,
				cell: ({ row }) =>
					row.original.notificationPreferences.length === 0 ? (
						<span className="text-muted-foreground">—</span>
					) : (
						<div className="flex flex-wrap gap-1">
							{row.original.notificationPreferences.map((c) => (
								<Badge key={c} variant="outline">
									{NOTIFICATION_CHANNEL_LABELS[c]}
								</Badge>
							))}
						</div>
					),
			},
			{
				accessorKey: "active",
				meta: { label: "Status" },
				header: "Status",
				cell: ({ row }) => <ActiveBadge active={row.original.active} />,
				filterFn: (row, _id, value: string[]) =>
					value.length === 0 || value.includes(String(row.original.active)),
			},
			{
				id: "actions",
				header: () => <span className="sr-only">Ações</span>,
				cell: ({ row }) => {
					const c = row.original;
					return (
						<div className="flex justify-end">
							<DataTableRowActions label={`Ações para ${c.name}`}>
								<DropdownMenuItem onClick={() => onEdit(c)}>
									<Pencil />
									Editar
								</DropdownMenuItem>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									variant={c.active ? "destructive" : "default"}
									onClick={() =>
										onToggle({ id: c.id, name: c.name, activeNow: c.active })
									}
								>
									{c.active ? <PowerOff /> : <Power />}
									{c.active ? "Inativar" : "Reativar"}
								</DropdownMenuItem>
							</DataTableRowActions>
						</div>
					);
				},
				enableSorting: false,
				enableHiding: false,
			},
		],
		[onToggle, onEdit],
	);
}

function AdminCustomersPage() {
	const navigate = useNavigate();
	const { edit } = Route.useSearch();
	const [activeOnly, setActiveOnly] = useState(false);
	const { data, isLoading } = useQuery(adminCustomersListOptions(activeOnly));
	const deactivate = useDeactivateCustomer();
	const reactivate = useReactivateCustomer();
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((c) => c.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...adminCustomerDetailOptions(edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const openEdit = useCallback(
		(c: CustomerResponse) =>
			navigate({ to: "/admin/customers", search: { edit: c.id } }),
		[navigate],
	);
	const closeEdit = useCallback(
		() => navigate({ to: "/admin/customers", search: {}, replace: true }),
		[navigate],
	);

	const columns = useColumns(setConfirm, openEdit);

	return (
		<>
			<PageHeader
				title="Clientes"
				description="Lista, edita e ativa/desativa cadastros de clientes finais."
				actions={
					<label
						htmlFor="activeOnly"
						className="flex items-center gap-2 text-sm"
					>
						<Checkbox
							id="activeOnly"
							checked={activeOnly}
							onCheckedChange={(v) => setActiveOnly(v === true)}
							aria-label="Mostrar somente ativos"
						/>
						Somente ativos
					</label>
				}
			/>

			<DataTable
				columns={columns}
				data={data ?? []}
				isLoading={isLoading}
				searchPlaceholder="Buscar por nome ou e-mail…"
				emptyTitle="Nenhum cliente encontrado"
				emptyDescription={
					activeOnly
						? "Tente desmarcar 'Somente ativos' ou ajustar a busca."
						: "Os cadastros aparecerão aqui assim que clientes se inscreverem."
				}
			/>

			<Sheet
				open={edit !== undefined}
				onOpenChange={(open) => {
					if (!open) closeEdit();
				}}
			>
				<SheetContent side="right" className="flex flex-col gap-0 sm:max-w-md">
					<SheetHeader className="border-b">
						<SheetTitle>Editar cliente</SheetTitle>
						<SheetDescription>
							Atualize os dados cadastrais. O e-mail é imutável.
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">
						{editTarget ? (
							<CustomerEditForm
								customer={editTarget}
								onSaved={closeEdit}
								onCancel={closeEdit}
							/>
						) : detailQuery.isError ? (
							<p className="text-sm text-destructive">
								Não foi possível carregar este cliente.
							</p>
						) : (
							<div className="flex items-center justify-center py-12">
								<LoaderCircle
									aria-label="Carregando cliente"
									className="size-6 animate-spin text-muted-foreground"
								/>
							</div>
						)}
					</div>
				</SheetContent>
			</Sheet>

			<ConfirmDialog
				open={confirm !== null}
				onOpenChange={(open) => !open && setConfirm(null)}
				title={confirm?.activeNow ? "Inativar cliente?" : "Reativar cliente?"}
				description={
					confirm
						? confirm.activeNow
							? `O cliente "${confirm.name}" perderá o acesso até ser reativado. O histórico é preservado.`
							: `O cliente "${confirm.name}" voltará a ter acesso normal.`
						: ""
				}
				confirmLabel={confirm?.activeNow ? "Inativar" : "Reativar"}
				variant={confirm?.activeNow ? "destructive" : "default"}
				isPending={deactivate.isPending || reactivate.isPending}
				onConfirm={async () => {
					if (!confirm) return;
					try {
						const op = confirm.activeNow ? deactivate : reactivate;
						await op.mutateAsync(confirm.id);
						toast.success(
							confirm.activeNow ? "Cliente inativado." : "Cliente reativado.",
						);
					} catch (err) {
						toast.error(
							err instanceof ApiError
								? err.body.error
								: "Não foi possível concluir a operação.",
						);
					} finally {
						setConfirm(null);
					}
				}}
			/>
		</>
	);
}
