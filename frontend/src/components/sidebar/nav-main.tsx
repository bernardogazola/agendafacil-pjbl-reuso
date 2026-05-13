import { Link, useRouterState } from "@tanstack/react-router";
import type { LucideIcon } from "lucide-react";

import {
	SidebarGroup,
	SidebarGroupLabel,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
} from "@/components/ui/sidebar";

export interface NavItem {
	title: string;
	url: string;
	icon: LucideIcon;
	matchPrefix?: boolean;
}

export function NavMain({ items }: Readonly<{ items: NavItem[] }>) {
	const pathname = useRouterState({ select: (s) => s.location.pathname });

	return (
		<SidebarGroup>
			<SidebarGroupLabel>Plataforma</SidebarGroupLabel>
			<SidebarMenu>
				{items.map((item) => {
					const isActive =
						item.matchPrefix === false
							? pathname === item.url
							: pathname === item.url || pathname.startsWith(`${item.url}/`);
					return (
						<SidebarMenuItem key={item.url}>
							<SidebarMenuButton
								tooltip={item.title}
								isActive={isActive}
								render={<Link to={item.url} />}
							>
								<item.icon />
								<span>{item.title}</span>
							</SidebarMenuButton>
						</SidebarMenuItem>
					);
				})}
			</SidebarMenu>
		</SidebarGroup>
	);
}
