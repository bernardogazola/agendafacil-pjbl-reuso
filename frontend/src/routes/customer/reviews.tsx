import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { LoaderCircle, Pencil, Star, Trash2 } from "lucide-react";
import { useCallback, useState } from "react";
import { toast } from "sonner";
import { z } from "zod";
import { ConfirmDialog } from "@/components/dashboard/confirm-dialog";
import { PageHeader } from "@/components/dashboard/page-header";
import { ReviewCreateForm } from "@/components/form/review-create-form";
import { ReviewEditForm } from "@/components/form/review-edit-form";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import { ApiError } from "@/lib/api";
import { myAppointmentsOptions } from "@/lib/queries/appointments";
import {
	myReviewsListOptions,
	reviewDetailOptions,
	useDeleteReview,
} from "@/lib/queries/reviews";
import type { ReviewResponse } from "@/lib/types";
import { cn } from "@/lib/utils";

const reviewsSearchSchema = z.object({
	edit: z.coerce.number().int().positive().optional(),
	create: z.coerce.number().int().positive().optional(),
});

export const Route = createFileRoute("/customer/reviews")({
	staticData: { crumb: "Minhas avaliações" },
	validateSearch: reviewsSearchSchema,
	loader: ({ context }) =>
		Promise.all([
			context.queryClient.ensureQueryData(myReviewsListOptions()),
			context.queryClient.ensureQueryData(myAppointmentsOptions()),
		]),
	component: MyReviewsPage,
});

interface ConfirmTarget {
	id: number;
	business: string;
}

function StarRow({ rating }: Readonly<{ rating: number }>) {
	return (
		<div
			role="img"
			className="flex gap-0.5 text-amber-500"
			aria-label={`Nota ${rating} de 5`}
		>
			{[1, 2, 3, 4, 5].map((n) => (
				<Star
					key={n}
					className={cn(
						"size-4",
						n <= rating ? "fill-current" : "fill-transparent text-muted",
					)}
				/>
			))}
		</div>
	);
}

