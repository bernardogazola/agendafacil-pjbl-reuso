/**
 * Hooks de administração para gerenciar administradores da plataforma.
 *
 * Todas as chamadas passam por `api()`, que usa o BFF para encaminhar a
 * requisição ao backend com o JWT lido do cookie `httpOnly`. No backend, esses
 * endpoints exigem papel `admin`.
 *
 * As chaves de cache seguem o prefixo `["admin", "administrators"]`. As
 * invalidações usam esse prefixo para atualizar listas e detalhes relacionados.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../../api";
import type {
	AdministratorResponse,
	CreateAdministratorRequest,
	UpdateAdministratorRequest,
} from "../../types";

const BASE = "/api/v1/admin/administrators";
const ROOT_KEY = ["admin", "administrators"] as const;

/**
 * Cria as options da query de listagem de administradores.
 *
 * @param activeOnly quando `true`, retorna apenas administradores ativos
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminAdministratorsListOptions(activeOnly = false) {
	const qs = activeOnly ? "?activeOnly=true" : "";
	return queryOptions({
		queryKey: [...ROOT_KEY, "list", { activeOnly }] as const,
		queryFn: () => api<AdministratorResponse[]>(`${BASE}${qs}`),
	});
}

/**
 * Cria as options da query de detalhe de um administrador.
 *
 * @param id identificador do administrador
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function adminAdministratorDetailOptions(id: number) {
	return queryOptions({
		queryKey: [...ROOT_KEY, "detail", id] as const,
		queryFn: () => api<AdministratorResponse>(`${BASE}/${id}`),
	});
}

/**
 * Mutation para criar um novo administrador.
 *
 * Após sucesso, invalida as queries de administradores para atualizar listas e
 * detalhes relacionados.
 */
export function useCreateAdministrator() {
	const qc = useQueryClient();
	return useMutation<AdministratorResponse, Error, CreateAdministratorRequest>({
		mutationFn: (body) =>
			api<AdministratorResponse>(BASE, {
				method: "POST",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para atualizar um administrador existente.
 *
 * @param id identificador do administrador que será atualizado
 */
export function useUpdateAdministrator(id: number) {
	const qc = useQueryClient();
	return useMutation<AdministratorResponse, Error, UpdateAdministratorRequest>({
		mutationFn: (body) =>
			api<AdministratorResponse>(`${BASE}/${id}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para desativar um administrador.
 *
 * O id é recebido como variável da mutation para facilitar o uso em tabelas e
 * listas, onde cada linha possui um administrador diferente.
 */
export function useDeactivateAdministrator() {
	const qc = useQueryClient();
	return useMutation<AdministratorResponse, Error, number>({
		mutationFn: (id) =>
			api<AdministratorResponse>(`${BASE}/${id}`, { method: "DELETE" }),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}

/**
 * Mutation para reativar um administrador previamente desativado.
 */
export function useReactivateAdministrator() {
	const qc = useQueryClient();
	return useMutation<AdministratorResponse, Error, number>({
		mutationFn: (id) =>
			api<AdministratorResponse>(`${BASE}/${id}/reactivate`, {
				method: "POST",
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ROOT_KEY });
		},
	});
}
