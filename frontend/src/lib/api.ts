/**
 * Cliente HTTP usado pelo frontend para acessar a API.
 *
 * Em vez de chamar o backend Quarkus diretamente do navegador, esta função
 * delega a requisição para `serverApiFn`, que roda no servidor do TanStack
 * Start. Essa server function lê o JWT do cookie `httpOnly` `agf_token`,
 * adiciona o header `Authorization` e encaminha a chamada ao backend.
 *
 * Com isso, o token nunca fica disponível para o JavaScript do navegador.
 *
 * A função mantém um contrato simples para os chamadores: retorna o corpo
 * tipado em respostas `2xx`, retorna `undefined` em `204` e lança `ApiError`
 * quando a API responder com erro.
 */
import { serverApiFn } from "./server/api-proxy";
import type { ApiErrorBody } from "./types";

/**
 * Erro lançado quando a API retorna um status fora da faixa `2xx`.
 *
 * Além da mensagem, mantém o status HTTP e o corpo de erro padronizado
 * retornado pelo backend.
 */
export class ApiError extends Error {
	constructor(
		public status: number,
		public body: ApiErrorBody,
	) {
		super(body.error);
		this.name = "ApiError";
	}
}

/**
 * Verifica se um valor possui o formato mínimo de erro retornado pela API.
 *
 * @param value valor que será verificado
 * @returns `true` quando o valor contém uma mensagem de erro válida
 */
function isApiErrorBody(value: unknown): value is ApiErrorBody {
	return (
		typeof value === "object" &&
		value !== null &&
		typeof (value as { error?: unknown }).error === "string"
	);
}

/**
 * Cria um corpo de erro genérico para respostas inesperadas.
 *
 * @returns erro padronizado usado quando o backend não retorna um corpo válido
 */
const UNKNOWN_ERROR: () => ApiErrorBody = () => ({
	error: "Erro inesperado. Tente novamente.",
	code: "UNKNOWN",
	timestamp: new Date().toISOString(),
});

/**
 * Executa uma chamada para a API usando o proxy BFF.
 *
 * <p>O método envia apenas o caminho, o verbo HTTP e o corpo serializado para a
 * server function. A autenticação é resolvida no servidor, a partir do cookie
 * `httpOnly`.</p>
 *
 * <p>Quando a resposta for `401`, o método dispara o evento
 * `agf:unauthorized` para que a interface possa tratar a sessão expirada.</p>
 *
 * @param path caminho da API, normalmente iniciado por `/api/v1/`
 * @param init opções da requisição, como método HTTP e corpo
 * @returns corpo da resposta convertido para o tipo esperado
 * @throws ApiError quando a API retornar status fora da faixa `2xx`
 */
export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
	const { status, body } = await serverApiFn({
		data: {
			path,
			method: typeof init.method === "string" ? init.method : undefined,
			body: typeof init.body === "string" ? init.body : undefined,
		},
	});

	if (status === 204) return undefined as T;

	if (status < 200 || status >= 300) {
		if (status === 401 && globalThis.window !== undefined) {
			globalThis.dispatchEvent(new Event("agf:unauthorized"));
		}
		throw new ApiError(status, isApiErrorBody(body) ? body : UNKNOWN_ERROR());
	}

	return body as T;
}
