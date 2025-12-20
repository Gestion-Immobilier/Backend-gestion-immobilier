package univh2.fstm.gestionimmobilier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import univh2.fstm.gestionimmobilier.dto.request.MaintenanceTicketRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.MaintenanceTicketResponseDto;
import univh2.fstm.gestionimmobilier.model.MaintenanceTicket;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MaintenanceTicketMapper {

    // ============================
    // 1. Entity → Response DTO
    // ============================
    @Mapping(source = "reclamation.id", target = "reclamationId")
    @Mapping(source = "reclamation.type", target = "typeReclamation")
    @Mapping(source = "reclamation.priorite", target = "priorite")
    @Mapping(source = "reclamation.statut", target = "statutReclamation")
    MaintenanceTicketResponseDto toResponseDto(MaintenanceTicket ticket);

    // ============================
    // 2. List Entity → List DTO
    // ============================
    List<MaintenanceTicketResponseDto> toResponseDtoList(List<MaintenanceTicket> tickets);

    // ============================
    // 3. Request DTO → Entity
    // ============================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reclamation", ignore = true)
    @Mapping(target = "statut", ignore = true)

    // Audit (héritage AuditDto)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    MaintenanceTicket toEntity(MaintenanceTicketRequestDto requestDto);
}
