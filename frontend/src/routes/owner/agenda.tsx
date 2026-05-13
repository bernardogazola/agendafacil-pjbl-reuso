import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import type { ColumnDef, Row } from "@tanstack/react-table";
import {
	CalendarRange,
	CheckCircle2,
	Clock,
	LoaderCircle,
	Pencil,
	UserX,
} from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";
import { PageHeader } from "@/components/dashboard/page-header";
import { AppointmentStatusBadge } from "@/components/dashboard/status-badge";
import {
	DataTable,
	DataTableColumnHeader,
	DataTableFilter,
	DataTableRowActions,
} from "@/components/dashboard/table/data-table";
import { DropdownMenuItem } from "@/components/dashboard/table/data-table-row-actions";
import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { formatCurrency, formatDateTime, toIsoDate } from "@/lib/formatters";
import { STATUS_LABELS } from "@/lib/labels";
import {
	businessAppointmentsOptions,
	useChangeAppointmentStatus,
	useRescheduleAppointment,
} from "@/lib/queries/appointments";
import { useSession } from "@/lib/session-hook";
import type {
	AppointmentResponse,
	AppointmentStatus,
	AppointmentStatusAction,
} from "@/lib/types";

export const Route = createFileRoute("/owner/agenda")({
	staticData: { crumb: "Agenda" },
	component: OwnerAgendaPage,
});

interface RescheduleTarget {
	id: number;
	scheduledAt: string;
}

const STATUS_OPTIONS = (Object.keys(STATUS_LABELS) as AppointmentStatus[]).map(
	(s) => ({
		value: s,
		label: STATUS_LABELS[s],
		icon: Clock,
	}),
);

const facetedFilterFn = <TData, T extends string>(
	row: Row<TData>,
	id: string,
	value: T[],
) => value.length === 0 || value.includes(row.getValue(id) as T);

function useColumns(
	onAction: (
		appt: AppointmentResponse,
		action: AppointmentStatusAction,
	) => void,
	onReschedule: (target: RescheduleTarget) => void,
): ColumnDef<AppointmentResponse>[] {
	return useMemo(
		() => [
			{
				accessorKey: "scheduledAt",
				meta: { label: "Quando" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Quando" />
				),
				cell: ({ row }) => formatDateTime(row.original.scheduledAt),
			},
			{
				accessorKey: "customerName",
				meta: { label: "Cliente" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Cliente" />
				),
				cell: ({ row }) => (
					<span className="font-medium">{row.original.customerName}</span>
				),
			},
			{
				accessorKey: "serviceName",
				meta: { label: "Serviço" },
				header: "Serviço",
				enableSorting: false,
			},
			{
				accessorKey: "status",
				meta: { label: "Status" },
				header: "Status",
				cell: ({ row }) => (
					<AppointmentStatusBadge status={row.original.status} />
				),
				filterFn: facetedFilterFn,
				enableSorting: false,
			},
			{
				accessorKey: "pricePaid",
				meta: { label: "Valor" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Valor" />
				),
				cell: ({ row }) => formatCurrency(row.original.pricePaid),
			},
			{
				id: "actions",
				header: () => <span className="sr-only">Ações</span>,
				cell: ({ row }) => {
					const a = row.original;
					const isScheduled = a.status === "SCHEDULED";
					const isConfirmed = a.status === "CONFIRMED";
					const canReschedule = isScheduled || isConfirmed;
					if (!isScheduled && !isConfirmed) return null;
					return (
						<div className="flex justify-end">
							<DataTableRowActions
								label={`Ações para agendamento de ${a.customerName}`}
							>
								{isScheduled ? (
									<DropdownMenuItem onClick={() => onAction(a, "CONFIRM")}>
										<CheckCircle2 />
										Confirmar
									</DropdownMenuItem>
								) : null}
								{isConfirmed ? (
									<DropdownMenuItem onClick={() => onAction(a, "COMPLETE")}>
										<CheckCircle2 />
										Concluir
									</DropdownMenuItem>
								) : null}
								{isConfirmed ? (
									<DropdownMenuItem onClick={() => onAction(a, "MARK_NO_SHOW")}>
										<UserX />
										Marcar não compareceu
									</DropdownMenuItem>
								) : null}
								{canReschedule ? (
									<DropdownMenuItem
										onClick={() =>
											onReschedule({ id: a.id, scheduledAt: a.scheduledAt })
										}
									>
										<Pencil />
										Reagendar
									</DropdownMenuItem>
								) : null}
							</DataTableRowActions>
						</div>
					);
				},
				enableSorting: false,
				enableHiding: false,
			},
		],
		[onAction, onReschedule],
	);
}

