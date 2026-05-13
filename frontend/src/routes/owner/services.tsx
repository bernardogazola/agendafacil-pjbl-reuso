import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import type { ColumnDef } from "@tanstack/react-table";
import {
	Link2,
	LoaderCircle,
	Pencil,
	Plus,
	Power,
	PowerOff,
} from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import {
	ActiveBadge,
	PricingPolicyBadge,
} from "@/components/dashboard/status-badge";
import {
	DataTable,
	DataTableColumnHeader,
	DataTableRowActions,
} from "@/components/dashboard/table/data-table";
import {
	DropdownMenuItem,
	DropdownMenuSeparator,
} from "@/components/dashboard/table/data-table-row-actions";
import { ServiceCreateForm } from "@/components/form/service-create-form";
import { ServiceEditForm } from "@/components/form/service-edit-form";
import { Button } from "@/components/ui/button";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { formatCurrency } from "@/lib/formatters";
import { businessServicesOptions } from "@/lib/queries/businesses";
import {
	offeredServiceDetailOptions,
	useDeactivateService,
	useReactivateService,
} from "@/lib/queries/services";
import { useSession } from "@/lib/session-hook";
import type { OfferedServiceResponse } from "@/lib/types";

const searchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
	create: z.coerce.boolean().optional(),
});

export const Route = createFileRoute("/owner/services")({
	staticData: { crumb: "Serviços" },
	validateSearch: searchSchema,
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		return context.queryClient.ensureQueryData(businessServicesOptions(bid));
	},
	component: OwnerServicesPage,
});

interface ConfirmTarget {
	id: number;
	name: string;
	activeNow: boolean;
}

function useColumns(
	onCopyLink: (serviceId: number) => void,
	onEdit: (svc: OfferedServiceResponse) => void,
	onToggle: (target: ConfirmTarget) => void,
): ColumnDef<OfferedServiceResponse>[] {
	return useMemo(
		() => [
			{
				accessorKey: "name",
				meta: { label: "Serviço" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Serviço" />
				),
				cell: ({ row }) => (
					<div>
						<p className="font-medium">{row.original.name}</p>
						{row.original.description ? (
							<p className="text-xs text-muted-foreground">
								{row.original.description}
							</p>
						) : null}
					</div>
				),
			},
			{
				accessorKey: "durationMinutes",
				meta: { label: "Duração" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Duração" />
				),
				cell: ({ row }) => `${row.original.durationMinutes} min`,
			},
			{
				accessorKey: "pricingPolicyType",
				meta: { label: "Política" },
				header: "Política",
				cell: ({ row }) => (
					<PricingPolicyBadge type={row.original.pricingPolicyType} />
				),
				enableSorting: false,
			},
			{
				accessorKey: "basePrice",
				meta: { label: "Preço" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Preço" />
				),
				cell: ({ row }) => formatCurrency(row.original.basePrice),
			},
			{
				accessorKey: "active",
				meta: { label: "Status" },
				header: "Status",
				cell: ({ row }) => <ActiveBadge active={row.original.active} />,
			},
			{
				id: "actions",
				header: () => <span className="sr-only">Ações</span>,
				cell: ({ row }) => {
					const s = row.original;
					return (
						<div className="flex justify-end">
							<DataTableRowActions label={`Ações para ${s.name}`}>
								<DropdownMenuItem onClick={() => onCopyLink(s.id)}>
									<Link2 />
									Copiar link de booking
								</DropdownMenuItem>
								<DropdownMenuItem onClick={() => onEdit(s)}>
									<Pencil />
									Editar
								</DropdownMenuItem>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									variant={s.active ? "destructive" : "default"}
									onClick={() =>
										onToggle({ id: s.id, name: s.name, activeNow: s.active })
									}
								>
									{s.active ? <PowerOff /> : <Power />}
									{s.active ? "Inativar" : "Reativar"}
								</DropdownMenuItem>
							</DataTableRowActions>
						</div>
					);
				},
				enableSorting: false,
				enableHiding: false,
			},
		],
		[onCopyLink, onEdit, onToggle],
	);
}

function OwnerServicesPage() {
	const navigate = useNavigate();
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const { edit, create } = Route.useSearch();
	const { data, isLoading } = useQuery(businessServicesOptions(businessId));
	const deactivate = useDeactivateService(businessId);
	const reactivate = useReactivateService(businessId);
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((s) => s.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...offeredServiceDetailOptions(businessId, edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const openCreate = useCallback(
		() => navigate({ to: "/owner/services", search: { create: true } }),
		[navigate],
	);
	const openEdit = useCallback(
		(s: OfferedServiceResponse) =>
			navigate({ to: "/owner/services", search: { edit: s.id } }),
		[navigate],
	);
	const close = useCallback(
		() => navigate({ to: "/owner/services", search: {}, replace: true }),
		[navigate],
	);

	const copyLink = useCallback(
		(serviceId: number) => {
			const url = `${globalThis.location.origin}/customer/book/${businessId}/${serviceId}`;
			navigator.clipboard
				.writeText(url)
				.then(() => toast.success("Link copiado para a área de transferência."))
				.catch(() => toast.error(url));
		},
		[businessId],
	);

	const columns = useColumns(copyLink, openEdit, setConfirm);
	const isCreating = create === true;
	const sheetOpen = isCreating || edit !== undefined;

	const renderSheetBody = () => {
		if (isCreating) {
			return (
				<ServiceCreateForm
					businessId={businessId}
					onSaved={close}
					onCancel={close}
				/>
			);
		}
		if (editTarget) {
			return (
				<ServiceEditForm
					businessId={businessId}
					service={editTarget}
					onSaved={close}
					onCancel={close}
				/>
			);
		}
		if (detailQuery.isError) {
			return (
				<p className="text-sm text-destructive">
					Não foi possível carregar este serviço.
				</p>
			);
		}
		return (
			<div className="flex items-center justify-center py-12">
				<LoaderCircle
					aria-label="Carregando serviço"
					className="size-6 animate-spin text-muted-foreground"
				/>
			</div>
		);
	};

	return (
		<>
			<PageHeader
				title="Meus serviços"
				description="Cadastre os serviços que seu estabelecimento oferece."
				actions={
					<Button size="sm" onClick={openCreate} className="cursor-pointer">
						<Plus className="mr-1.5 size-4" />
						Novo serviço
					</Button>
				}
			/>

			<DataTable
				columns={columns}
				data={data ?? []}
				isLoading={isLoading}
				searchPlaceholder="Buscar por nome do serviço…"
				emptyTitle="Você ainda não cadastrou serviços"
				emptyDescription="Crie o primeiro para começar a receber agendamentos."
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
							{isCreating ? "Novo serviço" : "Editar serviço"}
						</SheetTitle>
						<SheetDescription>
							{isCreating
								? "Preencha os dados do novo serviço."
								: "Atualize as informações do serviço."}
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">{renderSheetBody()}</div>
				</SheetContent>
			</Sheet>

			<ConfirmDialog
				open={confirm !== null}
				onOpenChange={(open) => !open && setConfirm(null)}
				title={confirm?.activeNow ? "Inativar serviço?" : "Reativar serviço?"}
				description={
					confirm
						? confirm.activeNow
							? `O serviço "${confirm.name}" deixará de aparecer aos clientes. Agendamentos em curso não são afetados.`
							: `O serviço "${confirm.name}" voltará a ser ofertado.`
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
							confirm.activeNow ? "Serviço inativado." : "Serviço reativado.",
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
