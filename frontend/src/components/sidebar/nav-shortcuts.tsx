import { Link } from "@tanstack/react-router";
import type { LucideIcon } from "lucide-react";

import {
	SidebarGroup,
	SidebarGroupLabel,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
} from "@/components/ui/sidebar";

export interface ShortcutItem {
	name: string;
	url: string;
	icon: LucideIcon;
}

export function NavShortcuts({
	shortcuts,
}: Readonly<{ shortcuts: ShortcutItem[] }>) {
	if (shortcuts.length === 0) return null;
	return (
		<SidebarGroup className="group-data-[collapsible=icon]:hidden">
			<SidebarGroupLabel>Atalhos</SidebarGroupLabel>
			<SidebarMenu>
				{shortcuts.map((item) => (
					<SidebarMenuItem key={item.url}>
						<SidebarMenuButton
							tooltip={item.name}
							render={<Link to={item.url} />}
						>
							<item.icon />
							<span>{item.name}</span>
						</SidebarMenuButton>
					</SidebarMenuItem>
				))}
			</SidebarMenu>
		</SidebarGroup>
	);
}
