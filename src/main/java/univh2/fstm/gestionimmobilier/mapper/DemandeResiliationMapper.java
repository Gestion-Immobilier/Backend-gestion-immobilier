package univh2.fstm.gestionimmobilier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import univh2.fstm.gestionimmobilier.dto.request.DemandeResiliationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeResiliationResponseDto;
import univh2.fstm.gestionimmobilier.model.Contrat;
import univh2.fstm.gestionimmobilier.model.DemandeResiliation;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DemandeResiliationMapper {
    // 1. DTO → Entité
    // ============================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contrat", ignore = true)  // Sera géré dans le service
    @Mapping(target = "locataire", ignore = true)  // Sera géré dans le service
    @Mapping(target = "dateDemandeResiliation", ignore = true)  // Géré dans le service
    @Mapping(target = "statut", ignore = true)  // Géré dans le service
    @Mapping(target = "dateTraitement", ignore = true)
    @Mapping(target = "commentaireAdmin", ignore = true)
    @Mapping(target = "dateResiliationEffective", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DemandeResiliation toEntity(DemandeResiliationRequestDto dto);

    // ============================
    // 2. Entité → DTO
    // ============================

    @Mapping(source = "contrat.id", target = "contratId")
    @Mapping(source = "locataire.id", target = "locataireId")
    @Mapping(target = "locataireNom", expression = "java(getLocataireNom(entity))")
    @Mapping(source = "locataire.email", target = "locataireEmail")

    DemandeResiliationResponseDto toResponseDto(DemandeResiliation entity);

    // ============================
    // 3. Liste DTO → Liste Entités
    // ============================
    List<DemandeResiliation> toEntityList(List<DemandeResiliationRequestDto> dtoList);

    // ============================
    // 4. Liste Entités → Liste DTO
    // ============================
    List<DemandeResiliationResponseDto> toResponseDtoList(List<DemandeResiliation> entityList);

    default String getLocataireNom(DemandeResiliation entity) {
        if (entity.getLocataire() == null) {
            return null;
        }
        return entity.getLocataire().getFirstName() + " " + entity.getLocataire().getLastName();
    }
}
