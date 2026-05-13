/**
 * Queries e mutations relacionadas a agendamentos.
 *
 * Reúne operações do cliente, como criar, cancelar e listar os próprios
 * agendamentos, e operações do dono, como consultar a agenda do estabelecimento,
 * alterar status e reagendar.
 *
 * Todas as chamadas passam por `api()`, que encaminha a requisição pelo BFF com
 * o JWT lido do cookie `httpOnly`.
 */
import {
	queryOptions,
	useMutation,
	useQueryClient,
} from "@tanstack/react-query";
import { api } from "../api";
import type {
	AppointmentResponse,
	BookAppointmentRequest,
	CancellationResponse,
	RescheduleAppointmentRequest,
	UpdateAppointmentStatusRequest,
} from "../types";

/**
 * Cria as options da query dos agendamentos do cliente autenticado.
 *
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const myAppointmentsOptions = () =>
	queryOptions({
		queryKey: ["appointments", "me"] as const,
		queryFn: () => api<AppointmentResponse[]>("/api/v1/appointments/me"),
	});

/**
 * Cria as options da query de agendamentos de um estabelecimento.
 *
 * @param businessId identificador do estabelecimento
 * @param from data inicial no formato `YYYY-MM-DD`
 * @param to data final no formato `YYYY-MM-DD`
 * @returns options prontas para `useQuery` ou `useSuspenseQuery`
 */
export const businessAppointmentsOptions = (
	businessId: number,
	from: string,
	to: string,
) =>
	queryOptions({
		queryKey: ["appointments", "business", businessId, from, to] as const,
		queryFn: () =>
			api<AppointmentResponse[]>(
				`/api/v1/businesses/${businessId}/appointments?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
			),
		enabled:
			Number.isFinite(businessId) &&
			businessId > 0 &&
			/^\d{4}-\d{2}-\d{2}$/.test(from) &&
			/^\d{4}-\d{2}-\d{2}$/.test(to),
	});

/**
 * Mutation para criar um novo agendamento.
 *
 * Após sucesso, invalida a lista do cliente, os slots do serviço e a agenda do
 * estabelecimento envolvido.
 */
export function useBookAppointment() {
	const qc = useQueryClient();
	return useMutation<AppointmentResponse, Error, BookAppointmentRequest>({
		mutationFn: (body) =>
			api<AppointmentResponse>("/api/v1/appointments", {
				method: "POST",
				body: JSON.stringify(body),
			}),
		onSuccess: (appt) => {
			qc.invalidateQueries({ queryKey: ["appointments", "me"] });
			qc.invalidateQueries({
				queryKey: ["slots", appt.businessId, appt.serviceId],
			});
			qc.invalidateQueries({
				queryKey: ["appointments", "business", appt.businessId],
			});
		},
	});
}

/**
 * Mutation para cancelar um agendamento do cliente.
 *
 * O `businessId` é recebido junto com o id do agendamento para invalidar a
 * agenda e os slots relacionados ao estabelecimento correto.
 */
export function useCancelAppointment() {
	const qc = useQueryClient();
	return useMutation<
		CancellationResponse,
		Error,
		{ appointmentId: number; businessId: number }
	>({
		mutationFn: ({ appointmentId }) =>
			api<CancellationResponse>(`/api/v1/appointments/${appointmentId}`, {
				method: "DELETE",
			}),
		onSuccess: (_res, { businessId }) => {
			qc.invalidateQueries({ queryKey: ["appointments", "me"] });
			qc.invalidateQueries({ queryKey: ["slots", businessId] });
			qc.invalidateQueries({
				queryKey: ["appointments", "business", businessId],
			});
		},
	});
}

/**
 * Mutation para alterar o status de um agendamento pela visão do dono.
 *
 * As ações aceitas são `CONFIRM`, `COMPLETE` e `MARK_NO_SHOW`. Como a operação
 * não muda horário nem duração, apenas a agenda e a lista do cliente são
 * invalidadas.
 */
export function useChangeAppointmentStatus() {
	const qc = useQueryClient();
	return useMutation<
		AppointmentResponse,
		Error,
		{
			appointmentId: number;
			businessId: number;
			payload: UpdateAppointmentStatusRequest;
		}
	>({
		mutationFn: ({ appointmentId, payload }) =>
			api<AppointmentResponse>(`/api/v1/appointments/${appointmentId}/status`, {
				method: "PATCH",
				body: JSON.stringify(payload),
			}),
		onSuccess: (_res, { businessId }) => {
			qc.invalidateQueries({ queryKey: ["appointments", "me"] });
			qc.invalidateQueries({
				queryKey: ["appointments", "business", businessId],
			});
		},
	});
}

/**
 * Mutation para reagendar um agendamento pela visão do dono.
 *
 * Como o horário muda, a operação invalida a agenda, os slots disponíveis e a
 * lista do cliente.
 */
export function useRescheduleAppointment() {
	const qc = useQueryClient();
	return useMutation<
		AppointmentResponse,
		Error,
		{
			appointmentId: number;
			businessId: number;
			payload: RescheduleAppointmentRequest;
		}
	>({
		mutationFn: ({ appointmentId, payload }) =>
			api<AppointmentResponse>(
				`/api/v1/appointments/${appointmentId}/schedule`,
				{ method: "PATCH", body: JSON.stringify(payload) },
			),
		onSuccess: (_res, { businessId }) => {
			qc.invalidateQueries({ queryKey: ["appointments", "me"] });
			qc.invalidateQueries({ queryKey: ["slots", businessId] });
			qc.invalidateQueries({
				queryKey: ["appointments", "business", businessId],
			});
		},
	});
}
