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

    BienResponseDto toResponseDto(Bien bien);
    List<BienResponseDto> toResponseDto(List<Bien> biens);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Bien toEntity(BienRequestDto requestDto);
    List<Bien> toEntity(List<BienRequestDto> requestDtos);
}
