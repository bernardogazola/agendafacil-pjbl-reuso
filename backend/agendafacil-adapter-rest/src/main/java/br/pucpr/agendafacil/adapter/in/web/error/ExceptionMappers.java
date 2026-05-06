package br.pucpr.agendafacil.adapter.in.web.error;

import br.pucpr.agendafacil.shared.exception.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

/**
 * Conjunto de mappers de exceção da API.
 *
 * <p>Cada mapper converte uma exceção específica em uma resposta HTTP com o
 * corpo padronizado {@link ApiError}. Isso evita que os resources precisem
 * tratar manualmente os mesmos erros em vários pontos da aplicação.</p>
 */
public class ExceptionMappers {

    private static final Logger log = Logger.getLogger(ExceptionMappers.class);

    /**
     * Converte erros de recurso não encontrado em HTTP 404.
     */
    @Provider
    public static class NotFoundMapper implements ExceptionMapper<NotFoundException> {
        @Override
        public Response toResponse(NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiError.of(ex.getMessage(), "NOT_FOUND"))
                    .build();
        }
    }

    /**
     * Converte conflitos de negócio em HTTP 409.
     */
    @Provider
    public static class ConflictMapper implements ExceptionMapper<ConflictException> {
        @Override
        public Response toResponse(ConflictException ex) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiError.of(ex.getMessage(), "CONFLICT"))
                    .build();
        }
    }

    /**
     * Converte erros de permissão em HTTP 403.
     */
    @Provider
    public static class ForbiddenMapper implements ExceptionMapper<ForbiddenException> {
        @Override
        public Response toResponse(ForbiddenException ex) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiError.of(ex.getMessage(), "FORBIDDEN"))
                    .build();
        }
    }

    /**
     * Converte falhas de autenticação em HTTP 401.
     */
    @Provider
    public static class UnauthorizedMapper implements ExceptionMapper<UnauthorizedException> {
        @Override
        public Response toResponse(UnauthorizedException ex) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiError.of(ex.getMessage(), "UNAUTHORIZED"))
                    .build();
        }
    }

    /**
     * Converte violações de regra de negócio em HTTP 422.
     */
    @Provider
    public static class BusinessRuleMapper implements ExceptionMapper<BusinessRuleException> {
        @Override
        public Response toResponse(BusinessRuleException ex) {
            return Response.status(422)
                    .entity(ApiError.of(ex.getMessage(), "UNPROCESSABLE_ENTITY"))
                    .build();
        }
    }

    /**
     * Converte estados inválidos da aplicação em HTTP 409.
     *
     * <p>Um exemplo é a tentativa de executar uma transição de status não permitida
     * em um agendamento.</p>
     */
    @Provider
    public static class IllegalStateMapper implements ExceptionMapper<IllegalStateException> {
        @Override
        public Response toResponse(IllegalStateException ex) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiError.of(ex.getMessage(), "ILLEGAL_STATE"))
                    .build();
        }
    }

    /**
     * Converte erros de validação em HTTP 400.
     *
     * <p>As mensagens das violações são agrupadas em uma única resposta para
     * facilitar a leitura pelo cliente da API.</p>
     */
    @Provider
    public static class ConstraintViolationMapper
            implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException ex) {
            String msg = ex.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.joining("; "));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiError.of(msg, "VALIDATION_ERROR"))
                    .build();
        }
    }

    /**
     * Trata exceções não mapeadas pelos outros mappers.
     *
     * <p>Exceções JAX-RS já possuem resposta própria e são preservadas. As demais
     * são registradas no log e retornam HTTP 500 com uma mensagem genérica.</p>
     */
    @Provider
    public static class GenericMapper implements ExceptionMapper<Throwable> {
        @Override
        public Response toResponse(Throwable ex) {
            if (ex instanceof jakarta.ws.rs.WebApplicationException wae) {
                return wae.getResponse();
            }
            log.error("Erro inesperado", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiError.of("Ocorreu um erro inesperado no servidor.", "INTERNAL_ERROR"))
                    .build();
        }
    }
}