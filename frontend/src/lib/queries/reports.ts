/**
 * Query de relatório de desempenho do estabelecimento.
 *
 * O relatório é gerado pelo backend para um período agregado, como diário,
 * semanal ou mensal. O endpoint exige papel `owner`.
 */
import { queryOptions } from "@tanstack/react-query";
import { api } from "../api";
import type { ReportPeriod, ReportResponse } from "../types";

/**
 * Cria as options da query de relatório.
 *
 * @param businessId identificador do estabelecimento
 * @param period período do relatório, como `DAILY`, `WEEKLY` ou `MONTHLY`
 * @param isoDate data de referência no formato `YYYY-MM-DD`
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const reportOptions = (
	businessId: number,
	period: ReportPeriod,
	isoDate: string,
) =>
	queryOptions({
		queryKey: ["reports", businessId, period, isoDate] as const,
		queryFn: () =>
			api<ReportResponse>(
				`/api/v1/businesses/${businessId}/reports?period=${period}&date=${encodeURIComponent(isoDate)}`,
			),
		enabled:
			Number.isFinite(businessId) &&
			businessId > 0 &&
			/^\d{4}-\d{2}-\d{2}$/.test(isoDate),
	});
