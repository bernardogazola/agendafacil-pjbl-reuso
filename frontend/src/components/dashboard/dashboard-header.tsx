import ToggleTheme from "@/components/theme/theme-toggle";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { DashboardBreadcrumb } from "./dashboard-breadcrumb";

export function DashboardHeader() {
	return (
		<header className="sticky top-0 z-10 flex h-14 shrink-0 items-center gap-2 border-b bg-background/95 px-4 backdrop-blur supports-backdrop-filter:bg-background/60">
			<SidebarTrigger className="-ml-1" />
			<Separator orientation="vertical" className="mr-2 h-4" />
			<DashboardBreadcrumb />
			<div className="ml-auto flex items-center gap-1">
				<ToggleTheme />
			</div>
		</header>
	);
}
