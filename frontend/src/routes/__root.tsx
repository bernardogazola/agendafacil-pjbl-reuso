import { TanStackDevtools } from "@tanstack/react-devtools";
import {
	createRootRouteWithContext,
	HeadContent,
	Link,
	Scripts,
	useRouter,
} from "@tanstack/react-router";
import { TanStackRouterDevtoolsPanel } from "@tanstack/react-router-devtools";
import { useEffect } from "react";
import { toast } from "sonner";
import { ThemeProvider } from "@/components/theme/theme-provider";
import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { notifySessionChange } from "@/lib/auth";
import { getSession } from "@/lib/auth.isomorphic";
import { queryClient } from "@/lib/query-client";
import { logoutFn } from "@/lib/server/auth-fns";
import type { RouterContext } from "@/router";
import TanStackQueryDevtools from "../integrations/tanstack-query/devtools";
import appCss from "../styles.css?url";

export const Route = createRootRouteWithContext<RouterContext>()({
	staleTime: Infinity,
	gcTime: Infinity,
	beforeLoad: () => ({ session: getSession() }),
	head: () => ({
		meta: [
			{ charSet: "utf-8" },
			{ name: "viewport", content: "width=device-width, initial-scale=1" },
			{ title: "AgendaFácil" },
			{
				name: "description",
				content:
					"AgendaFácil - sistema de agendamento online simples para pequenos negócios.",
			},
		],
		links: [{ rel: "stylesheet", href: appCss }],
	}),
	shellComponent: RootDocument,
	errorComponent: RootErrorBoundary,
	notFoundComponent: NotFoundPage,
});

function RootErrorBoundary({ error }: Readonly<{ error: Error }>) {
	return (
		<div className="mx-auto w-full max-w-lg px-4 py-16 text-center">
			<h1 className="text-xl font-semibold text-foreground">
				Algo não saiu como esperado
			</h1>
			<p className="mt-2 text-sm text-muted-foreground">
				{error.message || "Erro inesperado. Tente novamente."}
			</p>
			<Link
				to="/"
				className="mt-6 inline-block rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80"
			>
				Voltar ao início
			</Link>
		</div>
	);
}

function NotFoundPage() {
	return (
		<div className="mx-auto w-full max-w-lg px-4 py-16 text-center">
			<h1 className="text-xl font-semibold text-foreground">
				Página não encontrada
			</h1>
			<p className="mt-2 text-sm text-muted-foreground">
				A rota acessada não existe ou foi movida.
			</p>
			<Link
				to="/"
				className="mt-6 inline-block rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80"
			>
				Ir para o início
			</Link>
		</div>
	);
}

function RootDocument({ children }: { children: React.ReactNode }) {
	return (
		<html lang="pt-BR" suppressHydrationWarning>
			<head>
				<HeadContent />
			</head>
			<body className="min-h-screen bg-background antialiased">
				<ThemeProvider
					attribute="class"
					defaultTheme="system"
					enableSystem
					disableTransitionOnChange
				>
					<TooltipProvider>
						{children}
						<UnauthorizedListener />
					</TooltipProvider>
					<TanStackDevtools
						config={{
							position: "bottom-right",
						}}
						plugins={[
							{
								name: "Tanstack Router",
								render: <TanStackRouterDevtoolsPanel />,
							},
							TanStackQueryDevtools,
						]}
					/>
					<Toaster richColors />
					<Scripts />
				</ThemeProvider>
			</body>
		</html>
	);
}

function UnauthorizedListener() {
	const router = useRouter();
	useEffect(() => {
		const handler = async () => {
			await logoutFn();
			queryClient.clear();
			notifySessionChange();
			toast.error("Sessão expirada. Faça login novamente.");
			await router.invalidate();
			router.navigate({ to: "/login" });
		};
		globalThis.addEventListener("agf:unauthorized", handler);
		return () => globalThis.removeEventListener("agf:unauthorized", handler);
	}, [router]);
	return null;
}
