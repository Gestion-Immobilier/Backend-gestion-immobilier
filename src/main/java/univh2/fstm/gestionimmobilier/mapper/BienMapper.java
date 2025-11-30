package univh2.fstm.gestionimmobilier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.model.Bien;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BienMapper {

    @Mapping(target = "proprietaireId", source = "proprietaire.id")
    @Mapping(target = "proprietaireNom", expression = "java(getProprietaireNom(bien))")
    @Mapping(target = "proprietaireEmail", source = "proprietaire.email")
    BienResponseDto toResponseDto(Bien bien);
    List<BienResponseDto> toResponseDto(List<Bien> biens);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "proprietaire", ignore = true)
    Bien toEntity(BienRequestDto requestDto);
    List<Bien> toEntity(List<BienRequestDto> requestDtos);

    // ========== Méthode Helper ==========
    default String getProprietaireNom(Bien bien) {
        if (bien.getProprietaire() == null) {
            return null;
        }
        return bien.getProprietaire().getFirstName() + " " + bien.getProprietaire().getLastName();
    }

}
