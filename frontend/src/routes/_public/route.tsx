import { createFileRoute, Link, Outlet } from "@tanstack/react-router";
import { GalleryVerticalEnd, LayoutDashboard, LogIn } from "lucide-react";
import ToggleTheme from "@/components/theme/theme-toggle";
import { buttonVariants } from "@/components/ui/button";
import { useSession } from "@/lib/session-hook";

export const Route = createFileRoute("/_public")({
	component: PublicLayout,
});

function PublicLayout() {
	const session = useSession();

	return (
		<section className="grid min-h-screen grid-rows-[auto_1fr_auto] bg-background px-4">
			<header className="mx-auto flex w-full max-w-7xl items-center justify-between border-b py-3">
				<Link
					to="/"
					aria-label="Página inicial"
					className="inline-flex items-center gap-2"
				>
					<GalleryVerticalEnd className="size-5" />
					<span className="font-semibold">AgendaFácil</span>
				</Link>
				<div className="flex items-center gap-2">
					<DashboardOrLoginLink role={session?.role ?? null} />
					<ToggleTheme />
				</div>
			</header>
			<main className="mx-auto w-full max-w-5xl py-8 sm:py-10">
				<Outlet />
			</main>
			<footer className="mx-auto w-full max-w-7xl border-t py-4 text-center text-xs text-muted-foreground">
				© AgendaFácil
			</footer>
		</section>
	);
}

function DashboardOrLoginLink({
	role,
}: Readonly<{ role: "customer" | "owner" | "admin" | null }>) {
	const className = buttonVariants({ variant: "outline", size: "sm" });

	if (role === "customer") {
		return (
			<Link to="/customer/me" className={className}>
				<LayoutDashboard className="mr-1.5 size-4" />
				Meu painel
			</Link>
		);
	}
	if (role === "owner") {
		return (
			<Link to="/owner" className={className}>
				<LayoutDashboard className="mr-1.5 size-4" />
				Meu painel
			</Link>
		);
	}
	if (role === "admin") {
		return (
			<Link to="/admin" className={className}>
				<LayoutDashboard className="mr-1.5 size-4" />
				Meu painel
			</Link>
		);
	}
	return (
		<Link to="/login" className={className}>
			<LogIn className="mr-1.5 size-4" />
			Entrar
		</Link>
	);
}
