/**
 * Queries e mutations de avaliações.
 *
 * Reúne operações do cliente, como criar, listar, consultar, atualizar e
 * remover as próprias avaliações, e a consulta do dono às avaliações de um
 * estabelecimento.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	CreateReviewRequest,
	ReviewResponse,
	UpdateReviewRequest,
} from "../types";

/**
 * Cria as options da query de avaliações do cliente autenticado.
 *
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const myReviewsListOptions = () =>
	queryOptions({
		queryKey: ["reviews", "me"] as const,
		queryFn: () => api<ReviewResponse[]>("/api/v1/customers/me/reviews"),
	});

/**
 * Cria as options da query de avaliações de um estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const businessReviewsListOptions = (businessId: number) =>
	queryOptions({
		queryKey: ["business", businessId, "reviews"] as const,
		queryFn: () =>
			api<ReviewResponse[]>(`/api/v1/businesses/${businessId}/reviews`),
		enabled: Number.isFinite(businessId) && businessId > 0,
	});

/**
 * Cria as options da query de detalhe de uma avaliação.
 *
 * @param id identificador da avaliação
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const reviewDetailOptions = (id: number) =>
	queryOptions({
		queryKey: ["reviews", id] as const,
		queryFn: () => api<ReviewResponse>(`/api/v1/reviews/${id}`),
		enabled: Number.isFinite(id) && id > 0,
	});

/**
 * Mutation para criar uma avaliação de um agendamento concluído.
 *
 * Após sucesso, invalida a lista de avaliações do cliente.
 */
export function useCreateReview() {
	const qc = useQueryClient();
	return useMutation<
		ReviewResponse,
		Error,
		{ appointmentId: number; payload: CreateReviewRequest }
	>({
		mutationFn: ({ appointmentId, payload }) =>
			api<ReviewResponse>(`/api/v1/appointments/${appointmentId}/review`, {
				method: "POST",
				body: JSON.stringify(payload),
			}),
		onSuccess: () => qc.invalidateQueries({ queryKey: ["reviews", "me"] }),
	});
}

/**
 * Mutation para atualizar uma avaliação do cliente autenticado.
 *
 * Após sucesso, invalida o prefixo `["reviews"]`, atualizando a lista do
 * cliente e qualquer detalhe de avaliação em cache.
 *
 * @param id identificador da avaliação
 */
export function useUpdateReview(id: number) {
	const qc = useQueryClient();
	return useMutation<ReviewResponse, Error, UpdateReviewRequest>({
		mutationFn: (body) =>
			api<ReviewResponse>(`/api/v1/reviews/${id}`, {
				method: "PUT",
				body: JSON.stringify(body),
			}),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: ["reviews"] });
		},
	});
}

/**
 * Mutation para remover uma avaliação do cliente autenticado.
 *
 * O id da avaliação é recebido como variável da mutation para facilitar o uso
 * em listas e tabelas.
 */
export function useDeleteReview() {
	const qc = useQueryClient();
	return useMutation<void, Error, number>({
		mutationFn: (id) =>
			api<void>(`/api/v1/reviews/${id}`, { method: "DELETE" }),
		onSuccess: () => qc.invalidateQueries({ queryKey: ["reviews", "me"] }),
	});
}
