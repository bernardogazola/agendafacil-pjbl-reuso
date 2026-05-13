import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";

import { DashboardShell } from "@/components/dashboard/dashboard-shell";

export const Route = createFileRoute("/admin")({
	staticData: { crumb: "Painel administrativo" },
	beforeLoad: ({ context }) => {
		if (!context.session) {
			throw redirect({ to: "/login" });
		}
		if (context.session.role !== "admin") {
			throw redirect({
				to: context.session.role === "owner" ? "/owner" : "/customer/me",
			});
		}
	},
	component: AdminLayout,
});

function AdminLayout() {
	return (
		<DashboardShell userRole="admin">
			<Outlet />
		</DashboardShell>
	);
}
