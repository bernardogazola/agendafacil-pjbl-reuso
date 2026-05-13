import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { LoaderCircle, Plus, Save, Trash2 } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { ApiError } from "@/lib/api";
import { DAY_LABELS, DAY_ORDER } from "@/lib/labels";
import {
	businessHoursListOptions,
	useCreateHour,
	useDeleteHour,
	useUpdateHour,
} from "@/lib/queries/hours";
import { useSession } from "@/lib/session-hook";
import type { BusinessHoursResponse, DayOfWeek } from "@/lib/types";

export const Route = createFileRoute("/owner/hours")({
	staticData: { crumb: "Horários" },
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		return context.queryClient.ensureQueryData(businessHoursListOptions(bid));
	},
	component: HoursPage,
});

const normalizeTime = (t: string) => (t.length === 5 ? `${t}:00` : t);
const trimSeconds = (t: string) => (t.length >= 5 ? t.slice(0, 5) : t);

function HoursPage() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const { data, isLoading } = useQuery(businessHoursListOptions(businessId));
	const createHour = useCreateHour(businessId);
	const deleteHour = useDeleteHour(businessId);
	const [confirm, setConfirm] = useState<{ id: number; day: DayOfWeek } | null>(
		null,
	);

	if (isLoading || !data) {
		return (
			<>
				<PageHeader
					title="Horários de atendimento"
					description="Defina a janela de funcionamento por dia da semana."
				/>
				<div className="flex items-center justify-center py-12">
					<LoaderCircle
						aria-label="Carregando horários"
						className="size-6 animate-spin text-muted-foreground"
					/>
				</div>
			</>
		);
	}

	const byDay = new Map<DayOfWeek, BusinessHoursResponse>();
	for (const h of data) byDay.set(h.dayOfWeek, h);
	const present = DAY_ORDER.filter((d) => byDay.has(d));
	const missingDays = DAY_ORDER.filter((d) => !byDay.has(d));

	const addDay = async (day: DayOfWeek) => {
		try {
			await createHour.mutateAsync({
				dayOfWeek: day,
				startTime: "09:00:00",
				endTime: "18:00:00",
				active: true,
			});
			toast.success(`${DAY_LABELS[day]} adicionado.`);
		} catch (err) {
			toast.error(
				err instanceof ApiError
					? err.body.error
					: "Não foi possível adicionar o dia.",
			);
		}
	};

	return (
		<>
			<PageHeader
				title="Horários de atendimento"
				description="Cada dia tem sua própria janela. Edite, ative/desative ou remova individualmente."
			/>

			<div className="flex max-w-3xl flex-col gap-2">
				{present.length === 0 ? (
					<p className="rounded-md border border-dashed py-8 text-center text-sm text-muted-foreground">
						Nenhum horário cadastrado ainda. Adicione um dia abaixo.
					</p>
				) : null}
				{present.map((day) => {
					const hour = byDay.get(day);
					if (!hour) return null;
					return (
						<HourRow
							key={hour.id}
							hour={hour}
							businessId={businessId}
							onAskRemove={() => setConfirm({ id: hour.id, day })}
						/>
					);
				})}
			</div>

			{missingDays.length > 0 ? (
				<section className="mt-2 max-w-3xl rounded-lg border border-dashed p-4">
					<h2 className="text-sm font-semibold">Adicionar dia</h2>
					<p className="mt-1 text-xs text-muted-foreground">
						Os dias abaixo ainda não têm janela cadastrada.
					</p>
					<div className="mt-3 flex flex-wrap gap-2">
						{missingDays.map((day) => (
							<Button
								key={day}
								size="sm"
								variant="secondary"
								disabled={createHour.isPending}
								onClick={() => void addDay(day)}
								className="cursor-pointer"
							>
								<Plus className="mr-1.5 size-4" />
								{DAY_LABELS[day]}
							</Button>
						))}
					</div>
				</section>
			) : null}

			<ConfirmDialog
				open={confirm !== null}
				onOpenChange={(open) => {
					if (!open) setConfirm(null);
				}}
				title="Remover este dia?"
				description={
					confirm
						? `O dia ${DAY_LABELS[confirm.day]} deixará de aceitar agendamentos. Você pode adicionar de volta a qualquer momento.`
						: ""
				}
				confirmLabel="Remover"
				variant="destructive"
				isPending={deleteHour.isPending}
				onConfirm={async () => {
					if (!confirm) return;
					try {
						await deleteHour.mutateAsync(confirm.id);
						toast.success("Dia removido.");
					} catch (err) {
						toast.error(
							err instanceof ApiError
								? err.body.error
								: "Não foi possível remover.",
						);
					} finally {
						setConfirm(null);
					}
				}}
			/>
		</>
	);
}

