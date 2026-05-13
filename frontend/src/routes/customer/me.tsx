import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import type { ColumnDef, Row } from "@tanstack/react-table";
import { CalendarRange, Search, Star, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import { AppointmentStatusBadge } from "@/components/dashboard/status-badge";
import {
	DataTable,
	DataTableColumnHeader,
	DataTableRowActions,
} from "@/components/dashboard/table/data-table";
import {
	DropdownMenuItem,
	DropdownMenuSeparator,
} from "@/components/dashboard/table/data-table-row-actions";
import { buttonVariants } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ApiError } from "@/lib/api";
import { formatCurrency, formatDateTime } from "@/lib/formatters";
import {
	myAppointmentsOptions,
	useCancelAppointment,
} from "@/lib/queries/appointments";
import type { AppointmentResponse, AppointmentStatus } from "@/lib/types";

const meSearchSchema = z.object({
	view: z.enum(["upcoming", "history"]).optional(),
});

type MeView = NonNullable<z.infer<typeof meSearchSchema>["view"]>;

export const Route = createFileRoute("/customer/me")({
	staticData: { crumb: "Início" },
	validateSearch: meSearchSchema,
	loader: ({ context }) =>
		context.queryClient.ensureQueryData(myAppointmentsOptions()),
	component: MyAppointmentsPage,
});

const ACTIVE_STATUS: ReadonlySet<AppointmentStatus> = new Set([
	"SCHEDULED",
	"CONFIRMED",
]);

function isUpcoming(a: AppointmentResponse, startOfToday: number): boolean {
	return (
		ACTIVE_STATUS.has(a.status) &&
		new Date(a.scheduledAt).getTime() >= startOfToday
	);
}

function useColumns(
	onCancel: (appt: AppointmentResponse) => void,
	onReview: (appt: AppointmentResponse) => void,
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
				accessorKey: "businessName",
				meta: { label: "Estabelecimento" },
				header: ({ column }) => (
					<DataTableColumnHeader column={column} title="Estabelecimento" />
				),
				cell: ({ row }) => (
					<span className="font-medium">{row.original.businessName}</span>
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
				cell: ({ row }: { row: Row<AppointmentResponse> }) => {
					const a = row.original;
					const canCancel = ACTIVE_STATUS.has(a.status);
					const canReview = a.status === "COMPLETED";
					if (!canCancel && !canReview) return null;
					return (
						<div className="flex justify-end">
							<DataTableRowActions
								label={`Ações do agendamento em ${a.businessName}`}
							>
								{canReview ? (
									<DropdownMenuItem onClick={() => onReview(a)}>
										<Star />
										Avaliar atendimento
									</DropdownMenuItem>
								) : null}
								{canCancel ? (
									<>
										{canReview ? <DropdownMenuSeparator /> : null}
										<DropdownMenuItem
											variant="destructive"
											onClick={() => onCancel(a)}
										>
											<Trash2 />
											Cancelar agendamento
										</DropdownMenuItem>
									</>
								) : null}
							</DataTableRowActions>
						</div>
					);
				},
				enableSorting: false,
				enableHiding: false,
			},
		],
		[onCancel, onReview],
	);
}

function MyAppointmentsPage() {
	const navigate = useNavigate();
	const view: MeView = Route.useSearch().view ?? "upcoming";
	const { data, isLoading } = useQuery(myAppointmentsOptions());
	const cancel = useCancelAppointment();
	const [target, setTarget] = useState<AppointmentResponse | null>(null);

	const { upcoming, history } = useMemo(() => {
		const appts = data ?? [];
		const startOfToday = new Date(new Date().toDateString()).getTime();
		const next: AppointmentResponse[] = [];
		const past: AppointmentResponse[] = [];
		for (const a of appts) {
			if (isUpcoming(a, startOfToday)) next.push(a);
			else past.push(a);
		}
		return { upcoming: next, history: past };
	}, [data]);

	const onReview = (a: AppointmentResponse) =>
		navigate({ to: "/customer/reviews", search: { create: a.id } });

	const columns = useColumns(setTarget, onReview);

	return (
		<>
			<PageHeader
				title="Meus agendamentos"
				description="Veja seus próximos atendimentos e o histórico."
				actions={
					<Link
						to="/businesses"
						className={buttonVariants({ variant: "outline", size: "sm" })}
					>
						<Search className="mr-1.5 size-4" />
						Explorar estabelecimentos
					</Link>
				}
			/>

			<section className="grid gap-3 sm:grid-cols-2">
				<Card>
					<CardHeader className="pb-2">
						<CardDescription>Próximos</CardDescription>
						<CardTitle className="text-2xl">{upcoming.length}</CardTitle>
					</CardHeader>
					<CardContent className="pt-0 text-xs text-muted-foreground">
						<CalendarRange className="inline size-3.5 align-text-bottom" /> Hoje
						em diante
					</CardContent>
				</Card>
				<Card>
					<CardHeader className="pb-2">
						<CardDescription>Histórico</CardDescription>
						<CardTitle className="text-2xl">{history.length}</CardTitle>
					</CardHeader>
					<CardContent className="pt-0 text-xs text-muted-foreground">
						Concluídos, cancelados e ausências
					</CardContent>
				</Card>
			</section>

			<Tabs
				value={view}
				onValueChange={(v) =>
					navigate({
						to: "/customer/me",
						search: { view: v as MeView },
						replace: true,
					})
				}
			>
				<TabsList>
					<TabsTrigger value="upcoming">
						Próximos ({upcoming.length})
					</TabsTrigger>
					<TabsTrigger value="history">
						Histórico ({history.length})
					</TabsTrigger>
				</TabsList>
				<TabsContent value="upcoming" className="mt-4">
					<DataTable
						columns={columns}
						data={upcoming}
						isLoading={isLoading}
						searchPlaceholder="Buscar por estabelecimento ou serviço…"
						emptyTitle="Nenhum agendamento futuro"
						emptyDescription="Quando você marcar um atendimento, ele aparecerá aqui."
					/>
				</TabsContent>
				<TabsContent value="history" className="mt-4">
					<DataTable
						columns={columns}
						data={history}
						isLoading={isLoading}
						searchPlaceholder="Buscar por estabelecimento ou serviço…"
						emptyTitle="Sem histórico ainda"
						emptyDescription="Agendamentos passados ficam visíveis aqui."
					/>
				</TabsContent>
			</Tabs>

			<ConfirmDialog
				open={target !== null}
				onOpenChange={(open) => {
					if (!open) setTarget(null);
				}}
				title="Cancelar agendamento?"
				description="Pode haver taxa conforme a política do estabelecimento."
				confirmLabel="Cancelar agendamento"
				cancelLabel="Voltar"
				variant="destructive"
				isPending={cancel.isPending}
				onConfirm={async () => {
					if (!target) return;
					try {
						const res = await cancel.mutateAsync({
							appointmentId: target.id,
							businessId: target.businessId,
						});
						toast.success(
							Number(res.fee) > 0
								? `Cancelado com taxa de ${formatCurrency(res.fee)}.`
								: res.reason || "Cancelado sem taxa.",
						);
					} catch (err) {
						toast.error(
							err instanceof ApiError
								? err.body.error
								: "Não foi possível cancelar.",
						);
					} finally {
						setTarget(null);
					}
				}}
			/>
		</>
	);
}
