package univh2.fstm.gestionimmobilier.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import univh2.fstm.gestionimmobilier.dto.request.ReclamationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ReclamationResponseDto;
import univh2.fstm.gestionimmobilier.model.Reclamation;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ContratLightMapper.class}
)
public interface ReclamationMapper {

    // ============================
    // 1. Entité → Response DTO
    // ============================
    //@Mapping(source = "type", target = "typeReclamation")
    @Mapping(source = "contrat", target = "contrat")
    ReclamationResponseDto toResponseDto(Reclamation reclamation);

    // ============================
    // 2. Liste Entités → Liste Response DTO
    // ============================
    List<ReclamationResponseDto> toResponseDto(List<Reclamation> reclamations);

    // ============================
    // 3. Request DTO → Entité
    // ============================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contrat", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateResolution", ignore = true)
    @Mapping(target = "maintenanceTickets", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "downloadUri", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    //@Mapping(source = "typeReclamation", target = "type")
    Reclamation toEntity(ReclamationRequestDto requestDto);

    // ============================
    // 4. Liste Request DTO → Liste Entités
    // ============================
    List<Reclamation> toEntity(List<ReclamationRequestDto> requestDtos);
}
