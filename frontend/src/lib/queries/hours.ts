/**
 * Queries e mutations de horários de funcionamento.
 *
 * Esses hooks são usados pelo dono do estabelecimento para listar, criar,
 * atualizar e remover janelas de funcionamento. Todos os endpoints exigem papel
 * `owner` no backend.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	BusinessHoursDTO,
	BusinessHoursResponse,
	CreateBusinessHoursRequest,
	UpdateBusinessHoursEntryRequest,
	UpdateBusinessHoursRequest,
} from "../types";

const hoursKey = (businessId: number) =>
	["business", businessId, "hours"] as const;

/**
 * Cria as options da query de horários de funcionamento.
 *
 * @param businessId identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function businessHoursListOptions(businessId: number) {
	return queryOptions({
		queryKey: hoursKey(businessId),
		queryFn: () =>
			api<BusinessHoursResponse[]>(`/api/v1/businesses/${businessId}/hours`),
		enabled: Number.isFinite(businessId) && businessId > 0,
	});
}

/**
 * Mutation para atualizar horários em lote.
 *
 * Após sucesso, invalida a lista para buscar novamente os horários com seus ids.
 *
 * @param businessId identificador do estabelecimento
 */
export function useUpdateHours(businessId: number) {
	const qc = useQueryClient();
	return useMutation<BusinessHoursDTO[], Error, UpdateBusinessHoursRequest>({
		mutationFn: (body) =>
			api<BusinessHoursDTO[]>(`/api/v1/businesses/${businessId}/hours`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => qc.invalidateQueries({ queryKey: hoursKey(businessId) }),
	});
}

/**
 * Mutation para criar uma nova janela de funcionamento.
 *
 * @param businessId identificador do estabelecimento
 */
export function useCreateHour(businessId: number) {
	const qc = useQueryClient();
	return useMutation<BusinessHoursResponse, Error, CreateBusinessHoursRequest>({
		mutationFn: (body) =>
			api<BusinessHoursResponse>(`/api/v1/businesses/${businessId}/hours`, {
				method: "POST",
				body: JSON.stringify(body),
			}),
		onSuccess: () => qc.invalidateQueries({ queryKey: hoursKey(businessId) }),
	});
}

/**
 * Mutation para atualizar uma janela de funcionamento.
 *
 * @param businessId identificador do estabelecimento
 * @param hourId identificador da janela de funcionamento
 */
export function useUpdateHour(businessId: number, hourId: number) {
	const qc = useQueryClient();
	return useMutation<
		BusinessHoursResponse,
		Error,
		UpdateBusinessHoursEntryRequest
	>({
		mutationFn: (body) =>
			api<BusinessHoursResponse>(
				`/api/v1/businesses/${businessId}/hours/${hourId}`,
				{ method: "PUT", body: JSON.stringify(body) },
			),
		onSuccess: () => qc.invalidateQueries({ queryKey: hoursKey(businessId) }),
	});
}

/**
 * Mutation para remover uma janela de funcionamento.
 *
 * O `hourId` é recebido como variável da mutation para facilitar o uso em listas
 * e tabelas.
 *
 * @param businessId identificador do estabelecimento
 */
export function useDeleteHour(businessId: number) {
	const qc = useQueryClient();
	return useMutation<void, Error, number>({
		mutationFn: (hourId) =>
			api<void>(`/api/v1/businesses/${businessId}/hours/${hourId}`, {
				method: "DELETE",
			}),
		onSuccess: () => qc.invalidateQueries({ queryKey: hoursKey(businessId) }),
	});
}
