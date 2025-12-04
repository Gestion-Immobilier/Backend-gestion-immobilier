package univh2.fstm.gestionimmobilier.mapper;

import univh2.fstm.gestionimmobilier.dto.request.ContratRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ContratResponseDto;
import univh2.fstm.gestionimmobilier.model.Contrat;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContratMapper {

    // ========== Entity -> Response DTO ==========

    @Mapping(target = "bienId", source = "bien.id")
    @Mapping(target = "bienReference", source = "bien.reference")
    @Mapping(target = "bienAdresse", source = "bien.adresse")
    @Mapping(target = "bienVille", source = "bien.ville")
    @Mapping(target = "locataireId", source = "locataire.id")
    @Mapping(target = "locataireNom", expression = "java(getLocataireNom(contrat))")
    @Mapping(target = "locataireEmail", source = "locataire.email")
    @Mapping(target = "locatairePhone", source = "locataire.phone")
    @Mapping(target = "proprietaireId", source = "bien.proprietaire.id")
    @Mapping(target = "proprietaireNom", expression = "java(getProprietaireNom(contrat))")
    @Mapping(target = "proprietaireEmail", source = "bien.proprietaire.email")
    ContratResponseDto toResponseDto(Contrat contrat);

    List<ContratResponseDto> toResponseDto(List<Contrat> contrats);

    // ========== Request DTO -> Entity ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "bien", ignore = true)
    @Mapping(target = "locataire", ignore = true)
    @Mapping(target = "demandeLocation", ignore = true)
    @Mapping(target = "statut", ignore = true)
//    @Mapping(target = "paiements", ignore = true)
   // @Mapping(target = "demandesResiliation", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "downloadUri", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "size", ignore = true)
    Contrat toEntity(ContratRequestDto dto);

    // ========== Helpers ==========

    default String getLocataireNom(Contrat contrat) {
        if (contrat.getLocataire() == null) {
            return null;
        }
        return contrat.getLocataire().getFirstName() + " " + contrat.getLocataire().getLastName();
    }

    default String getProprietaireNom(Contrat contrat) {
        if (contrat.getBien() == null || contrat.getBien().getProprietaire() == null) {
            return null;
        }
        return contrat.getBien().getProprietaire().getFirstName() + " " +
                contrat.getBien().getProprietaire().getLastName();
    }
}