import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";

import { DashboardShell } from "@/components/dashboard/dashboard-shell";

export const Route = createFileRoute("/customer")({
	staticData: { crumb: "Início" },
	beforeLoad: ({ context }) => {
		if (!context.session) {
			throw redirect({ to: "/login" });
		}
		if (context.session.role === "admin") {
			throw redirect({ to: "/admin" });
		}
		if (context.session.role !== "customer") {
			throw redirect({ to: "/owner" });
		}
	},
	component: CustomerLayout,
});

function CustomerLayout() {
	return (
		<DashboardShell userRole="customer">
			<Outlet />
		</DashboardShell>
	);
}
