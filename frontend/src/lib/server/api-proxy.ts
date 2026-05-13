/**
 * Proxy autenticado do BFF para chamadas ao backend.
 *
 * Esta server function roda apenas no servidor. Ela recebe uma chamada do
 * cliente, lê o JWT do cookie `httpOnly` `agf_token`, adiciona o header
 * `Authorization` e encaminha a requisição para o backend Quarkus.
 *
 * O token nunca fica disponível para o JavaScript do navegador. O retorno é
 * sempre normalizado como `{ status, body }`, inclusive em erros HTTP, para que
 * o cliente consiga tratar a resposta sem depender de exceções.
 *
 * Por segurança, o proxy só aceita caminhos iniciados por `/api/v1/`, só envia
 * requisições para `API_URL` e não repassa headers arbitrários do cliente.
 */
import { createServerFn } from "@tanstack/react-start";
import { deleteCookie, getCookie } from "@tanstack/react-start/server";
import { SESSION_COOKIE, TOKEN_COOKIE } from "../auth";
import type { ApiErrorBody } from "../types";
import { API_URL } from "./config";

/**
 * Dados recebidos pelo proxy.
 *
 * `path` deve ser um caminho relativo da API, começando com `/api/v1/`.
 * `body`, quando informado, já deve estar serializado em JSON.
 */
export interface ApiProxyInput {
	path: string;
	method?: string;
	body?: string;
}

/**
 * Resposta normalizada do proxy.
 *
 * `status` representa o status HTTP retornado pelo backend. `body` contém o
 * corpo JSON da resposta, ou `null` quando não houver corpo.
 *
 * O tipo de `body` é genérico de propósito, pois este proxy apenas repassa JSON.
 * A tipagem específica de cada endpoint fica nos chamadores, como `api<T>()` e
 * os módulos de queries.
 */
export interface ApiProxyResult {
	status: number;
	body: any;
}

/**
 * Monta um corpo de erro no mesmo formato usado pela API.
 *
 * @param error mensagem de erro em português
 * @param code código curto do erro
 * @returns corpo de erro padronizado
 */
function errorBody(error: string, code: string): ApiErrorBody {
	return { error, code, timestamp: new Date().toISOString() };
}

/**
 * Server function usada pelo cliente para acessar o backend com autenticação.
 */
export const serverApiFn = createServerFn({ method: "POST" })
	.inputValidator((d: ApiProxyInput) => {
		if (typeof d?.path !== "string" || !d.path.startsWith("/api/v1/")) {
			throw new Error("Caminho de API não permitido.");
		}
		return d;
	})
	.handler(async ({ data }): Promise<ApiProxyResult> => {
		const token = getCookie(TOKEN_COOKIE);

		let res: Response;
		try {
			res = await fetch(`${API_URL}${data.path}`, {
				method: data.method ?? "GET",
				headers: {
					"Content-Type": "application/json",
					Accept: "application/json",
					...(token ? { Authorization: `Bearer ${token}` } : {}),
				},
				body: data.body,
			});
		} catch {
			return {
				status: 503,
				body: errorBody(
					"Não foi possível contatar o servidor. Tente novamente.",
					"BACKEND_UNREACHABLE",
				),
			};
		}

		// Sessão inválida ou expirada. Como não há refresh token, o usuário deve refazer login.
		if (res.status === 401) {
			deleteCookie(TOKEN_COOKIE, { path: "/" });
			deleteCookie(SESSION_COOKIE, { path: "/" });
		}

		if (res.status === 204) {
			return { status: 204, body: null };
		}

		const text = await res.text();
		let body: unknown = null;
		if (text) {
			try {
				body = JSON.parse(text);
			} catch {
				body = errorBody("Resposta inválida do servidor.", "INVALID_RESPONSE");
			}
		}

		return { status: res.status, body };
	});
