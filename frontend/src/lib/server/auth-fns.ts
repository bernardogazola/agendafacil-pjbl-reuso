/**
 * Server functions de autenticação da camada BFF.
 *
 * Estas funções rodam apenas no servidor. Elas chamam o backend Quarkus,
 * recebem o `LoginResponse` e gravam o JWT no cookie `httpOnly` `agf_token`.
 * Assim, o token não fica acessível ao JavaScript do navegador.
 *
 * Além do token, é gravado o cookie legível `agf_session`, que contém apenas o
 * perfil básico do usuário. Esse perfil é usado pela interface e pelos guards
 * de rota.
 *
 * Erros de autenticação e validação são retornados como `{ ok: false, error }`,
 * preservando as mensagens em português vindas do backend.
 */
import { createServerFn } from "@tanstack/react-start";
import {
	deleteCookie,
	getRequestProtocol,
	setCookie,
} from "@tanstack/react-start/server";
import { SESSION_COOKIE, type Session, TOKEN_COOKIE, toSession } from "../auth";
import type {
	LoginRequest,
	LoginResponse,
	SignupCustomerRequest,
	SignupOwnerRequest,
} from "../types";
import { API_URL } from "./config";

/**
 * Tempo de vida dos cookies de sessão, em segundos.
 *
 * Mantém o mesmo tempo configurado para o JWT emitido pelo backend.
 */
const SESSION_MAX_AGE_SECONDS = 60 * 60;

/**
 * Resultado de uma operação de autenticação.
 *
 * Quando `ok` for `true`, a sessão foi criada e os cookies foram gravados.
 * Quando `ok` for `false`, `error` contém a mensagem que deve ser exibida ao
 * usuário.
 */
export type AuthResult =
	| { ok: true; session: Session }
	| { ok: false; error: string };

/**
 * Indica se os cookies devem ser gravados com a flag `secure`.
 *
 * @returns `true` quando a requisição atual usa HTTPS
 */
function secureCookies(): boolean {
	try {
		return getRequestProtocol() === "https";
	} catch {
		return false;
	}
}

/**
 * Remove os cookies usados pela sessão.
 */
function clearSessionCookies(): void {
	deleteCookie(TOKEN_COOKIE, { path: "/" });
	deleteCookie(SESSION_COOKIE, { path: "/" });
}

/**
 * Persiste a sessão autenticada em cookies.
 *
 * O JWT é salvo no cookie `httpOnly` `agf_token`. O perfil legível, sem o
 * token, é salvo em `agf_session` para uso pela interface.
 *
 * @param loginResponse resposta de autenticação recebida do backend
 * @returns perfil de sessão sem o token JWT
 */
function persistSession(loginResponse: LoginResponse): Session {
	const session = toSession(loginResponse);
	const secure = secureCookies();
	setCookie(TOKEN_COOKIE, loginResponse.token, {
		httpOnly: true,
		secure,
		sameSite: "lax",
		path: "/",
		maxAge: SESSION_MAX_AGE_SECONDS,
	});
	setCookie(SESSION_COOKIE, JSON.stringify(session), {
		httpOnly: false,
		secure,
		sameSite: "lax",
		path: "/",
		maxAge: SESSION_MAX_AGE_SECONDS,
	});
	return session;
}

/**
 * Executa uma chamada de autenticação no backend.
 *
 * A função é compartilhada entre login e cadastro. Em caso de sucesso, grava os
 * cookies de sessão. Em caso de erro, devolve a mensagem recebida do backend
 * sempre que possível.
 *
 * @param path endpoint de autenticação no backend
 * @param payload corpo da requisição
 * @returns resultado da autenticação
 */
async function authenticate(
	path: string,
	payload: unknown,
): Promise<AuthResult> {
	let res: Response;
	try {
		res = await fetch(`${API_URL}${path}`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Accept: "application/json",
			},
			body: JSON.stringify(payload),
		});
	} catch {
		return {
			ok: false,
			error: "Não foi possível contatar o servidor. Tente novamente.",
		};
	}

	const text = await res.text();
	let body: unknown = null;
	if (text) {
		try {
			body = JSON.parse(text);
		} catch {
			body = null;
		}
	}

	if (!res.ok) {
		return {
			ok: false,
			error:
				(body as { error?: string } | null)?.error ??
				"Não foi possível concluir a operação. Tente novamente.",
		};
	}

	return { ok: true, session: persistSession(body as LoginResponse) };
}

/**
 * Autentica um usuário existente e grava os cookies de sessão.
 */
export const loginFn = createServerFn({ method: "POST" })
	.inputValidator((d: LoginRequest) => d)
	.handler(({ data }) => authenticate("/api/v1/auth/login", data));

/**
 * Cadastra um novo cliente e grava os cookies de sessão.
 */
export const signupCustomerFn = createServerFn({ method: "POST" })
	.inputValidator((d: SignupCustomerRequest) => d)
	.handler(({ data }) => authenticate("/api/v1/auth/customer/signup", data));

/**
 * Cadastra um novo dono com estabelecimento e grava os cookies de sessão.
 */
export const signupOwnerFn = createServerFn({ method: "POST" })
	.inputValidator((d: SignupOwnerRequest) => d)
	.handler(({ data }) => authenticate("/api/v1/auth/owner/signup", data));

/**
 * Encerra a sessão do usuário removendo os cookies de autenticação.
 */
export const logoutFn = createServerFn({ method: "POST" }).handler(() => {
	clearSessionCookies();
});
