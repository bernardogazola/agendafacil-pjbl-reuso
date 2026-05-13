import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import type { ColumnDef } from "@tanstack/react-table";
import { LoaderCircle, Pencil, Plus, Power, PowerOff } from "lucide-react";
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
import { PromotionCreateForm } from "@/components/form/promotion-create-form";
import { PromotionEditForm } from "@/components/form/promotion-edit-form";
import { Button } from "@/components/ui/button";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/formatters";
import { businessServicesOptions } from "@/lib/queries/businesses";
import {
	promotionDetailOptions,
	promotionsListOptions,
	useDeactivatePromotion,
	useReactivatePromotion,
} from "@/lib/queries/promotions";
import { useSession } from "@/lib/session-hook";
import type { PromotionResponse } from "@/lib/types";

const searchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
	create: z.coerce.boolean().optional(),
});

export const Route = createFileRoute("/owner/promotions")({
	staticData: { crumb: "Promoções" },
	validateSearch: searchSchema,
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		return Promise.all([
			context.queryClient.ensureQueryData(promotionsListOptions(bid)),
			context.queryClient.ensureQueryData(businessServicesOptions(bid)),
		]);
	},
	component: OwnerPromotionsPage,
});

interface ConfirmTarget {
	id: number;
	name: string;
	activeNow: boolean;
}

function discountLabel(p: PromotionResponse): string {
	if (p.discountPercentage != null) return `${p.discountPercentage}%`;
	if (p.discountAmount != null) return formatCurrency(p.discountAmount);
	return "—";
}

function useColumns(
	onEdit: (p: PromotionResponse) => void,
	onToggle: (target: ConfirmTarget) => void,
): ColumnDef<PromotionResponse>[] {
	return useMemo(
		() => [
			{
				accessorKey: "name",
				meta: { label: "Nome" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Nome" />
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
				id: "period",
				meta: { label: "Vigência" },
				header: "Vigência",
				accessorFn: (row) => row.validFrom,
				cell: ({ row }) =>
					`${formatDate(row.original.validFrom)} – ${formatDate(row.original.validTo)}`,
			},
			{
				id: "discount",
				meta: { label: "Desconto" },
				header: "Desconto",
				cell: ({ row }) => discountLabel(row.original),
				enableSorting: false,
			},
			{
				id: "services",
				meta: { label: "Serviços" },
				header: "Serviços",
				cell: ({ row }) =>
					row.original.eligibleServiceIds.length === 0
						? "Todos"
						: `${row.original.eligibleServiceIds.length} serviço(s)`,
				enableSorting: false,
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
					const p = row.original;
					return (
						<div className="flex justify-end">
							<DataTableRowActions label={`Ações para ${p.name}`}>
								<DropdownMenuItem onClick={() => onEdit(p)}>
									<Pencil />
									Editar
								</DropdownMenuItem>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									variant={p.active ? "destructive" : "default"}
									onClick={() =>
										onToggle({ id: p.id, name: p.name, activeNow: p.active })
									}
								>
									{p.active ? <PowerOff /> : <Power />}
									{p.active ? "Inativar" : "Reativar"}
								</DropdownMenuItem>
							</DataTableRowActions>
						</div>
					);
				},
				enableSorting: false,
				enableHiding: false,
			},
		],
		[onEdit, onToggle],
	);
}

function OwnerPromotionsPage() {
	const navigate = useNavigate();
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const { edit, create } = Route.useSearch();
	const { data, isLoading } = useQuery(promotionsListOptions(businessId));
	const deactivate = useDeactivatePromotion(businessId);
	const reactivate = useReactivatePromotion(businessId);
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((p) => p.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...promotionDetailOptions(businessId, edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const openCreate = useCallback(
		() => navigate({ to: "/owner/promotions", search: { create: true } }),
		[navigate],
	);
	const openEdit = useCallback(
		(p: PromotionResponse) =>
			navigate({ to: "/owner/promotions", search: { edit: p.id } }),
		[navigate],
	);
	const close = useCallback(
		() => navigate({ to: "/owner/promotions", search: {}, replace: true }),
		[navigate],
	);

	const columns = useColumns(openEdit, setConfirm);
	const isCreating = create === true;
	const sheetOpen = isCreating || edit !== undefined;

	const renderSheetBody = () => {
		if (isCreating) {
			return (
				<PromotionCreateForm
					businessId={businessId}
					onSaved={close}
					onCancel={close}
				/>
			);
		}
		if (editTarget) {
			return (
				<PromotionEditForm
					businessId={businessId}
					promotion={editTarget}
					onSaved={close}
					onCancel={close}
				/>
			);
		}
		if (detailQuery.isError) {
			return (
				<p className="text-sm text-destructive">
					Não foi possível carregar esta promoção.
				</p>
			);
		}
		return (
			<div className="flex items-center justify-center py-12">
				<LoaderCircle
					aria-label="Carregando promoção"
					className="size-6 animate-spin text-muted-foreground"
				/>
			</div>
		);
	};

	return (
		<>
			<PageHeader
				title="Promoções"
				description="Cadastre campanhas com descontos por janela de datas. Use percentual ou valor fixo, nunca os dois."
				actions={
					<Button size="sm" onClick={openCreate} className="cursor-pointer">
						<Plus className="mr-1.5 size-4" />
						Nova promoção
					</Button>
				}
			/>

			<DataTable
				columns={columns}
				data={data ?? []}
				isLoading={isLoading}
				searchPlaceholder="Buscar por nome da promoção…"
				emptyTitle="Nenhuma promoção cadastrada"
				emptyDescription="Crie sua primeira campanha para atrair clientes."
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
							{isCreating ? "Nova promoção" : "Editar promoção"}
						</SheetTitle>
						<SheetDescription>
							{isCreating
								? "Configure uma campanha promocional."
								: "Atualize os dados da campanha."}
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">{renderSheetBody()}</div>
				</SheetContent>
			</Sheet>

			<ConfirmDialog
				open={confirm !== null}
				onOpenChange={(open) => !open && setConfirm(null)}
				title={confirm?.activeNow ? "Inativar promoção?" : "Reativar promoção?"}
				description={
					confirm
						? confirm.activeNow
							? `A promoção "${confirm.name}" deixará de ser aplicada.`
							: `A promoção "${confirm.name}" voltará a ser aplicada.`
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
							confirm.activeNow ? "Promoção inativada." : "Promoção reativada.",
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
