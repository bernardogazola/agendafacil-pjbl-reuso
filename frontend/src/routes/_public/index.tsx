import { createFileRoute, Link } from "@tanstack/react-router";
import {
	ArrowRight,
	Brain,
	Building2,
	CalendarCheck2,
	Check,
	Dumbbell,
	Heart,
	Scissors,
	Sparkles,
	Stethoscope,
	Tag,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
	Tooltip,
	TooltipContent,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import {
	BUSINESS_CATEGORY_HINTS,
	BUSINESS_CATEGORY_LABELS,
	PLAN_FEATURES,
	PLAN_HINTS,
	PLAN_LABELS,
	PLAN_PRICES,
} from "@/lib/labels";
import type { BusinessCategory, BusinessPlan } from "@/lib/types";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_public/")({
	staticData: { crumb: "Início" },
	head: () => ({
		meta: [{ title: "AgendaFácil — agendamento online simples" }],
	}),
	component: LandingPage,
});

const CATEGORY_ICONS: Record<
	BusinessCategory,
	React.ComponentType<{ className?: string }>
> = {
	BARBER_SHOP: Scissors,
	SALON: Sparkles,
	CLINIC: Stethoscope,
	AESTHETICS: Heart,
	PERSONAL_TRAINER: Dumbbell,
	PSYCHOLOGIST: Brain,
	OTHER: Tag,
};

const PLAN_ORDER = [
	"BASIC",
	"PROFESSIONAL",
	"PREMIUM",
] as const satisfies readonly BusinessPlan[];

function LandingPage() {
	return (
		<div className="space-y-24 sm:space-y-32">
			<HeroSection />
			<AudienceSection />
			<CategoriesSection />
			<StepsSection />
			<PlansSection />
			<FinalCtaSection />
		</div>
	);
}

function HeroSection() {
	return (
		<section className="relative isolate flex flex-col items-center pt-6 pb-4 text-center sm:pt-12">
			<div
				aria-hidden="true"
				className="pointer-events-none absolute -top-12 left-1/2 -z-10 h-160 w-screen -translate-x-1/2 overflow-hidden"
			>
				<div className="absolute -top-24 left-1/2 size-160 -translate-x-1/2 rounded-full bg-chart-1/15 blur-3xl" />
				<div className="absolute top-24 right-[8%] size-120 rounded-full bg-chart-2/12 blur-3xl" />
				<div className="absolute top-32 left-[8%] size-112 rounded-full bg-chart-3/10 blur-3xl" />
			</div>

			<h1 className="mt-7 max-w-3xl text-balance text-4xl font-semibold tracking-tight text-foreground sm:text-5xl md:text-6xl lg:text-7xl">
				Agendamento online{" "}
				<span className="bg-linear-to-r from-primary via-chart-2 to-chart-3 bg-clip-text text-transparent">
					simples
				</span>
				<br className="hidden sm:inline" /> para pequenos negócios.
			</h1>

			<p className="mt-6 max-w-2xl text-balance text-base text-muted-foreground sm:text-lg">
				Agende com tranquilidade ou atenda com eficiência.
			</p>

			<div className="mt-9 flex flex-col gap-3 sm:flex-row">
				<Link
					to="/signup"
					className={cn(
						buttonVariants({ size: "lg" }),
						"group cursor-pointer shadow-lg shadow-primary/20",
					)}
				>
					Comece grátis
					<ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
				</Link>
				<Link
					to="/businesses"
					className={cn(
						buttonVariants({ variant: "outline", size: "lg" }),
						"cursor-pointer bg-background/60 backdrop-blur-sm",
					)}
				>
					Encontrar serviços
				</Link>
			</div>
		</section>
	);
}

function AudienceSection() {
	return (
		<section className="grid gap-5 sm:grid-cols-2">
			<AudienceCard
				icon={<CalendarCheck2 className="size-6" />}
				title="Para clientes"
				description="Quero agendar serviços de forma simples."
				bullets={[
					"Encontre estabelecimentos por categoria",
					"Reserve em poucos cliques",
					"Acompanhe seus agendamentos",
				]}
				ctaLabel="Sou cliente"
			/>
			<AudienceCard
				icon={<Building2 className="size-6" />}
				title="Para donos"
				description="Quero oferecer serviços e gerenciar minha agenda."
				bullets={[
					"Configure horários e serviços",
					"Receba agendamentos online",
					"Relatórios e promoções",
				]}
				ctaLabel="Tenho um negócio"
			/>
		</section>
	);
}

