import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import type { ColumnDef, Row } from "@tanstack/react-table";
import {
	LoaderCircle,
	Pencil,
	Power,
	PowerOff,
	ShieldCheck,
	UserPlus,
} from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import {
	AccessLevelBadge,
	ActiveBadge,
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
import { AdministratorCreateForm } from "@/components/form/administrator-create-form";
import { AdministratorEditForm } from "@/components/form/administrator-edit-form";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { ACCESS_LEVEL_LABELS } from "@/lib/labels";
import {
	adminAdministratorDetailOptions,
	adminAdministratorsListOptions,
	useDeactivateAdministrator,
	useReactivateAdministrator,
} from "@/lib/queries/admin/administrators";
import type { AccessLevel, AdministratorResponse } from "@/lib/types";

const adminSearchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
	create: z.coerce.boolean().optional(),
});

export const Route = createFileRoute("/admin/administrators")({
	staticData: { crumb: "Administradores" },
	validateSearch: adminSearchSchema,
	loader: ({ context }) =>
		context.queryClient.ensureQueryData(adminAdministratorsListOptions(false)),
	component: AdminAdministratorsPage,
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

const ACCESS_OPTIONS = (Object.keys(ACCESS_LEVEL_LABELS) as AccessLevel[]).map(
	(lvl) => ({
		value: lvl,
		label: ACCESS_LEVEL_LABELS[lvl],
		icon: ShieldCheck,
	}),
);

function useColumns(
	onToggle: (target: ConfirmTarget) => void,
	onEdit: (administrator: AdministratorResponse) => void,
): ColumnDef<AdministratorResponse>[] {
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
				accessorKey: "accessLevel",
				meta: { label: "Nível" },
				header: "Nível",
				cell: ({ row }) => (
					<AccessLevelBadge level={row.original.accessLevel} />
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
					const a = row.original;
					return (
						<div className="flex justify-end">
							<DataTableRowActions label={`Ações para ${a.name}`}>
								<DropdownMenuItem onClick={() => onEdit(a)}>
									<Pencil />
									Editar
								</DropdownMenuItem>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									variant={a.active ? "destructive" : "default"}
									onClick={() =>
										onToggle({ id: a.id, name: a.name, activeNow: a.active })
									}
								>
									{a.active ? <PowerOff /> : <Power />}
									{a.active ? "Inativar" : "Reativar"}
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

function AdminAdministratorsPage() {
	const navigate = useNavigate();
	const { edit, create } = Route.useSearch();
	const [activeOnly, setActiveOnly] = useState(false);
	const { data, isLoading } = useQuery(
		adminAdministratorsListOptions(activeOnly),
	);
	const deactivate = useDeactivateAdministrator();
	const reactivate = useReactivateAdministrator();
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((a) => a.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...adminAdministratorDetailOptions(edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const openEdit = useCallback(
		(a: AdministratorResponse) =>
			navigate({ to: "/admin/administrators", search: { edit: a.id } }),
		[navigate],
	);
	const openCreate = useCallback(
		() => navigate({ to: "/admin/administrators", search: { create: true } }),
		[navigate],
	);
	const close = useCallback(
		() => navigate({ to: "/admin/administrators", search: {}, replace: true }),
		[navigate],
	);

	const columns = useColumns(setConfirm, openEdit);

	const isCreating = create === true;
	const sheetOpen = isCreating || edit !== undefined;

	return (
		<>
			<PageHeader
				title="Administradores"
				description="Cria e gerencia administradores da plataforma e donos de estabelecimento."
				actions={
					<div className="flex flex-wrap items-center gap-3">
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
						<Button size="sm" onClick={openCreate} className="cursor-pointer">
							<UserPlus className="mr-1.5 size-4" />
							Novo administrador
						</Button>
					</div>
				}
			/>

			<DataTable
				columns={columns}
				data={data ?? []}
				isLoading={isLoading}
				searchPlaceholder="Buscar por nome ou e-mail…"
				filters={(table) => (
					<DataTableFilter
						column={table.getColumn("accessLevel")}
						title="Nível"
						options={ACCESS_OPTIONS}
					/>
				)}
				emptyTitle="Nenhum administrador encontrado"
				emptyDescription={
					activeOnly
						? "Tente desmarcar 'Somente ativos' ou ajustar a busca."
						: "Use o botão 'Novo administrador' para criar o primeiro."
				}
			/>

			<Sheet
				open={sheetOpen}
				onOpenChange={(open) => {
					if (!open) close();
				}}
			>
				<SheetContent side="right" className="flex flex-col gap-0 sm:max-w-md">
					<SheetHeader className="border-b">
						<SheetTitle>
							{isCreating ? "Novo administrador" : "Editar administrador"}
						</SheetTitle>
						<SheetDescription>
							{isCreating
								? "Crie um novo administrador da plataforma ou dono de estabelecimento."
								: "Atualize os dados ou redefina a senha. O e-mail é imutável."}
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">
						{isCreating ? (
							<AdministratorCreateForm onSaved={close} onCancel={close} />
						) : editTarget ? (
							<AdministratorEditForm
								administrator={editTarget}
								onSaved={close}
								onCancel={close}
							/>
						) : detailQuery.isError ? (
							<p className="text-sm text-destructive">
								Não foi possível carregar este administrador.
							</p>
						) : (
							<div className="flex items-center justify-center py-12">
								<LoaderCircle
									aria-label="Carregando administrador"
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
						? "Inativar administrador?"
						: "Reativar administrador?"
				}
				description={
					confirm
						? confirm.activeNow
							? `O administrador "${confirm.name}" perderá o acesso até ser reativado.`
							: `O administrador "${confirm.name}" voltará a ter acesso normal.`
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
								? "Administrador inativado."
								: "Administrador reativado.",
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
