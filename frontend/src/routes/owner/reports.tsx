import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import { useState } from "react";
import { PageHeader } from "@/components/dashboard/page-header";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
} from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { formatCurrency, formatDate, toIsoDate } from "@/lib/formatters";
import { PERIOD_LABELS } from "@/lib/labels";
import { reportOptions } from "@/lib/queries/reports";
import { useSession } from "@/lib/session-hook";
import type { ReportPeriod } from "@/lib/types";

const PERIOD_KEYS = Object.keys(PERIOD_LABELS) as ReportPeriod[];

export const Route = createFileRoute("/owner/reports")({
	staticData: { crumb: "Relatórios" },
	component: OwnerReportsPage,
});

function OwnerReportsPage() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const [period, setPeriod] = useState<ReportPeriod>("DAILY");
	const [date, setDate] = useState(toIsoDate(new Date()));

	const { data, isLoading, isError } = useQuery(
		reportOptions(businessId, period, date),
	);

	return (
		<>
			<PageHeader
				title="Relatórios"
				description="Escolha um período e uma data de referência."
			/>

			<FieldGroup className="grid gap-3 sm:grid-cols-2 sm:max-w-md">
				<Field className="space-y-3">
					<FieldLabel htmlFor="period">Período</FieldLabel>
					<Select
						items={PERIOD_LABELS}
						value={period}
						onValueChange={(v) => setPeriod(v as ReportPeriod)}
					>
						<SelectTrigger id="period" className="w-full cursor-pointer">
							<SelectValue placeholder="Selecione um período" />
						</SelectTrigger>
						<SelectContent>
							{PERIOD_KEYS.map((p) => (
								<SelectItem key={p} value={p} className="cursor-pointer">
									<span>{PERIOD_LABELS[p]}</span>
								</SelectItem>
							))}
						</SelectContent>
					</Select>
				</Field>
				<Field className="space-y-3">
					<FieldLabel htmlFor="date">Data de referência</FieldLabel>
					<Input
						id="date"
						type="date"
						value={date}
						onChange={(e) => setDate(e.currentTarget.value)}
					/>
				</Field>
			</FieldGroup>

			{isLoading ? (
				<div className="flex items-center justify-center py-12">
					<LoaderCircle
						aria-label="Carregando relatório"
						className="size-6 animate-spin text-muted-foreground"
					/>
				</div>
			) : isError || !data ? (
				<Card className="border-dashed">
					<CardContent className="py-10 text-center text-sm text-muted-foreground">
						Não foi possível carregar o relatório.
					</CardContent>
				</Card>
			) : (
				<section className="flex flex-col gap-4">
					<div className="grid gap-3 sm:grid-cols-3">
						<Stat
							label="Agendamentos no período"
							value={data.appointmentCount.toString()}
						/>
						<Stat
							label="Faturamento"
							value={formatCurrency(data.totalRevenue)}
						/>
						<Stat
							label="Janela"
							value={`${formatDate(data.startDate)} – ${formatDate(data.endDate)}`}
						/>
					</div>

					<div>
						<p className="text-xs uppercase tracking-wider text-muted-foreground">
							Resumo gerado
						</p>
						<pre className="mt-1 whitespace-pre-wrap rounded-md border bg-card p-4 text-sm">
							{data.formatted}
						</pre>
					</div>
				</section>
			)}
		</>
	);
}

function Stat({ label, value }: Readonly<{ label: string; value: string }>) {
	return (
		<Card>
			<CardHeader className="pb-2">
				<CardDescription>{label}</CardDescription>
			</CardHeader>
			<CardContent>
				<p className="text-lg font-semibold">{value}</p>
			</CardContent>
		</Card>
	);
}
