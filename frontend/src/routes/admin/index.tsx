import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight, Building2, UserCog, Users } from "lucide-react";
import { PageHeader } from "@/components/dashboard/page-header";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { adminAdministratorsListOptions } from "@/lib/queries/admin/administrators";
import { adminBusinessesListOptions } from "@/lib/queries/admin/businesses";
import { adminCustomersListOptions } from "@/lib/queries/admin/customers";

export const Route = createFileRoute("/admin/")({
	staticData: { crumb: "Painel" },
	loader: ({ context }) =>
		Promise.all([
			context.queryClient.ensureQueryData(adminCustomersListOptions(false)),
			context.queryClient.ensureQueryData(
				adminAdministratorsListOptions(false),
			),
			context.queryClient.ensureQueryData(adminBusinessesListOptions(false)),
		]),
	component: AdminDashboard,
});

function AdminDashboard() {
	const customersQ = useQuery(adminCustomersListOptions(false));
	const adminsQ = useQuery(adminAdministratorsListOptions(false));
	const businessesQ = useQuery(adminBusinessesListOptions(false));

	return (
		<>
			<PageHeader
				title="Painel administrativo"
				description="Acesso reservado a administradores da plataforma. Selecione uma área para começar."
			/>

			<div className="grid gap-3 sm:grid-cols-3">
				<AdminCard
					to="/admin/customers"
					icon={<Users className="size-5" />}
					title="Clientes"
					description="Lista, edita e ativa/desativa clientes finais."
					count={customersQ.data?.length}
					active={customersQ.data?.filter((c) => c.active).length}
				/>
				<AdminCard
					to="/admin/administrators"
					icon={<UserCog className="size-5" />}
					title="Administradores"
					description="Cria e gerencia administradores e donos de estabelecimento."
					count={adminsQ.data?.length}
					active={adminsQ.data?.filter((a) => a.active).length}
				/>
				<AdminCard
					to="/admin/businesses"
					icon={<Building2 className="size-5" />}
					title="Estabelecimentos"
					description="Visualiza, edita e ativa/desativa estabelecimentos."
					count={businessesQ.data?.length}
					active={businessesQ.data?.filter((b) => b.active).length}
				/>
			</div>
		</>
	);
}

interface AdminCardProps {
	to: "/admin/customers" | "/admin/administrators" | "/admin/businesses";
	icon: React.ReactNode;
	title: string;
	description: string;
	count: number | undefined;
	active: number | undefined;
}

function AdminCard({
	to,
	icon,
	title,
	description,
	count,
	active,
}: Readonly<AdminCardProps>) {
	return (
		<Link
			to={to}
			className="group block focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 rounded-lg"
		>
			<Card className="h-full transition-colors group-hover:border-primary/50">
				<CardHeader>
					<div className="flex items-center justify-between text-muted-foreground">
						<span aria-hidden>{icon}</span>
						<ArrowRight className="size-4 opacity-0 transition-opacity group-hover:opacity-100" />
					</div>
					<CardTitle className="mt-2">{title}</CardTitle>
					<CardDescription>{description}</CardDescription>
				</CardHeader>
				<CardContent>
					{count === undefined ? (
						<p className="text-sm text-muted-foreground">Carregando…</p>
					) : (
						<div className="flex items-baseline gap-2">
							<p className="text-2xl font-semibold">{count}</p>
							<p className="text-xs text-muted-foreground">
								{active ?? 0} ativos
							</p>
						</div>
					)}
				</CardContent>
			</Card>
		</Link>
	);
}
