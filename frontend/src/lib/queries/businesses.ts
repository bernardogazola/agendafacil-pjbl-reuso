/**
 * Queries e mutations relacionadas a estabelecimentos.
 *
 * Inclui consultas públicas, como listagem de estabelecimentos e serviços, e
 * operações do dono autenticado sobre o próprio estabelecimento. Operações
 * administrativas ficam em `lib/queries/admin/businesses`.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	BusinessResponse,
	OfferedServiceResponse,
	UpdateBusinessRequest,
} from "../types";

/**
 * Cria as options da query de listagem pública de estabelecimentos.
 *
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const businessesListOptions = () =>
	queryOptions({
		queryKey: ["businesses", "list"] as const,
		queryFn: () => api<BusinessResponse[]>("/api/v1/businesses"),
	});

/**
 * Cria as options da query de serviços ativos de um estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const businessServicesOptions = (businessId: number) =>
	queryOptions({
		queryKey: ["business", businessId, "services"] as const,
		queryFn: () =>
			api<OfferedServiceResponse[]>(
				`/api/v1/businesses/${businessId}/services`,
			),
		enabled: Number.isFinite(businessId) && businessId > 0,
	});

/**
 * Cria as options da query de detalhe de um estabelecimento.
 *
 * Atualmente, o backend exige papel `owner` para este endpoint. Por isso, o uso
 * esperado é o dono consultando o próprio estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const businessDetailOptions = (businessId: number) =>
	queryOptions({
		queryKey: ["business", businessId, "detail"] as const,
		queryFn: () => api<BusinessResponse>(`/api/v1/businesses/${businessId}`),
		enabled: Number.isFinite(businessId) && businessId > 0,
	});

/**
 * Mutation para o dono atualizar o próprio estabelecimento.
 *
 * Após sucesso, invalida o cache do estabelecimento e a listagem pública.
 *
 * @param businessId identificador do estabelecimento que será atualizado
 */
export function useUpdateBusiness(businessId: number) {
	const qc = useQueryClient();
	return useMutation<BusinessResponse, Error, UpdateBusinessRequest>({
		mutationFn: (body) =>
			api<BusinessResponse>(`/api/v1/businesses/${businessId}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ["business", businessId] });
			qc.invalidateQueries({ queryKey: ["businesses", "list"] });
		},
	});
}
