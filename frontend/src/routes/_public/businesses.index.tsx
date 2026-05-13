import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { LoaderCircle, Search, Store } from "lucide-react";
import { useMemo } from "react";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectItemDescription,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import {
	BUSINESS_CATEGORY_HINTS,
	BUSINESS_CATEGORY_LABELS,
	PLAN_LABELS,
} from "@/lib/labels";
import { businessesListOptions } from "@/lib/queries/businesses";
import type { BusinessCategory } from "@/lib/types";
import { CATEGORY_OPTIONS } from "@/lib/validator/signup.schema";

const ALL_CATEGORIES = "ALL" as const;
type CategoryFilter = BusinessCategory | typeof ALL_CATEGORIES;

const CATEGORY_SELECT_ITEMS: Record<CategoryFilter, string> = {
	[ALL_CATEGORIES]: "Todas as categorias",
	...BUSINESS_CATEGORY_LABELS,
};

const businessesSearchSchema = z.object({
	q: z.string().optional(),
	category: z.enum(CATEGORY_OPTIONS).optional(),
});

export const Route = createFileRoute("/_public/businesses/")({
	staticData: { crumb: "Estabelecimentos" },
	validateSearch: businessesSearchSchema,
	loader: ({ context }) =>
		context.queryClient.ensureQueryData(businessesListOptions()),
	component: BusinessesIndexPage,
});

function BusinessesIndexPage() {
	const navigate = useNavigate();
	const { q, category } = Route.useSearch();
	const { data, isLoading } = useQuery(businessesListOptions());

	const search = q ?? "";
	const categoryValue = category ?? ALL_CATEGORIES;

	const filtered = useMemo(() => {
		const list = data ?? [];
		const needle = search.trim().toLowerCase();
		return list.filter((b) => {
			if (category && b.category !== category) return false;
			if (needle && !b.tradeName.toLowerCase().includes(needle)) return false;
			return true;
		});
	}, [data, search, category]);

	const setSearch = (next: string) => {
		const value = next.trim() === "" ? undefined : next;
		void navigate({
			to: "/businesses",
			search: (prev) => ({ ...prev, q: value }),
			replace: true,
		});
	};

	const setCategory = (next: CategoryFilter) => {
		const value = next === ALL_CATEGORIES ? undefined : next;
		void navigate({
			to: "/businesses",
			search: (prev) => ({ ...prev, category: value }),
			replace: true,
		});
	};

	return (
		<div className="flex flex-col gap-6">
			<header>
				<h1 className="text-2xl font-semibold tracking-tight">
					Estabelecimentos
				</h1>
				<p className="mt-1 text-sm text-muted-foreground">
					Encontre onde agendar seu próximo atendimento.
				</p>
			</header>

			<div className="grid gap-3 sm:grid-cols-2">
				<div className="flex flex-col gap-2">
					<label
						htmlFor="businesses-search"
						className="text-sm font-medium text-foreground"
					>
						Buscar por nome
					</label>
					<div className="relative">
						<Search
							aria-hidden
							className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
						/>
						<Input
							id="businesses-search"
							type="text"
							value={search}
							onChange={(e) => setSearch(e.currentTarget.value)}
							placeholder="Ex: Barbearia do João"
							className="pl-9"
						/>
					</div>
				</div>
				<div className="flex flex-col gap-2">
					<label
						htmlFor="businesses-category"
						className="text-sm font-medium text-foreground"
					>
						Categoria
					</label>
					<Select
						items={CATEGORY_SELECT_ITEMS}
						value={categoryValue}
						onValueChange={(v) => setCategory(v as CategoryFilter)}
					>
						<SelectTrigger
							id="businesses-category"
							className="w-full cursor-pointer"
						>
							<SelectValue placeholder="Todas as categorias" />
						</SelectTrigger>
						<SelectContent>
							<SelectItem value={ALL_CATEGORIES} className="cursor-pointer">
								<span>Todas as categorias</span>
							</SelectItem>
							{CATEGORY_OPTIONS.map((c) => (
								<SelectItem key={c} value={c} className="cursor-pointer">
									<span>{BUSINESS_CATEGORY_LABELS[c]}</span>
									<SelectItemDescription>
										{BUSINESS_CATEGORY_HINTS[c]}
									</SelectItemDescription>
								</SelectItem>
							))}
						</SelectContent>
					</Select>
				</div>
			</div>

			{isLoading ? (
				<div className="flex items-center justify-center py-12">
					<LoaderCircle
						aria-label="Carregando estabelecimentos"
						className="size-6 animate-spin text-muted-foreground"
					/>
				</div>
			) : filtered.length === 0 ? (
				<Card className="border-dashed">
					<CardContent className="flex flex-col items-center gap-2 py-10 text-center">
						<Store className="size-8 text-muted-foreground" />
						<p className="text-sm font-medium">
							Nenhum estabelecimento encontrado
						</p>
						<p className="text-sm text-muted-foreground">
							Tente outra categoria ou ajuste a busca.
						</p>
					</CardContent>
				</Card>
			) : (
				<ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
					{filtered.map((b) => (
						<li key={b.id} className="contents">
							<Link
								to="/businesses/$businessId"
								params={{ businessId: b.id }}
								className="block rounded-xl transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/50"
							>
								<Card className="h-full transition-colors hover:border-primary">
									<CardHeader>
										<CardDescription className="uppercase tracking-wider">
											{BUSINESS_CATEGORY_LABELS[b.category]}
										</CardDescription>
										<CardTitle className="text-base">{b.tradeName}</CardTitle>
									</CardHeader>
									<CardContent className="flex flex-col gap-3 pt-0">
										<p className="truncate text-xs text-muted-foreground">
											{b.email}
										</p>
										<Badge variant="outline" className="w-fit">
											Plano {PLAN_LABELS[b.plan]}
										</Badge>
									</CardContent>
								</Card>
							</Link>
						</li>
					))}
				</ul>
			)}
		</div>
	);
}
