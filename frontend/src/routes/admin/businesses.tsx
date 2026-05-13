import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import type { ColumnDef, Row } from "@tanstack/react-table";
import {
	Building2,
	Layers,
	LoaderCircle,
	Pencil,
	Power,
	PowerOff,
	ShieldOff,
} from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import {
	ActiveBadge,
	CancellationPolicyBadge,
} from "@/components/dashboard/status-badge";
import {
	DataTable,
	DataTableColumnHeader,
	DataTableFilter,
	DataTableRowActions,
} from "@/components/dashboard/table/data-table";
import {
	DropdownMenuItem,
	DropdownMenuSeparator,
} from "@/components/dashboard/table/data-table-row-actions";
import { BusinessEditForm } from "@/components/form/business-edit-form";
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
import {
	BUSINESS_CATEGORY_LABELS,
	CANCELLATION_POLICY_LABELS,
	PLAN_LABELS,
} from "@/lib/labels";
import {
	adminBusinessDetailOptions,
	adminBusinessesListOptions,
	useAdminDeactivateBusiness,
	useAdminReactivateBusiness,
} from "@/lib/queries/admin/businesses";
import type {
	BusinessCategory,
	BusinessPlan,
	BusinessResponse,
	CancellationPolicyType,
} from "@/lib/types";

const editSearchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
});

export const Route = createFileRoute("/admin/businesses")({
	staticData: { crumb: "Estabelecimentos" },
	validateSearch: editSearchSchema,
	loader: ({ context }) =>
		context.queryClient.ensureQueryData(adminBusinessesListOptions(false)),
	component: AdminBusinessesPage,
});

interface ConfirmTarget {
	id: number;
	name: string;
	activeNow: boolean;
}

const facetedFilterFn = <TData, T extends string>(
	row: Row<TData>,
	id: string,
	value: T[],
) => value.length === 0 || value.includes(row.getValue(id) as T);

const CATEGORY_OPTIONS = (
	Object.keys(BUSINESS_CATEGORY_LABELS) as BusinessCategory[]
).map((c) => ({
	value: c,
	label: BUSINESS_CATEGORY_LABELS[c],
	icon: Building2,
}));

const PLAN_OPTIONS = (Object.keys(PLAN_LABELS) as BusinessPlan[]).map((p) => ({
	value: p,
	label: PLAN_LABELS[p],
	icon: Layers,
}));

const CANCELLATION_OPTIONS = (
	Object.keys(CANCELLATION_POLICY_LABELS) as CancellationPolicyType[]
).map((c) => ({
	value: c,
	label: CANCELLATION_POLICY_LABELS[c],
	icon: ShieldOff,
}));

function useColumns(
	onToggle: (target: ConfirmTarget) => void,
	onEdit: (business: BusinessResponse) => void,
): ColumnDef<BusinessResponse>[] {
	return useMemo(
		() => [
			{
				accessorKey: "tradeName",
				meta: { label: "Nome" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Nome" />
				),
				cell: ({ row }) => (
					<span className="font-medium">{row.original.tradeName}</span>
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
				accessorKey: "category",
				meta: { label: "Categoria" },
				header: "Categoria",
				cell: ({ row }) => (
					<Badge variant="secondary">
						{BUSINESS_CATEGORY_LABELS[row.original.category]}
					</Badge>
				),
				filterFn: facetedFilterFn,
				enableSorting: false,
			},
			{
				accessorKey: "plan",
				meta: { label: "Plano" },
				header: "Plano",
				cell: ({ row }) => (
					<Badge variant="outline">{PLAN_LABELS[row.original.plan]}</Badge>
				),
				filterFn: facetedFilterFn,
				enableSorting: false,
			},
			{
				accessorKey: "cancellationPolicyType",
				meta: { label: "Cancelamento" },
				header: "Cancelamento",
				cell: ({ row }) => (
					<CancellationPolicyBadge type={row.original.cancellationPolicyType} />
				),
				filterFn: facetedFilterFn,
				enableSorting: false,
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
					const b = row.original;
					return (
						<div className="flex justify-end">
							<DataTableRowActions label={`Ações para ${b.tradeName}`}>
								<DropdownMenuItem onClick={() => onEdit(b)}>
									<Pencil />
									Editar
								</DropdownMenuItem>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									variant={b.active ? "destructive" : "default"}
									onClick={() =>
										onToggle({
											id: b.id,
											name: b.tradeName,
											activeNow: b.active,
										})
									}
								>
									{b.active ? <PowerOff /> : <Power />}
									{b.active ? "Inativar" : "Reativar"}
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

function AdminBusinessesPage() {
	const navigate = useNavigate();
	const { edit } = Route.useSearch();
	const [activeOnly, setActiveOnly] = useState(false);
	const { data, isLoading } = useQuery(adminBusinessesListOptions(activeOnly));
	const deactivate = useAdminDeactivateBusiness();
	const reactivate = useAdminReactivateBusiness();
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((b) => b.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...adminBusinessDetailOptions(edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const openEdit = useCallback(
		(b: BusinessResponse) =>
			navigate({ to: "/admin/businesses", search: { edit: b.id } }),
		[navigate],
	);
	const closeEdit = useCallback(
		() => navigate({ to: "/admin/businesses", search: {}, replace: true }),
		[navigate],
	);

	const columns = useColumns(setConfirm, openEdit);

	return (
		<>
			<PageHeader
				title="Estabelecimentos"
				description="Lista todos os estabelecimentos cadastrados, edita seus dados e ativa/desativa o cadastro."
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
				filters={(table) => (
					<>
						<DataTableFilter
							column={table.getColumn("category")}
							title="Categoria"
							options={CATEGORY_OPTIONS}
						/>
						<DataTableFilter
							column={table.getColumn("plan")}
							title="Plano"
							options={PLAN_OPTIONS}
						/>
						<DataTableFilter
							column={table.getColumn("cancellationPolicyType")}
							title="Cancelamento"
							options={CANCELLATION_OPTIONS}
						/>
					</>
				)}
				emptyTitle="Nenhum estabelecimento encontrado"
				emptyDescription={
					activeOnly
						? "Tente desmarcar 'Somente ativos' ou ajustar a busca."
						: "Estabelecimentos cadastrados via signup aparecem aqui."
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
						<SheetTitle>Editar estabelecimento</SheetTitle>
						<SheetDescription>
							Atualize os dados cadastrais do estabelecimento.
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">
						{editTarget ? (
							<BusinessEditForm
								business={editTarget}
								onSaved={closeEdit}
								onCancel={closeEdit}
							/>
						) : detailQuery.isError ? (
							<p className="text-sm text-destructive">
								Não foi possível carregar este estabelecimento.
							</p>
						) : (
							<div className="flex items-center justify-center py-12">
								<LoaderCircle
									aria-label="Carregando estabelecimento"
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
				title={
					confirm?.activeNow
						? "Inativar estabelecimento?"
						: "Reativar estabelecimento?"
				}
				description={
					confirm
						? confirm.activeNow
							? `O estabelecimento "${confirm.name}" deixará de aparecer publicamente, mas o histórico é preservado.`
							: `O estabelecimento "${confirm.name}" voltará a aparecer publicamente.`
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
							confirm.activeNow
								? "Estabelecimento inativado."
								: "Estabelecimento reativado.",
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
