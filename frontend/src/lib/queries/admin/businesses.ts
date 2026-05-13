/**
 * Hooks de administração para gerenciar estabelecimentos da plataforma.
 *
 * Todas as chamadas passam por `api()`, que usa o BFF para encaminhar a
 * requisição ao backend com o JWT lido do cookie `httpOnly`. No backend, esses
 * endpoints exigem papel `admin`.
 *
 * Não há mutation de criação neste arquivo, pois estabelecimentos são criados
 * no cadastro do dono. Aqui ficam apenas listagem, consulta, atualização,
 * desativação e reativação pela visão administrativa.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../../api";
import type { BusinessResponse, UpdateBusinessRequest } from "../../types";

const BASE = "/api/v1/admin/businesses";
const ROOT_KEY = ["admin", "businesses"] as const;

/**
 * Cria as options da query de listagem de estabelecimentos.
 *
 * @param activeOnly quando `true`, retorna apenas estabelecimentos ativos
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminBusinessesListOptions(activeOnly = false) {
	const qs = activeOnly ? "?activeOnly=true" : "";
	return queryOptions({
		queryKey: [...ROOT_KEY, "list", { activeOnly }] as const,
		queryFn: () => api<BusinessResponse[]>(`${BASE}${qs}`),
	});
}

/**
 * Cria as options da query de detalhe de um estabelecimento.
 *
 * @param id identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminBusinessDetailOptions(id: number) {
	return queryOptions({
		queryKey: [...ROOT_KEY, "detail", id] as const,
		queryFn: () => api<BusinessResponse>(`${BASE}/${id}`),
	});
}

/**
 * Mutation para atualizar um estabelecimento pela visão administrativa.
 *
 * @param id identificador do estabelecimento que será atualizado
 */
export function useAdminUpdateBusiness(id: number) {
	const qc = useQueryClient();
	return useMutation<BusinessResponse, Error, UpdateBusinessRequest>({
		mutationFn: (body) =>
			api<BusinessResponse>(`${BASE}/${id}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para desativar um estabelecimento.
 *
 * O id é recebido como variável da mutation para facilitar o uso em tabelas e
 * listas, onde cada linha possui um estabelecimento diferente.
 */
export function useAdminDeactivateBusiness() {
	const qc = useQueryClient();
	return useMutation<BusinessResponse, Error, number>({
		mutationFn: (id) =>
			api<BusinessResponse>(`${BASE}/${id}`, { method: "DELETE" }),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para reativar um estabelecimento previamente desativado.
 */
export function useAdminReactivateBusiness() {
	const qc = useQueryClient();
	return useMutation<BusinessResponse, Error, number>({
		mutationFn: (id) =>
			api<BusinessResponse>(`${BASE}/${id}/reactivate`, { method: "POST" }),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}
