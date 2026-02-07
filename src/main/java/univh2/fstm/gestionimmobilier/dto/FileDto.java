package univh2.fstm.gestionimmobilier.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FileDto extends AuditDto {

    private String uuid;

    private String name;
    private String downloadUri;
    private String type;
    private Long size;
}
