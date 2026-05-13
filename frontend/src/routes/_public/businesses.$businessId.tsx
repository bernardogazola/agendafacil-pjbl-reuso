import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import { CalendarCheck2, LoaderCircle, LogIn, Mail, Phone } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { formatCurrency } from "@/lib/formatters";
import {
	BUSINESS_CATEGORY_LABELS,
	PLAN_LABELS,
	PRICING_POLICY_LABELS,
} from "@/lib/labels";
import {
	businessesListOptions,
	businessServicesOptions,
} from "@/lib/queries/businesses";
import { useSession } from "@/lib/session-hook";

export const Route = createFileRoute("/_public/businesses/$businessId")({
	staticData: { crumb: "Detalhe" },
	parseParams: ({ businessId }) => ({ businessId: Number(businessId) }),
	loader: ({ context, params }) =>
		Promise.all([
			context.queryClient.ensureQueryData(
				businessServicesOptions(params.businessId),
			),
			context.queryClient.ensureQueryData(businessesListOptions()),
		]),
	component: BusinessDetailPage,
});

function BusinessDetailPage() {
	const { businessId } = Route.useParams();
	const servicesQ = useQuery(businessServicesOptions(businessId));
	const businessesQ = useQuery(businessesListOptions());
	const session = useSession();

	if (servicesQ.isLoading || businessesQ.isLoading) {
		return (
			<div className="flex items-center justify-center py-12">
				<LoaderCircle
					aria-label="Carregando estabelecimento"
					className="size-6 animate-spin text-muted-foreground"
				/>
			</div>
		);
	}

	const biz = businessesQ.data?.find((b) => b.id === businessId);
	if (!biz) {
		return (
			<Card className="border-dashed">
				<CardContent className="flex flex-col items-center gap-2 py-10 text-center">
					<p className="text-sm font-medium">Estabelecimento não encontrado</p>
					<p className="text-sm text-muted-foreground">
						Ele pode estar inativo ou ter sido removido.
					</p>
					<Link
						to="/businesses"
						className={buttonVariants({ variant: "outline", size: "sm" })}
					>
						Voltar para a lista
					</Link>
				</CardContent>
			</Card>
		);
	}

	const services = servicesQ.data ?? [];
	const isCustomer = session?.role === "customer";

	return (
		<div className="flex flex-col gap-8">
			<header className="flex flex-col gap-3">
				<p className="text-xs uppercase tracking-wider text-muted-foreground">
					{BUSINESS_CATEGORY_LABELS[biz.category]}
				</p>
				<div className="flex flex-wrap items-start justify-between gap-3">
					<h1 className="text-2xl font-semibold tracking-tight">
						{biz.tradeName}
					</h1>
					<Badge variant="outline">Plano {PLAN_LABELS[biz.plan]}</Badge>
				</div>
				<div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
					<span className="inline-flex items-center gap-1.5">
						<Mail className="size-3.5" />
						{biz.email}
					</span>
					{biz.phone ? (
						<span className="inline-flex items-center gap-1.5">
							<Phone className="size-3.5" />
							{biz.phone}
						</span>
					) : null}
				</div>
			</header>

			<section className="flex flex-col gap-3">
				<h2 className="text-lg font-semibold">Serviços</h2>
				{services.length === 0 ? (
					<Card className="border-dashed">
						<CardContent className="py-8 text-center text-sm text-muted-foreground">
							Este estabelecimento ainda não cadastrou serviços.
						</CardContent>
					</Card>
				) : (
					<ul className="flex flex-col gap-3">
						{services.map((s) => (
							<li key={s.id}>
								<Card>
									<CardHeader className="pb-2">
										<div className="flex flex-wrap items-start justify-between gap-3">
											<div className="min-w-0">
												<CardTitle className="text-base">{s.name}</CardTitle>
												{s.description ? (
													<CardDescription className="mt-1">
														{s.description}
													</CardDescription>
												) : null}
											</div>
											<span className="shrink-0 text-base font-semibold text-primary">
												{formatCurrency(s.basePrice)}
											</span>
										</div>
									</CardHeader>
									<CardContent className="flex flex-wrap items-center justify-between gap-3 pt-0">
										<p className="text-xs text-muted-foreground">
											{s.durationMinutes} min ·{" "}
											{PRICING_POLICY_LABELS[s.pricingPolicyType]}
										</p>
										{isCustomer ? (
											<Link
												to="/customer/book/$businessId/$serviceId"
												params={{
													businessId,
													serviceId: s.id,
												}}
												className={buttonVariants({ size: "sm" })}
											>
												<CalendarCheck2 className="mr-1.5 size-4" />
												Agendar
											</Link>
										) : (
											<Link
												to="/login"
												className={buttonVariants({
													variant: "outline",
													size: "sm",
												})}
											>
												<LogIn className="mr-1.5 size-4" />
												Entrar para agendar
											</Link>
										)}
									</CardContent>
								</Card>
							</li>
						))}
					</ul>
				)}
			</section>
		</div>
	);
}
