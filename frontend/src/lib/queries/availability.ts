/**
 * Query de horários disponíveis para agendamento.
 *
 * O endpoint é público e retorna os slots livres de um serviço em uma data.
 * O cache usa `staleTime: 0`, pois a disponibilidade pode mudar rapidamente
 * quando outros clientes fazem reservas.
 */
import { queryOptions } from "@tanstack/react-query";
import { api } from "../api";
import type { AvailableSlotsResponse } from "../types";

/**
 * Cria as options da query de slots disponíveis.
 *
 * @param businessId identificador do estabelecimento
 * @param serviceId identificador do serviço
 * @param isoDate data no formato `YYYY-MM-DD`
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const availableSlotsOptions = (
	businessId: number,
	serviceId: number,
	isoDate: string,
) =>
	queryOptions({
		queryKey: ["slots", businessId, serviceId, isoDate] as const,
		queryFn: () =>
			api<AvailableSlotsResponse>(
				`/api/v1/businesses/${businessId}/services/${serviceId}/available-slots?date=${encodeURIComponent(isoDate)}`,
			),
		enabled:
			Number.isFinite(businessId) &&
			businessId > 0 &&
			Number.isFinite(serviceId) &&
			serviceId > 0 &&
			/^\d{4}-\d{2}-\d{2}$/.test(isoDate),
		staleTime: 0,
	});