interface HourRowProps {
	hour: BusinessHoursResponse;
	businessId: number;
	onAskRemove: () => void;
}

function HourRow({ hour, businessId, onAskRemove }: Readonly<HourRowProps>) {
	const update = useUpdateHour(businessId, hour.id);
	const [active, setActive] = useState(hour.active);
	const [startTime, setStartTime] = useState(trimSeconds(hour.startTime));
	const [endTime, setEndTime] = useState(trimSeconds(hour.endTime));

	const dirty =
		active !== hour.active ||
		startTime !== trimSeconds(hour.startTime) ||
		endTime !== trimSeconds(hour.endTime);

	const onSave = async () => {
		try {
			await update.mutateAsync({
				startTime: normalizeTime(startTime),
				endTime: normalizeTime(endTime),
				active,
			});
			toast.success(`${DAY_LABELS[hour.dayOfWeek]} atualizado.`);
		} catch (err) {
			toast.error(
				err instanceof ApiError
					? err.body.error
					: "Não foi possível salvar o horário.",
			);
		}
	};

	const timeClass =
		"h-9 rounded-md border bg-background px-2 text-sm shadow-xs disabled:cursor-not-allowed disabled:bg-muted";

	return (
		<div className="grid items-center gap-2 rounded-md border bg-card p-3 sm:grid-cols-[160px_1fr_auto_1fr_auto_auto]">
			<div className="flex items-center gap-2">
				<Checkbox
					id={`active-${hour.id}`}
					checked={active}
					onCheckedChange={(v) => setActive(v === true)}
					aria-label={`Ativar ${DAY_LABELS[hour.dayOfWeek]}`}
				/>
				<label htmlFor={`active-${hour.id}`} className="text-sm font-medium">
					{DAY_LABELS[hour.dayOfWeek]}
				</label>
			</div>
			<input
				type="time"
				value={startTime}
				disabled={!active}
				onChange={(e) => setStartTime(e.currentTarget.value)}
				className={timeClass}
				aria-label={`Início ${DAY_LABELS[hour.dayOfWeek]}`}
			/>
			<span className="text-sm text-muted-foreground">até</span>
			<input
				type="time"
				value={endTime}
				disabled={!active}
				onChange={(e) => setEndTime(e.currentTarget.value)}
				className={timeClass}
				aria-label={`Fim ${DAY_LABELS[hour.dayOfWeek]}`}
			/>
			<Button
				size="sm"
				disabled={!dirty || update.isPending}
				onClick={() => void onSave()}
				className="cursor-pointer disabled:cursor-not-allowed"
			>
				{update.isPending ? (
					<LoaderCircle className="mr-1.5 size-4 animate-spin" />
				) : (
					<Save className="mr-1.5 size-4" />
				)}
				Salvar
			</Button>
			<Button
				size="sm"
				variant="ghost"
				onClick={onAskRemove}
				className="cursor-pointer text-destructive hover:text-destructive"
				aria-label={`Remover ${DAY_LABELS[hour.dayOfWeek]}`}
			>
				<Trash2 className="mr-1.5 size-4" />
				Remover
			</Button>
		</div>
	);
}