function OwnerAgendaPage() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;

	const today = new Date();
	const defaultFrom = toIsoDate(new Date(today.getTime() - 7 * 24 * 3600_000));
	const defaultTo = toIsoDate(new Date(today.getTime() + 14 * 24 * 3600_000));
	const [from, setFrom] = useState(defaultFrom);
	const [to, setTo] = useState(defaultTo);

	const { data, isLoading } = useQuery(
		businessAppointmentsOptions(businessId, from, to),
	);
	const changeStatus = useChangeAppointmentStatus();
	const reschedule = useRescheduleAppointment();

	const [target, setTarget] = useState<RescheduleTarget | null>(null);
	const [newDateTime, setNewDateTime] = useState("");

	const applyAction = useCallback(
		async (appt: AppointmentResponse, action: AppointmentStatusAction) => {
			try {
				await changeStatus.mutateAsync({
					appointmentId: appt.id,
					businessId,
					payload: { action },
				});
				toast.success("Status atualizado.");
			} catch (err) {
				toast.error(
					err instanceof ApiError
						? err.body.error
						: "Não foi possível atualizar o status.",
				);
			}
		},
		[changeStatus, businessId],
	);

	const openReschedule = useCallback((next: RescheduleTarget) => {
		setTarget(next);
		setNewDateTime(next.scheduledAt.slice(0, 16));
	}, []);

	const closeReschedule = useCallback(() => {
		setTarget(null);
		setNewDateTime("");
	}, []);

	const submitReschedule = async () => {
		if (!target) return;
		try {
			const iso = newDateTime.length === 16 ? `${newDateTime}:00` : newDateTime;
			await reschedule.mutateAsync({
				appointmentId: target.id,
				businessId,
				payload: { newScheduledAt: iso },
			});
			toast.success("Agendamento reagendado.");
			closeReschedule();
		} catch (err) {
			toast.error(
				err instanceof ApiError
					? err.body.error
					: "Não foi possível reagendar.",
			);
		}
	};

	const columns = useColumns(applyAction, openReschedule);

	return (
		<>
			<PageHeader
				title="Agenda"
				description="Consulte e atualize os agendamentos do seu estabelecimento."
				actions={
					<span className="inline-flex items-center gap-1.5 text-xs text-muted-foreground">
						<CalendarRange className="size-3.5" />
						{`${from} → ${to}`}
					</span>
				}
			/>

			<div className="flex flex-wrap items-end gap-3">
				<div className="flex flex-col gap-1.5">
					<Label htmlFor="from">De</Label>
					<Input
						id="from"
						type="date"
						value={from}
						onChange={(e) => setFrom(e.currentTarget.value)}
						className="w-44"
					/>
				</div>
				<div className="flex flex-col gap-1.5">
					<Label htmlFor="to">Até</Label>
					<Input
						id="to"
						type="date"
						value={to}
						onChange={(e) => setTo(e.currentTarget.value)}
						className="w-44"
					/>
				</div>
			</div>

			<DataTable
				columns={columns}
				data={data ?? []}
				isLoading={isLoading}
				searchPlaceholder="Buscar por cliente ou serviço…"
				filters={(table) => {
					const statusColumn = table.getColumn("status");
					if (!statusColumn) return null;
					return (
						<DataTableFilter
							column={statusColumn}
							title="Status"
							options={STATUS_OPTIONS}
						/>
					);
				}}
				emptyTitle="Nenhum agendamento no período"
				emptyDescription="Ajuste o intervalo acima ou aguarde novas reservas."
			/>

			<Sheet
				open={target !== null}
				onOpenChange={(open) => {
					if (!open) closeReschedule();
				}}
			>
				<SheetContent side="right" className="flex flex-col gap-0 sm:max-w-sm">
					<SheetHeader className="border-b">
						<SheetTitle>Reagendar</SheetTitle>
						<SheetDescription>
							Escolha um novo instante. O backend valida conflitos
							automaticamente.
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">
						<FieldGroup>
							<Field className="space-y-3">
								<FieldLabel htmlFor="reschedule-datetime">
									Novo horário
								</FieldLabel>
								<Input
									id="reschedule-datetime"
									type="datetime-local"
									value={newDateTime}
									onChange={(e) => setNewDateTime(e.currentTarget.value)}
								/>
							</Field>
						</FieldGroup>
					</div>
					<div className="flex items-center justify-end gap-3 border-t p-4">
						<Button
							variant="ghost"
							onClick={closeReschedule}
							disabled={reschedule.isPending}
							className="cursor-pointer"
						>
							Voltar
						</Button>
						<Button
							onClick={() => void submitReschedule()}
							disabled={!newDateTime || reschedule.isPending}
							className="cursor-pointer disabled:cursor-not-allowed"
						>
							{reschedule.isPending && (
								<LoaderCircle className="mr-2 size-4 animate-spin" />
							)}
							Confirmar
						</Button>
					</div>
				</SheetContent>
			</Sheet>
		</>
	);
}