function MyReviewsPage() {
	const navigate = useNavigate();
	const { edit, create } = Route.useSearch();
	const { data, isLoading } = useQuery(myReviewsListOptions());
	const apptsQuery = useQuery(myAppointmentsOptions());
	const remove = useDeleteReview();
	const [confirm, setConfirm] = useState<ConfirmTarget | null>(null);

	const fromList = edit ? (data?.find((r) => r.id === edit) ?? null) : null;
	const detailQuery = useQuery({
		...reviewDetailOptions(edit ?? 0),
		enabled: edit !== undefined && fromList === null,
	});
	const editTarget = fromList ?? detailQuery.data ?? null;

	const createTarget = create
		? (apptsQuery.data?.find((a) => a.id === create) ?? null)
		: null;

	const openEdit = useCallback(
		(r: ReviewResponse) =>
			navigate({ to: "/customer/reviews", search: { edit: r.id } }),
		[navigate],
	);
	const close = useCallback(
		() => navigate({ to: "/customer/reviews", search: {}, replace: true }),
		[navigate],
	);

	const isCreating = create !== undefined;
	const sheetOpen = isCreating || edit !== undefined;

	const renderSheetBody = () => {
		if (isCreating) {
			if (createTarget) {
				return (
					<ReviewCreateForm
						appointment={createTarget}
						onSaved={close}
						onCancel={close}
					/>
				);
			}
			if (apptsQuery.isError) {
				return (
					<p className="text-sm text-destructive">
						Não foi possível carregar este agendamento.
					</p>
				);
			}
			return (
				<div className="flex items-center justify-center py-12">
					<LoaderCircle
						aria-label="Carregando agendamento"
						className="size-6 animate-spin text-muted-foreground"
					/>
				</div>
			);
		}
		if (editTarget) {
			return (
				<ReviewEditForm review={editTarget} onSaved={close} onCancel={close} />
			);
		}
		if (detailQuery.isError) {
			return (
				<p className="text-sm text-destructive">
					Não foi possível carregar esta avaliação.
				</p>
			);
		}
		return (
			<div className="flex items-center justify-center py-12">
				<LoaderCircle
					aria-label="Carregando avaliação"
					className="size-6 animate-spin text-muted-foreground"
				/>
			</div>
		);
	};

	const reviews = data ?? [];

	return (
		<>
			<PageHeader
				title="Minhas avaliações"
				description="Gerencie as avaliações que você deixou em atendimentos concluídos."
			/>

			{isLoading ? (
				<div className="flex items-center justify-center py-12">
					<LoaderCircle
						aria-label="Carregando avaliações"
						className="size-6 animate-spin text-muted-foreground"
					/>
				</div>
			) : reviews.length === 0 ? (
				<Card className="border-dashed">
					<CardContent className="flex flex-col items-center gap-2 py-10 text-center">
						<Star className="size-8 text-muted-foreground" />
						<p className="text-sm font-medium">
							Você ainda não avaliou nenhum atendimento
						</p>
						<p className="text-sm text-muted-foreground">
							Após concluir um atendimento, avalie a partir de "Meus
							agendamentos".
						</p>
					</CardContent>
				</Card>
			) : (
				<div className="grid gap-3">
					{reviews.map((r) => (
						<Card key={r.id}>
							<CardContent className="flex flex-col gap-3 p-4 sm:flex-row sm:items-start sm:justify-between">
								<div className="min-w-0 flex-1">
									<div className="flex items-start justify-between gap-3">
										<div className="min-w-0">
											<p className="truncate text-sm font-semibold">
												{r.businessName}
											</p>
											<p className="text-xs text-muted-foreground">
												{r.serviceName}
											</p>
										</div>
										<StarRow rating={r.rating} />
									</div>
									{r.comment ? (
										<p className="mt-2 text-sm text-foreground/80">
											{r.comment}
										</p>
									) : (
										<p className="mt-2 text-sm text-muted-foreground italic">
											Sem comentário
										</p>
									)}
								</div>
								<div className="flex shrink-0 gap-2 sm:flex-col">
									<Button
										size="sm"
										variant="outline"
										onClick={() => openEdit(r)}
										className="cursor-pointer"
									>
										<Pencil className="mr-1.5 size-4" />
										Editar
									</Button>
									<Button
										size="sm"
										variant="ghost"
										onClick={() =>
											setConfirm({ id: r.id, business: r.businessName })
										}
										className="cursor-pointer text-destructive hover:text-destructive"
									>
										<Trash2 className="mr-1.5 size-4" />
										Excluir
									</Button>
								</div>
							</CardContent>
						</Card>
					))}
				</div>
			)}

			<Sheet
				open={sheetOpen}
				onOpenChange={(open) => {
					if (!open) close();
				}}
			>
				<SheetContent side="right" className="flex flex-col gap-0 sm:max-w-md">
					<SheetHeader className="border-b">
						<SheetTitle>
							{isCreating ? "Avaliar atendimento" : "Editar avaliação"}
						</SheetTitle>
						<SheetDescription>
							{isCreating
								? "Dê uma nota e, se quiser, escreva um comentário."
								: "Atualize a nota ou o comentário desta avaliação."}
						</SheetDescription>
					</SheetHeader>
					<div className="flex-1 overflow-y-auto p-4">{renderSheetBody()}</div>
				</SheetContent>
			</Sheet>

			<ConfirmDialog
				open={confirm !== null}
				onOpenChange={(open) => {
					if (!open) setConfirm(null);
				}}
				title="Excluir avaliação?"
				description={
					confirm
						? `Sua avaliação para "${confirm.business}" será removida.`
						: ""
				}
				confirmLabel="Excluir"
				cancelLabel="Voltar"
				variant="destructive"
				isPending={remove.isPending}
				onConfirm={async () => {
					if (!confirm) return;
					try {
						await remove.mutateAsync(confirm.id);
						toast.success("Avaliação removida.");
					} catch (err) {
						toast.error(
							err instanceof ApiError
								? err.body.error
								: "Não foi possível remover a avaliação.",
						);
					} finally {
						setConfirm(null);
					}
				}}
			/>
		</>
	);
}
