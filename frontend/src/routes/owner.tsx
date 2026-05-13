import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";

import { DashboardShell } from "@/components/dashboard/dashboard-shell";

export const Route = createFileRoute("/owner")({
	staticData: { crumb: "Painel" },
	beforeLoad: ({ context }) => {
		if (!context.session) {
			throw redirect({ to: "/login" });
		}
		if (context.session.role === "admin") {
			throw redirect({ to: "/admin" });
		}
		if (context.session.role !== "owner") {
			throw redirect({ to: "/customer/me" });
		}
		if (!context.session.businessId) {
			throw redirect({ to: "/" });
		}
	},
	component: OwnerLayout,
});

function OwnerLayout() {
	return (
		<DashboardShell userRole="owner">
			<Outlet />
		</DashboardShell>
	);
}
