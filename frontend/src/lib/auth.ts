/**
 * Utilitários de sessão usados pelo frontend.
 *
 * <p>O token JWT não fica disponível para o JavaScript do navegador. Ele é
 * armazenado no cookie httpOnly `agf_token` e usado apenas pelo servidor
 * para se comunicar com o backend.</p>
 *
 * <p>Para a interface e para as verificações de rota, o frontend usa apenas um
 * perfil resumido da sessão, salvo no cookie `agf_session`. Esse perfil
 * não contém o token.</p>
 */
import type { LoginResponse } from "./types";

/**
 * Nome do cookie httpOnly que armazena o JWT.
 *
 * <p>Esse cookie deve ser lido apenas no servidor.</p>
 */
export const TOKEN_COOKIE = "agf_token";

/**
 * Nome do cookie legível usado pela interface para identificar a sessão atual.
 *
 * <p>Esse cookie guarda apenas dados básicos do usuário, sem o token JWT.</p>
 */
export const SESSION_COOKIE = "agf_session";

/**
 * Perfil de sessão exposto ao frontend.
 *
 * <p>Corresponde ao {@link LoginResponse} sem o campo `token`, para evitar
 * que o JWT fique disponível no JavaScript do navegador.</p>
 */
export type Session = Omit<LoginResponse, "token">;

/**
 * Converte a resposta de login em um perfil de sessão.
 *
 * @param loginResponse resposta recebida após autenticação
 * @returns dados da sessão sem o token JWT
 */
export function toSession(loginResponse: LoginResponse): Session {
	const { token: _token, ...session } = loginResponse;
	return session;
}

/**
 * Converte o valor bruto do cookie de sessão em um objeto {@link Session}.
 *
 * <p>Quando o cookie estiver ausente, inválido ou com formato inesperado, o
 * método retorna `null`.</p>
 *
 * @param raw valor bruto do cookie `agf_session`
 * @returns sessão convertida ou `null`
 */
export function parseSessionCookieValue(
	raw: string | null | undefined,
): Session | null {
	if (!raw) return null;

	try {
		const parsed = JSON.parse(raw) as Partial<Session>;

		if (
			typeof parsed?.userId !== "number" ||
			typeof parsed?.role !== "string"
		) {
			return null;
		}

		return parsed as Session;
	} catch {
		return null;
	}
}

/**
 * Notifica a interface que os dados de sessão foram alterados.
 *
 * <p>Usado após login e logout para que componentes inscritos, como o hook de
 * sessão, possam atualizar o estado exibido na tela.</p>
 */
export function notifySessionChange(): void {
	if (globalThis.window !== undefined) {
		globalThis.dispatchEvent(new Event("agf:session"));
	}
}
