import { createRouter as createTanStackRouter } from "@tanstack/react-router";
import { setupRouterSsrQueryIntegration } from "@tanstack/react-router-ssr-query";
import type { Session } from "./lib/auth";
import { getSession } from "./lib/auth.isomorphic";
import { queryClient } from "./lib/query-client";
import { routeTree } from "./routeTree.gen";

/**
 * Contexto compartilhado pelas rotas do TanStack Router.
 *
 * `session` é resolvida de forma isomórfica: no servidor, a leitura acontece a
 * partir dos cookies da requisição; no navegador, a partir de `document.cookie`.
 * A rota raiz atualiza esse valor novamente em seu `beforeLoad`, especialmente
 * após `router.invalidate()` no login e no logout.
 */
export interface RouterContext {
	queryClient: typeof queryClient;
	session: Session | null;
}

/**
 * Cria a instância do router da aplicação.
 *
 * Além de registrar a árvore de rotas, o método injeta o `queryClient` e a
 * sessão inicial no contexto. A integração com SSR Query permite que loaders e
 * queries trabalhem juntos durante renderização no servidor e no cliente.
 *
 * @returns instância configurada do TanStack Router
 */
export function getRouter() {
	const router = createTanStackRouter({
		routeTree,
		scrollRestoration: true,
		defaultPreload: "intent",
		defaultPreloadStaleTime: 0,
		context: {
			queryClient,
			session: getSession(),
		} satisfies RouterContext,
	});

	setupRouterSsrQueryIntegration({ router, queryClient });

	return router;
}

declare module "@tanstack/react-router" {
	interface Register {
		router: ReturnType<typeof getRouter>;
	}
}
