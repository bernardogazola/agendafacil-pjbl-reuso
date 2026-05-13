/**
 * Hook React para acompanhar a sessão atual do usuário.
 *
 * A sessão é lida a partir do cookie legível `agf_session`, que contém apenas
 * os dados básicos do usuário. O JWT não é acessado por este hook, pois fica no
 * cookie `httpOnly` usado pelo servidor.
 *
 * Como alterações em cookies não disparam automaticamente eventos no navegador,
 * os fluxos de login e logout emitem o evento `agf:session` depois de atualizar
 * a sessão. O hook escuta esse evento para renderizar novamente os componentes
 * que dependem do usuário autenticado.
 */
import { useSyncExternalStore } from "react";
import { parseSessionCookieValue, SESSION_COOKIE, type Session } from "./auth";

let cachedRaw: string | null = null;
let cachedSession: Session | null = null;

/**
 * Lê o valor bruto do cookie de sessão no navegador.
 *
 * @returns valor decodificado do cookie `agf_session` ou `null` quando ele não existir
 */
function readSessionCookie(): string | null {
	if (typeof document === "undefined") return null;

	const match = new RegExp(
		String.raw`(?:^|;\s*)${SESSION_COOKIE}=([^;]+)`,
	).exec(document.cookie);

	return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Retorna o snapshot atual da sessão.
 *
 * O resultado é cacheado pelo valor bruto do cookie para manter a mesma
 * referência enquanto a sessão não muda. Isso evita renderizações desnecessárias
 * e atende ao contrato do `useSyncExternalStore`.
 *
 * @returns sessão atual ou `null` quando o usuário não estiver autenticado
 */
function getSnapshot(): Session | null {
	const raw = readSessionCookie();

	if (raw !== cachedRaw) {
		cachedRaw = raw;
		cachedSession = parseSessionCookieValue(raw);
	}

	return cachedSession;
}

/**
 * Inscreve um listener para mudanças manuais de sessão.
 *
 * @param listener função chamada quando login ou logout alterar a sessão
 * @returns função usada pelo React para cancelar a inscrição
 */
function subscribe(listener: () => void): () => void {
	if (globalThis.window === undefined) return () => {};

	globalThis.addEventListener("agf:session", listener);
	return () => globalThis.removeEventListener("agf:session", listener);
}

/**
 * Retorna a sessão atual do usuário.
 *
 * No servidor, retorna `null`, pois a leitura direta de `document.cookie` só
 * existe no navegador. Após hidratação, o hook passa a refletir o cookie
 * `agf_session` e atualiza quando o evento `agf:session` é emitido.
 *
 * @returns sessão atual ou `null` quando o usuário não estiver autenticado
 */
export function useSession(): Session | null {
	return useSyncExternalStore(subscribe, getSnapshot, () => null);
}
