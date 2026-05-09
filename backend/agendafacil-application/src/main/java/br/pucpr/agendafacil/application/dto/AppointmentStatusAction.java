package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Ações que o dono do estabelecimento pode aplicar ao status de um agendamento.
 *
 * <p>O enum representa a ação solicitada pelo usuário, não diretamente o status
 * final. A validação da transição continua sendo responsabilidade da regra de
 * domínio do agendamento.</p>
 */
@Schema(description = "Ação de mudança de status de um agendamento")
public enum AppointmentStatusAction {

    /**
     * Confirma um agendamento que ainda está apenas marcado.
     */
    CONFIRM,

    /**
     * Marca um agendamento confirmado como concluído.
     */
    COMPLETE,

    /**
     * Marca um agendamento confirmado como não comparecido.
     */
    MARK_NO_SHOW
}