import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import {
	CalendarRange,
	CheckCircle2,
	DollarSign,
	LoaderCircle,
	XCircle,
} from "lucide-react";
import { PageHeader } from "@/components/dashboard/page-header";
import { AppointmentStatusBadge } from "@/components/dashboard/status-badge";
import { buttonVariants } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { formatCurrency, formatDateTime, toIsoDate } from "@/lib/formatters";
import { businessAppointmentsOptions } from "@/lib/queries/appointments";
import { useSession } from "@/lib/session-hook";

export const Route = createFileRoute("/owner/")({
	staticData: { crumb: "Painel" },
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		const today = toIsoDate(new Date());
		return context.queryClient.ensureQueryData(
			businessAppointmentsOptions(bid, today, today),
		);
	},
	component: OwnerDashboard,
});

function OwnerDashboard() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const today = toIsoDate(new Date());
	const { data, isLoading } = useQuery(
		businessAppointmentsOptions(businessId, today, today),
	);
	const todays = data ?? [];

	const confirmed = todays.filter((a) => a.status === "CONFIRMED").length;
	const canceled = todays.filter((a) => a.status === "CANCELED").length;
	const revenue = todays
		.filter((a) => a.status !== "CANCELED")
		.reduce((acc, a) => acc + Number(a.pricePaid), 0);

	const firstName = session?.name?.split(" ")[0];

	return (
		<>
			<PageHeader
				title={firstName ? `Olá, ${firstName}` : "Olá"}
				description="Resumo dos atendimentos de hoje no seu estabelecimento."
				actions={
					<Link
						to="/owner/agenda"
						className={buttonVariants({ variant: "outline", size: "sm" })}
					>
						<CalendarRange className="mr-1.5 size-4" />
						Ver agenda completa
					</Link>
				}
			/>

			<section className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
				<StatCard
					icon={<CalendarRange className="size-4" />}
					label="Agendamentos hoje"
					value={todays.length.toString()}
				/>
				<StatCard
					icon={<CheckCircle2 className="size-4" />}
					label="Confirmados"
					value={confirmed.toString()}
				/>
				<StatCard
					icon={<XCircle className="size-4" />}
					label="Cancelados"
					value={canceled.toString()}
				/>
				<StatCard
					icon={<DollarSign className="size-4" />}
					label="Faturamento previsto"
					value={formatCurrency(revenue)}
				/>
			</section>

			<Card>
				<CardHeader>
					<CardTitle>Agenda de hoje</CardTitle>
					<CardDescription>
						{todays.length === 0
							? "Nenhum agendamento marcado para hoje."
							: `${todays.length} ${
									todays.length === 1 ? "agendamento" : "agendamentos"
								} no total.`}
					</CardDescription>
				</CardHeader>
				<CardContent>
					{isLoading ? (
						<div className="flex items-center justify-center py-6">
							<LoaderCircle
								aria-label="Carregando agenda"
								className="size-6 animate-spin text-muted-foreground"
							/>
						</div>
					) : todays.length === 0 ? (
						<p className="py-6 text-center text-sm text-muted-foreground">
							Compartilhe os links dos seus serviços para receber agendamentos.
						</p>
					) : (
						<Table>
							<TableHeader>
								<TableRow>
									<TableHead>Quando</TableHead>
									<TableHead>Cliente</TableHead>
									<TableHead>Serviço</TableHead>
									<TableHead>Status</TableHead>
									<TableHead className="text-right">Valor</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{todays.map((a) => (
									<TableRow key={a.id}>
										<TableCell>{formatDateTime(a.scheduledAt)}</TableCell>
										<TableCell className="font-medium">
											{a.customerName}
										</TableCell>
										<TableCell>{a.serviceName}</TableCell>
										<TableCell>
											<AppointmentStatusBadge status={a.status} />
										</TableCell>
										<TableCell className="text-right">
											{formatCurrency(a.pricePaid)}
										</TableCell>
									</TableRow>
								))}
							</TableBody>
						</Table>
					)}
				</CardContent>
			</Card>
		</>
	);
}

interface StatCardProps {
	icon: React.ReactNode;
	label: string;
	value: string;
}

function StatCard({ icon, label, value }: Readonly<StatCardProps>) {
	return (
		<Card>
			<CardHeader className="pb-2">
				<div className="flex items-center justify-between text-muted-foreground">
					<CardDescription>{label}</CardDescription>
					<span aria-hidden>{icon}</span>
				</div>
			</CardHeader>
			<CardContent>
				<p className="text-2xl font-semibold tracking-tight">{value}</p>
			</CardContent>
		</Card>
	);
}