function AudienceCard({
	icon,
	title,
	description,
	bullets,
	ctaLabel,
}: Readonly<{
	icon: React.ReactNode;
	title: string;
	description: string;
	bullets: readonly string[];
	ctaLabel: string;
}>) {
	return (
		<Card className="group relative overflow-hidden transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-primary/10">
			<div
				aria-hidden="true"
				className="pointer-events-none absolute inset-x-0 top-0 h-28 bg-linear-to-b from-primary/8 via-primary/4 to-transparent"
			/>
			<CardHeader className="relative">
				<div className="flex size-12 items-center justify-center rounded-xl bg-primary/10 text-primary ring-1 ring-primary/20">
					{icon}
				</div>
				<CardTitle className="mt-3 text-xl">{title}</CardTitle>
				<CardDescription className="text-base">{description}</CardDescription>
			</CardHeader>
			<CardContent>
				<ul className="space-y-3 text-sm sm:text-[15px]">
					{bullets.map((bullet) => (
						<li key={bullet} className="flex items-start gap-2.5">
							<span className="mt-0.5 inline-flex size-5 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary ring-1 ring-primary/15">
								<Check className="size-3" />
							</span>
							<span className="text-foreground/90">{bullet}</span>
						</li>
					))}
				</ul>
			</CardContent>
			<CardFooter>
				<Link
					to="/signup"
					className={cn(
						buttonVariants({ variant: "ghost", size: "sm" }),
						"group/cta ml-auto cursor-pointer",
					)}
				>
					{ctaLabel}
					<ArrowRight className="size-3.5 transition-transform group-hover/cta:translate-x-0.5" />
				</Link>
			</CardFooter>
		</Card>
	);
}

function CategoriesSection() {
	return (
		<section className="text-center">
			<h2 className="text-2xl font-semibold tracking-tight sm:text-3xl md:text-4xl">
				Para diferentes tipos de serviço
			</h2>
			<p className="mt-3 text-sm text-muted-foreground sm:text-base">
				Funciona para qualquer agenda que precise de horários e profissionais.
			</p>
			<ul className="mx-auto mt-10 flex max-w-3xl flex-wrap justify-center gap-2.5">
				{(
					Object.entries(BUSINESS_CATEGORY_LABELS) as Array<
						[BusinessCategory, string]
					>
				).map(([key, label]) => {
					const Icon = CATEGORY_ICONS[key];
					return (
						<li key={key}>
							<Tooltip>
								<TooltipTrigger
									type="button"
									className="inline-flex h-10 cursor-default items-center gap-2 rounded-full border border-foreground/10 bg-card/60 px-4 text-sm font-medium text-foreground/90 shadow-xs ring-1 ring-foreground/5 backdrop-blur-sm transition-colors hover:border-primary/40 hover:bg-primary/5"
								>
									<Icon className="size-4 text-primary" />
									{label}
								</TooltipTrigger>
								<TooltipContent>{BUSINESS_CATEGORY_HINTS[key]}</TooltipContent>
							</Tooltip>
						</li>
					);
				})}
			</ul>
		</section>
	);
}

const STEPS = [
	{
		number: "1",
		title: "Crie sua conta",
		body: "Cadastre seu estabelecimento e escolha um plano. Leva menos de 2 minutos.",
		bg: "bg-chart-1",
	},
	{
		number: "2",
		title: "Configure serviços e horários",
		body: "Cadastre os serviços oferecidos, defina preços e horários de atendimento.",
		bg: "bg-chart-2",
	},
	{
		number: "3",
		title: "Receba agendamentos",
		body: "Clientes encontram seu negócio e marcam horários online. Acompanhe tudo pelo painel.",
		bg: "bg-chart-3",
	},
] as const;

function StepsSection() {
	return (
		<section>
			<h2 className="text-center text-2xl font-semibold tracking-tight sm:text-3xl md:text-4xl">
				Como funciona
			</h2>
			<p className="mt-3 text-center text-sm text-muted-foreground sm:text-base">
				Três passos para começar a receber agendamentos online.
			</p>
			<ol className="mt-12 grid gap-10 sm:grid-cols-3 sm:gap-8">
				{STEPS.map((step, i) => (
					<li key={step.number} className="relative flex flex-col items-start">
						{i < STEPS.length - 1 && (
							<div
								aria-hidden="true"
								className="pointer-events-none absolute top-6 left-14 hidden h-px w-[calc(100%-2rem)] bg-linear-to-r from-border via-border/60 to-transparent sm:block"
							/>
						)}
						<div
							className={cn(
								"relative z-10 flex size-12 items-center justify-center rounded-2xl text-base font-semibold text-primary-foreground shadow-md ring-4 ring-background",
								step.bg,
							)}
						>
							{step.number}
						</div>
						<h3 className="mt-5 text-lg font-semibold text-foreground">
							{step.title}
						</h3>
						<p className="mt-2 text-sm text-muted-foreground sm:text-base">
							{step.body}
						</p>
					</li>
				))}
			</ol>
		</section>
	);
}

