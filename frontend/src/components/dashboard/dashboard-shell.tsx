import { AppSidebar } from "@/components/sidebar/app-sidebar";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { useSession } from "@/lib/session-hook";
import type { UserRole } from "@/lib/types";

import { DashboardHeader } from "./dashboard-header";

export interface DashboardShellProps {
	userRole: UserRole;
	children: React.ReactNode;
}

export function DashboardShell({
	userRole,
	children,
}: Readonly<DashboardShellProps>) {
	const session = useSession();
	return (
		<SidebarProvider>
			<AppSidebar userRole={userRole} session={session} />
			<SidebarInset>
				<DashboardHeader />
				<main className="flex-1 space-y-6 p-4 md:p-6">{children}</main>
			</SidebarInset>
		</SidebarProvider>
	);
}
