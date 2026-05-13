import { useRouter } from "@tanstack/react-router";
import {
	Check,
	ChevronsUpDown,
	LogOut,
	Monitor,
	Moon,
	Palette,
	Sun,
} from "lucide-react";
import { useTheme } from "next-themes";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuGroup,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuSub,
	DropdownMenuSubContent,
	DropdownMenuSubTrigger,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
	useSidebar,
} from "@/components/ui/sidebar";
import { notifySessionChange, type Session } from "@/lib/auth";
import { queryClient } from "@/lib/query-client";
import { logoutFn } from "@/lib/server/auth-fns";

function getInitials(name: string): string {
	const parts = name.trim().split(/\s+/).filter(Boolean);
	if (parts.length === 0) return "?";
	if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
	return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function NavUser({ session }: Readonly<{ session: Session }>) {
	const { isMobile } = useSidebar();
	const router = useRouter();
	const { theme, setTheme } = useTheme();

	const initials = getInitials(session.name);

	const handleLogout = async () => {
		await logoutFn();
		queryClient.clear();
		notifySessionChange();
		await router.invalidate();
		router.navigate({ to: "/" });
	};

	return (
		<SidebarMenu>
			<SidebarMenuItem>
				<DropdownMenu>
					<DropdownMenuTrigger
						render={
							<SidebarMenuButton
								size="lg"
								className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
							>
								<Avatar className="h-8 w-8 rounded-lg">
									<AvatarFallback className="rounded-lg">
										{initials}
									</AvatarFallback>
								</Avatar>
								<div className="grid flex-1 text-left text-sm leading-tight">
									<span className="truncate font-medium">{session.name}</span>
									<span className="truncate text-xs text-muted-foreground">
										{session.email}
									</span>
								</div>
								<ChevronsUpDown className="ml-auto size-4" />
							</SidebarMenuButton>
						}
					/>
					<DropdownMenuContent
						className="min-w-56 rounded-lg"
						side={isMobile ? "bottom" : "right"}
						align="end"
						sideOffset={4}
					>
						<DropdownMenuGroup>
							<DropdownMenuLabel className="p-0 font-normal">
								<div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
									<Avatar className="h-8 w-8 rounded-lg">
										<AvatarFallback className="rounded-lg">
											{initials}
										</AvatarFallback>
									</Avatar>
									<div className="grid flex-1 text-left text-sm leading-tight">
										<span className="truncate font-medium">{session.name}</span>
										<span className="truncate text-xs text-muted-foreground">
											{session.email}
										</span>
									</div>
								</div>
							</DropdownMenuLabel>
						</DropdownMenuGroup>
						<DropdownMenuSeparator />
						<DropdownMenuGroup>
							<DropdownMenuSub>
								<DropdownMenuSubTrigger>
									<Palette />
									Tema
								</DropdownMenuSubTrigger>
								<DropdownMenuSubContent>
									<DropdownMenuItem onClick={() => setTheme("light")}>
										<Sun />
										Claro
										{theme === "light" ? (
											<Check className="ml-auto size-3.5" />
										) : null}
									</DropdownMenuItem>
									<DropdownMenuItem onClick={() => setTheme("dark")}>
										<Moon />
										Escuro
										{theme === "dark" ? (
											<Check className="ml-auto size-3.5" />
										) : null}
									</DropdownMenuItem>
									<DropdownMenuItem onClick={() => setTheme("system")}>
										<Monitor />
										Sistema
										{theme === "system" ? (
											<Check className="ml-auto size-3.5" />
										) : null}
									</DropdownMenuItem>
								</DropdownMenuSubContent>
							</DropdownMenuSub>
							<DropdownMenuSeparator />
							<DropdownMenuItem onClick={() => void handleLogout()}>
								<LogOut />
								Sair
							</DropdownMenuItem>
						</DropdownMenuGroup>
					</DropdownMenuContent>
				</DropdownMenu>
			</SidebarMenuItem>
		</SidebarMenu>
	);
}
