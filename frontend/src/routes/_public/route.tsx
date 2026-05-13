import { createFileRoute, Link, Outlet } from "@tanstack/react-router";
import { CalendarCheck2, LayoutDashboard, LogIn } from "lucide-react";
import ToggleTheme from "@/components/theme/theme-toggle";
import { buttonVariants } from "@/components/ui/button";
import { useSession } from "@/lib/session-hook";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_public")({
	component: PublicLayout,
});

function PublicLayout() {
	const session = useSession();

	return (
		<section className="relative flex min-h-screen flex-col overflow-x-clip bg-background">
			<PublicNavbar role={session?.role ?? null} />
			<main className="mx-auto w-full max-w-6xl flex-1 px-4 pt-24 pb-12 sm:pt-28">
				<Outlet />
			</main>
			<footer className="mx-auto w-full max-w-6xl border-t px-4 py-6 text-center text-xs text-muted-foreground">
				© AgendaFácil
			</footer>
		</section>
	);
}

function PublicNavbar({
	role,
}: Readonly<{ role: "customer" | "owner" | "admin" | null }>) {
	return (
		<header className="fixed inset-x-0 top-3 z-50 mx-auto flex w-[calc(100%-1.5rem)] max-w-3xl items-center justify-between gap-2 rounded-full border border-foreground/10 bg-background/60 px-2.5 py-1.5 shadow-lg shadow-primary/5 ring-1 ring-foreground/5 backdrop-blur-xl supports-backdrop-filter:bg-background/55 sm:top-4 sm:px-3 sm:py-2">
			<Link
				to="/"
				aria-label="Página inicial"
				className="inline-flex items-center gap-2 pl-1.5 text-sm font-semibold"
			>
				<CalendarCheck2 className="size-4 text-primary" />
				<span>AgendaFácil</span>
			</Link>
			<div className="flex items-center gap-1">
				<DashboardOrLoginLink role={role} />
				<ToggleTheme />
			</div>
		</header>
	);
}

function DashboardOrLoginLink({
	role,
}: Readonly<{ role: "customer" | "owner" | "admin" | null }>) {
	const className = cn(
		buttonVariants({ variant: "ghost", size: "sm" }),
		"rounded-full",
	);

	if (role === "customer") {
		return (
			<Link to="/customer/me" className={className}>
				<LayoutDashboard className="size-4" />
				Meu painel
			</Link>
		);
	}
	if (role === "owner") {
		return (
			<Link to="/owner" className={className}>
				<LayoutDashboard className="size-4" />
				Meu painel
			</Link>
		);
	}
	if (role === "admin") {
		return (
			<Link to="/admin" className={className}>
				<LayoutDashboard className="size-4" />
				Meu painel
			</Link>
		);
	}
	return (
		<Link to="/login" className={cn(className, "border border-foreground/10")}>
			<LogIn className="size-4" />
			Entrar
		</Link>
	);
}
