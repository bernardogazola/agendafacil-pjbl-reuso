package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.application.dto.BusinessResponse;
import br.pucpr.agendafacil.application.dto.OfferedServiceResponse;
import br.pucpr.agendafacil.application.mapper.BusinessMapper;
import br.pucpr.agendafacil.application.mapper.OfferedServiceMapper;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Resource público para descoberta de estabelecimentos e serviços.
 *
 * <p>Estes endpoints não exigem autenticação, pois são usados por clientes e
 * visitantes para consultar estabelecimentos ativos e seus serviços disponíveis
 * para agendamento.</p>
 */
@Path("/api/v1/businesses")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Negócios públicos", description = "Descoberta pública de estabelecimentos e serviços")
public class PublicBusinessResource {

    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final BusinessMapper businessMapper;
    private final OfferedServiceMapper offeredServiceMapper;

    @Inject
    public PublicBusinessResource(BusinessRepository businessRepository,
                                  OfferedServiceRepository offeredServiceRepository,
                                  BusinessMapper businessMapper,
                                  OfferedServiceMapper offeredServiceMapper) {
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.businessMapper = businessMapper;
        this.offeredServiceMapper = offeredServiceMapper;
    }

    /**
     * Lista os estabelecimentos ativos disponíveis na plataforma.
     *
     * @return lista de estabelecimentos ativos
     */
    @GET
    @PermitAll
    @Operation(
            summary = "Lista estabelecimentos ativos",
            description = "Retorna os estabelecimentos ativos disponíveis para descoberta pública."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de estabelecimentos ativos",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = BusinessResponse.class
            ))
    )
    public List<BusinessResponse> listActive() {
        return businessRepository.listActive().stream()
                .map(businessMapper::toResponse)
                .toList();
    }

    /**
     * Lista os serviços ativos de um estabelecimento.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de serviços ativos do estabelecimento
     */
    @GET
    @Path("/{businessId}/services")
    @PermitAll
    @Operation(
            summary = "Lista serviços ativos do estabelecimento",
            description = "Retorna os serviços ativos de um estabelecimento disponível para agendamento."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de serviços ativos",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = OfferedServiceResponse.class
            ))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado ou inativo",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<OfferedServiceResponse> listServices(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId) {
        businessRepository.findActiveById(businessId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado."));
        return offeredServiceRepository.findActiveByBusinessId(businessId).stream()
                .map(offeredServiceMapper::toResponse)
                .toList();
    }
}