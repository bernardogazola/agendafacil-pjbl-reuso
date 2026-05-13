import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { LoaderCircle, Star } from "lucide-react";
import { PageHeader } from "@/components/dashboard/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { businessReviewsListOptions } from "@/lib/queries/reviews";
import { useSession } from "@/lib/session-hook";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/owner/reviews")({
	staticData: { crumb: "Avaliações" },
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		return context.queryClient.ensureQueryData(businessReviewsListOptions(bid));
	},
	component: OwnerReviewsPage,
});

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

function OwnerReviewsPage() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const { data, isLoading } = useQuery(businessReviewsListOptions(businessId));
	const reviews = data ?? [];

	return (
		<>
			<PageHeader
				title="Avaliações"
				description="Avaliações deixadas por clientes em atendimentos concluídos."
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
						<p className="text-sm font-medium">Nenhuma avaliação ainda</p>
						<p className="text-sm text-muted-foreground">
							Quando um cliente avaliar um atendimento, aparece aqui.
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
												{r.customerName}
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
										<p className="mt-2 text-sm italic text-muted-foreground">
											Sem comentário
										</p>
									)}
								</div>
							</CardContent>
						</Card>
					))}
				</div>
			)}
		</>
	);
}