function PlansSection() {
	return (
		<section>
			<h2 className="text-center text-2xl font-semibold tracking-tight sm:text-3xl md:text-4xl">
				Planos para cada tamanho de negócio
			</h2>
			<p className="mt-3 text-center text-sm text-muted-foreground sm:text-base">
				Comece grátis e evolua conforme a agenda crescer.
			</p>
			<div className="mt-12 grid gap-6 sm:grid-cols-3 sm:gap-5">
				{PLAN_ORDER.map((plan) => (
					<PlanCard key={plan} plan={plan} />
				))}
			</div>
		</section>
	);
}

function PlanCard({ plan }: Readonly<{ plan: BusinessPlan }>) {
	const highlighted = plan === "PROFESSIONAL";
	const price = PLAN_PRICES[plan];

	return (
		<div className={cn("relative h-full", highlighted && "sm:-translate-y-4")}>
			{highlighted && (
				<div className="absolute -top-3 left-1/2 z-10 -translate-x-1/2">
					<Badge className="gap-1 bg-linear-to-r from-primary via-chart-2 to-chart-3 px-3 py-1 text-primary-foreground shadow-md shadow-primary/30">
						<Sparkles className="size-3" />
						Mais popular
					</Badge>
				</div>
			)}
			<Card
				className={cn(
					"h-full transition-shadow",
					highlighted
						? "shadow-2xl shadow-primary/20 ring-2 ring-primary"
						: "hover:shadow-lg hover:shadow-primary/10",
				)}
			>
				<CardHeader>
					<CardTitle className="text-lg">{PLAN_LABELS[plan]}</CardTitle>
					<CardDescription>{PLAN_HINTS[plan]}</CardDescription>
					<div className="mt-4 flex items-baseline gap-1">
						<span className="text-4xl font-semibold tracking-tight text-foreground">
							{price.amount}
						</span>
						<span className="text-sm text-muted-foreground">
							{price.period}
						</span>
					</div>
				</CardHeader>
				<CardContent className="flex-1">
					<Separator className="mb-5" />
					<ul className="space-y-3 text-sm">
						{PLAN_FEATURES[plan].map((feature) => (
							<li key={feature} className="flex items-start gap-2.5">
								<span className="mt-0.5 inline-flex size-5 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary ring-1 ring-primary/15">
									<Check className="size-3" />
								</span>
								<span className="text-foreground/90">{feature}</span>
							</li>
						))}
					</ul>
				</CardContent>
				<CardFooter>
					<Link
						to="/signup"
						className={cn(
							buttonVariants({
								variant: highlighted ? "default" : "outline",
								size: "lg",
							}),
							"w-full cursor-pointer",
							highlighted && "shadow-lg shadow-primary/25",
						)}
					>
						Começar
					</Link>
				</CardFooter>
			</Card>
		</div>
	);
}

function FinalCtaSection() {
	return (
		<section>
			<Card className="relative isolate overflow-hidden border-primary/15 bg-linear-to-br from-primary/8 via-chart-2/5 to-chart-3/8 text-center">
				<div
					aria-hidden="true"
					className="pointer-events-none absolute -top-24 left-1/2 size-112 -translate-x-1/2 rounded-full bg-primary/15 blur-3xl"
				/>
				<CardContent className="relative px-6 py-12 sm:px-12 sm:py-20">
					<h2 className="text-3xl font-semibold tracking-tight text-foreground sm:text-4xl md:text-5xl">
						Pronto para começar?
					</h2>
					<p className="mx-auto mt-4 max-w-xl text-base text-muted-foreground sm:text-lg">
						Crie sua conta gratuita em menos de 2 minutos.
					</p>
					<div className="mt-9 flex flex-col gap-3 sm:flex-row sm:justify-center">
						<Link
							to="/signup"
							className={cn(
								buttonVariants({ size: "lg" }),
								"group cursor-pointer shadow-lg shadow-primary/20",
							)}
						>
							Comece grátis
							<ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
						</Link>
						<Link
							to="/login"
							className={cn(
								buttonVariants({ variant: "ghost", size: "lg" }),
								"cursor-pointer",
							)}
						>
							Já tenho conta
						</Link>
					</div>
				</CardContent>
			</Card>
		</section>
	);
}
