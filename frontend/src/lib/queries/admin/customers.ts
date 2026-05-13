/**
 * Hooks de administração para gerenciar clientes da plataforma.
 *
 * Todas as chamadas passam por `api()`, que usa o BFF para encaminhar a
 * requisição ao backend com o JWT lido do cookie `httpOnly`. No backend, esses
 * endpoints exigem papel `admin`.
 *
 * Não há mutation de criação neste arquivo, pois clientes são criados pelo fluxo
 * público de cadastro. Aqui ficam apenas listagem, consulta, atualização,
 * desativação e reativação pela visão administrativa.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../../api";
import type { CustomerResponse, UpdateCustomerRequest } from "../../types";

const BASE = "/api/v1/admin/customers";
const ROOT_KEY = ["admin", "customers"] as const;

/**
 * Cria as options da query de listagem de clientes.
 *
 * @param activeOnly quando `true`, retorna apenas clientes ativos
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminCustomersListOptions(activeOnly = false) {
	const qs = activeOnly ? "?activeOnly=true" : "";
	return queryOptions({
		queryKey: [...ROOT_KEY, "list", { activeOnly }] as const,
		queryFn: () => api<CustomerResponse[]>(`${BASE}${qs}`),
	});
}

/**
 * Cria as options da query de detalhe de um cliente.
 *
 * @param id identificador do cliente
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminCustomerDetailOptions(id: number) {
	return queryOptions({
		queryKey: [...ROOT_KEY, "detail", id] as const,
		queryFn: () => api<CustomerResponse>(`${BASE}/${id}`),
	});
}

/**
 * Mutation para atualizar um cliente pela visão administrativa.
 *
 * @param id identificador do cliente que será atualizado
 */
export function useUpdateCustomer(id: number) {
	const qc = useQueryClient();
	return useMutation<CustomerResponse, Error, UpdateCustomerRequest>({
		mutationFn: (body) =>
			api<CustomerResponse>(`${BASE}/${id}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para desativar um cliente.
 *
 * O id é recebido como variável da mutation para facilitar o uso em tabelas e
 * listas, onde cada linha possui um cliente diferente.
 */
export function useDeactivateCustomer() {
	const qc = useQueryClient();
	return useMutation<CustomerResponse, Error, number>({
		mutationFn: (id) =>
			api<CustomerResponse>(`${BASE}/${id}`, { method: "DELETE" }),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para reativar um cliente previamente desativado.
 */
export function useReactivateCustomer() {
	const qc = useQueryClient();
	return useMutation<CustomerResponse, Error, number>({
		mutationFn: (id) =>
			api<CustomerResponse>(`${BASE}/${id}/reactivate`, { method: "POST" }),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}
