import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import { PageHeader } from "@/components/dashboard/page-header";
import { OwnerProfileForm } from "@/components/form/owner-profile-form";
import { businessDetailOptions } from "@/lib/queries/businesses";
import { useSession } from "@/lib/session-hook";

export const Route = createFileRoute("/owner/profile")({
	staticData: { crumb: "Perfil" },
	loader: ({ context }) => {
		const bid = context.session?.businessId;
		if (!bid) return;
		return context.queryClient.ensureQueryData(businessDetailOptions(bid));
	},
	component: OwnerProfilePage,
});

function OwnerProfilePage() {
	const session = useSession();
	const businessId = session?.businessId ?? 0;
	const { data, isLoading } = useQuery(businessDetailOptions(businessId));

	return (
		<>
			<PageHeader
				title="Dados do estabelecimento"
				description="Atualize as informações exibidas para os clientes e a política de cancelamento aplicada aos agendamentos."
			/>

			<div className="max-w-xl">
				{isLoading || !data ? (
					<div className="flex items-center justify-center py-12">
						<LoaderCircle
							aria-label="Carregando estabelecimento"
							className="size-6 animate-spin text-muted-foreground"
						/>
					</div>
				) : (
					<OwnerProfileForm business={data} />
				)}
			</div>
		</>
	);
}
