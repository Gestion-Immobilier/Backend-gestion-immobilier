package univh2.fstm.gestionimmobilier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import univh2.fstm.gestionimmobilier.dto.response.ContratLightDto;
import univh2.fstm.gestionimmobilier.model.Contrat;

@Mapper(componentModel = "spring")
public interface ContratLightMapper {

    @Mapping(target = "bienId", source = "bien.id")
    @Mapping(target = "bienReference", source = "bien.reference")
    @Mapping(target = "bienAdresse", source = "bien.adresse")
    @Mapping(target = "bienVille", source = "bien.ville")
    @Mapping(target = "locataireId", source = "locataire.id")
    @Mapping(target = "locataireNom",
             expression = "java(contrat.getLocataire().getFirstName() + \" \" + contrat.getLocataire().getLastName())")
    @Mapping(target = "locataireEmail", source = "locataire.email")
    @Mapping(target = "locatairePhone", source = "locataire.phone")
    @Mapping(target = "proprietaireId", source = "bien.proprietaire.id")
    @Mapping(target = "proprietaireNom",
             expression = "java(contrat.getBien().getProprietaire().getFirstName() + \" \" + contrat.getBien().getProprietaire().getLastName())")
    @Mapping(target = "proprietaireEmail", source = "bien.proprietaire.email")
    ContratLightDto toLightDto(Contrat contrat);
}
