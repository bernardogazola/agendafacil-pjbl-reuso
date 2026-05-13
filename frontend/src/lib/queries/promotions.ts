/**
 * Queries e mutations de promoções do estabelecimento.
 *
 * Esses hooks são usados pelo dono para listar, consultar, criar, atualizar,
 * desativar e reativar promoções. Todos os endpoints exigem papel `owner` no
 * backend.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	CreatePromotionRequest,
	PromotionResponse,
	UpdatePromotionRequest,
} from "../types";

const baseFor = (businessId: number) =>
	`/api/v1/businesses/${businessId}/promotions`;

const promotionsKey = (businessId: number) =>
	["business", businessId, "promotions"] as const;

/**
 * Cria as options da query de promoções de um estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function promotionsListOptions(businessId: number) {
	return queryOptions({
		queryKey: promotionsKey(businessId),
		queryFn: () => api<PromotionResponse[]>(baseFor(businessId)),
		enabled: Number.isFinite(businessId) && businessId > 0,
	});
}

/**
 * Cria as options da query de detalhe de uma promoção.
 *
 * @param businessId identificador do estabelecimento
 * @param id identificador da promoção
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function promotionDetailOptions(businessId: number, id: number) {
	return queryOptions({
		queryKey: ["business", businessId, "promotions", id] as const,
		queryFn: () => api<PromotionResponse>(`${baseFor(businessId)}/${id}`),
		enabled:
			Number.isFinite(businessId) &&
			businessId > 0 &&
			Number.isFinite(id) &&
			id > 0,
	});
}

/**
 * Mutation para criar uma nova promoção.
 *
 * Após sucesso, invalida a lista de promoções do estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 */
export function useCreatePromotion(businessId: number) {
	const qc = useQueryClient();
	return useMutation<PromotionResponse, Error, CreatePromotionRequest>({
		mutationFn: (body) =>
			api<PromotionResponse>(baseFor(businessId), {
				method: "POST",
				body: JSON.stringify(body),
			}),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: promotionsKey(businessId) }),
	});
}

/**
 * Mutation para atualizar uma promoção existente.
 *
 * Após sucesso, invalida a lista e os detalhes de promoções do estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @param id identificador da promoção
 */
export function useUpdatePromotion(businessId: number, id: number) {
	const qc = useQueryClient();
	return useMutation<PromotionResponse, Error, UpdatePromotionRequest>({
		mutationFn: (body) =>
			api<PromotionResponse>(`${baseFor(businessId)}/${id}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: promotionsKey(businessId) }),
	});
}

/**
 * Mutation para desativar uma promoção.
 *
 * O id da promoção é recebido como variável da mutation para facilitar o uso em
 * listas e tabelas.
 *
 * @param businessId identificador do estabelecimento
 */
export function useDeactivatePromotion(businessId: number) {
	const qc = useQueryClient();
	return useMutation<PromotionResponse, Error, number>({
		mutationFn: (id) =>
			api<PromotionResponse>(`${baseFor(businessId)}/${id}`, {
				method: "DELETE",
			}),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: promotionsKey(businessId) }),
	});
}

/**
 * Mutation para reativar uma promoção previamente desativada.
 *
 * O id da promoção é recebido como variável da mutation para facilitar o uso em
 * listas e tabelas.
 *
 * @param businessId identificador do estabelecimento
 */
export function useReactivatePromotion(businessId: number) {
	const qc = useQueryClient();
	return useMutation<PromotionResponse, Error, number>({
		mutationFn: (id) =>
			api<PromotionResponse>(`${baseFor(businessId)}/${id}/reactivate`, {
				method: "POST",
			}),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: promotionsKey(businessId) }),
	});
}
