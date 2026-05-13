/**
 * Queries e mutations de serviços oferecidos.
 *
 * Este arquivo cobre as operações do dono sobre os serviços de um
 * estabelecimento. A listagem pública de serviços fica em `businessServicesOptions`,
 * no arquivo `businesses.ts`.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	CreateOfferedServiceRequest,
	OfferedServiceResponse,
	UpdateOfferedServiceRequest,
} from "../types";

const servicesKey = (businessId: number) =>
	["business", businessId, "services"] as const;

/**
 * Cria as options da query de detalhe de um serviço.
 *
 * @param businessId identificador do estabelecimento
 * @param serviceId identificador do serviço
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export function offeredServiceDetailOptions(
	businessId: number,
	serviceId: number,
) {
	return queryOptions({
		queryKey: ["business", businessId, "services", serviceId] as const,
		queryFn: () =>
			api<OfferedServiceResponse>(
				`/api/v1/businesses/${businessId}/services/${serviceId}`,
			),
		enabled:
			Number.isFinite(businessId) &&
			businessId > 0 &&
			Number.isFinite(serviceId) &&
			serviceId > 0,
	});
}

/**
 * Mutation para criar um novo serviço.
 *
 * Após sucesso, invalida o cache de serviços do estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 */
export function useCreateService(businessId: number) {
	const qc = useQueryClient();
	return useMutation<
		OfferedServiceResponse,
		Error,
		CreateOfferedServiceRequest
	>({
		mutationFn: (body) =>
			api<OfferedServiceResponse>(`/api/v1/businesses/${businessId}/services`, {
				method: "POST",
				body: JSON.stringify(body),
			}),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: servicesKey(businessId) }),
	});
}

/**
 * Mutation para atualizar um serviço existente.
 *
 * Após sucesso, invalida a lista e os detalhes de serviços do estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @param serviceId identificador do serviço
 */
export function useUpdateService(businessId: number, serviceId: number) {
	const qc = useQueryClient();
	return useMutation<
		OfferedServiceResponse,
		Error,
		UpdateOfferedServiceRequest
	>({
		mutationFn: (body) =>
			api<OfferedServiceResponse>(
				`/api/v1/businesses/${businessId}/services/${serviceId}`,
				{ method: "PUT", body: JSON.stringify(body) },
			),
		onSuccess: () => {
			qc.invalidateQueries({ queryKey: servicesKey(businessId) });
		},
	});
}

/**
 * Mutation para desativar um serviço.
 *
 * O id do serviço é recebido como variável da mutation para facilitar o uso em
 * listas e tabelas.
 *
 * @param businessId identificador do estabelecimento
 */
export function useDeactivateService(businessId: number) {
	const qc = useQueryClient();
	return useMutation<OfferedServiceResponse, Error, number>({
		mutationFn: (serviceId) =>
			api<OfferedServiceResponse>(
				`/api/v1/businesses/${businessId}/services/${serviceId}`,
				{ method: "DELETE" },
			),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: servicesKey(businessId) }),
	});
}

/**
 * Mutation para reativar um serviço previamente desativado.
 *
 * O id do serviço é recebido como variável da mutation para facilitar o uso em
 * listas e tabelas.
 *
 * @param businessId identificador do estabelecimento
 */
export function useReactivateService(businessId: number) {
	const qc = useQueryClient();
	return useMutation<OfferedServiceResponse, Error, number>({
		mutationFn: (serviceId) =>
			api<OfferedServiceResponse>(
				`/api/v1/businesses/${businessId}/services/${serviceId}/reactivate`,
				{ method: "POST" },
			),
		onSuccess: () =>
			qc.invalidateQueries({ queryKey: servicesKey(businessId) }),
	});
}
