import { Link } from "@tanstack/react-router";
import {
	BarChart3,
	Building2,
	CalendarCheck2,
	CalendarRange,
	Clock,
	LayoutDashboard,
	Scissors,
	Search,
	Settings,
	Star,
	Tag,
	UserCog,
	UserPlus,
	Users,
} from "lucide-react";

import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarHeader,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
	SidebarRail,
} from "@/components/ui/sidebar";
import type { Session } from "@/lib/auth";
import type { UserRole } from "@/lib/types";

import { type NavItem, NavMain } from "./nav-main";
import { NavShortcuts, type ShortcutItem } from "./nav-shortcuts";
import { NavUser } from "./nav-user";

const NAV_BY_ROLE: Record<UserRole, NavItem[]> = {
	owner: [
		{
			title: "Painel",
			url: "/owner",
			icon: LayoutDashboard,
			matchPrefix: false,
		},
		{ title: "Agenda", url: "/owner/agenda", icon: CalendarRange },
		{ title: "Serviços", url: "/owner/services", icon: Scissors },
		{ title: "Promoções", url: "/owner/promotions", icon: Tag },
		{ title: "Horários", url: "/owner/hours", icon: Clock },
		{ title: "Avaliações", url: "/owner/reviews", icon: Star },
		{ title: "Relatórios", url: "/owner/reports", icon: BarChart3 },
		{ title: "Perfil", url: "/owner/profile", icon: Settings },
	],
	customer: [
		{
			title: "Início",
			url: "/customer/me",
			icon: LayoutDashboard,
			matchPrefix: false,
		},
		{ title: "Estabelecimentos", url: "/businesses", icon: Search },
		{ title: "Minhas avaliações", url: "/customer/reviews", icon: Star },
	],
	admin: [
		{
			title: "Painel",
			url: "/admin",
			icon: LayoutDashboard,
			matchPrefix: false,
		},
		{ title: "Clientes", url: "/admin/customers", icon: Users },
		{ title: "Administradores", url: "/admin/administrators", icon: UserCog },
		{ title: "Estabelecimentos", url: "/admin/businesses", icon: Building2 },
	],
};

const SHORTCUTS_BY_ROLE: Record<UserRole, ShortcutItem[]> = {
	owner: [
		{
			name: "Novo serviço",
			url: "/owner/services",
			search: { create: true },
			icon: Scissors,
		},
		{
			name: "Nova promoção",
			url: "/owner/promotions",
			search: { create: true },
			icon: Tag,
		},
	],
	customer: [],
	admin: [
		{
			name: "Novo administrador",
			url: "/admin/administrators",
			search: { create: true },
			icon: UserPlus,
		},
	],
};

const HOME_BY_ROLE: Record<UserRole, string> = {
	owner: "/owner",
	customer: "/customer/me",
	admin: "/admin",
};

export interface AppSidebarProps extends React.ComponentProps<typeof Sidebar> {
	userRole: UserRole;
	session: Session | null;
}

export function AppSidebar({
	userRole,
	session,
	...props
}: Readonly<AppSidebarProps>) {
	return (
		<Sidebar collapsible="icon" {...props}>
			<SidebarHeader>
				<SidebarMenu>
					<SidebarMenuItem>
						<SidebarMenuButton
							size="lg"
							tooltip="AgendaFácil"
							render={<Link to={HOME_BY_ROLE[userRole]} />}
						>
							<div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
								<CalendarCheck2 className="size-4" />
							</div>
							<div className="grid flex-1 text-left text-sm leading-tight">
								<span className="truncate font-semibold">AgendaFácil</span>
								<span className="truncate text-xs text-muted-foreground">
									{userRole === "owner"
										? "Estabelecimento"
										: userRole === "admin"
											? "Administração"
											: "Cliente"}
								</span>
							</div>
						</SidebarMenuButton>
					</SidebarMenuItem>
				</SidebarMenu>
			</SidebarHeader>

			<SidebarContent>
				<NavMain items={NAV_BY_ROLE[userRole]} />
				<NavShortcuts shortcuts={SHORTCUTS_BY_ROLE[userRole]} />
			</SidebarContent>

			<SidebarFooter>
				{session ? <NavUser session={session} /> : null}
			</SidebarFooter>
			<SidebarRail className="cursor-pointer!" />
		</Sidebar>
	);
}
