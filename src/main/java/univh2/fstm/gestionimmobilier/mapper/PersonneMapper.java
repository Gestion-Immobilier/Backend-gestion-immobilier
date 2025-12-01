package univh2.fstm.gestionimmobilier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import univh2.fstm.gestionimmobilier.dto.response.ProfileResponse;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.dto.response.PersonneDTO;


@Mapper(componentModel = "spring")
public interface PersonneMapper {

    // Convert DTO -> Entity
//    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", ignore = true)
    Personne toEntity(PersonneDTO dto);

    // Convert Entity -> Response DTO
    ProfileResponse toResponse(Personne personne);

}
