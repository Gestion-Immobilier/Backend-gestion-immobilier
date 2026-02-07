package univh2.fstm.gestionimmobilier.mapper;

import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeLocationResponseDto;
import univh2.fstm.gestionimmobilier.model.DemandeLocation;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DemandeLocationMapper {

    // ========== Entity -> Response DTO ==========

    @Mapping(target = "bienId", source = "bien.id")
    @Mapping(target = "bienReference", source = "bien.reference")
    @Mapping(target = "bienAdresse", source = "bien.adresse")
    @Mapping(target = "bienVille", source = "bien.ville")
    @Mapping(target = "locataireId", source = "locataire.id")
    @Mapping(target = "locataireNom", expression = "java(getLocataireNom(demande))")
    @Mapping(target = "locataireEmail", source = "locataire.email")
    @Mapping(target = "locatairePhone", source = "locataire.phone")
    DemandeLocationResponseDto toResponseDto(DemandeLocation demande);

    List<DemandeLocationResponseDto> toResponseDto(List<DemandeLocation> demandes);

    // ========== Request DTO -> Entity ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "bien", ignore = true)
    @Mapping(target = "locataire", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "motifRefus", ignore = true)
    @Mapping(target = "dateTraitement", ignore = true)
    @Mapping(target = "contrat", ignore = true)
    DemandeLocation toEntity(DemandeLocationRequestDto dto);

    // ========== Helper ==========

    default String getLocataireNom(DemandeLocation demande) {
        if (demande.getLocataire() == null) {
            return null;
        }
        return demande.getLocataire().getFirstName() + " " + demande.getLocataire().getLastName();
    }
}