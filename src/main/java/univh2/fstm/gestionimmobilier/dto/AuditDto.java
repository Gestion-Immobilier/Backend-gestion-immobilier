package univh2.fstm.gestionimmobilier.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuditDto extends BaseDto {

    protected String createdBy;
    protected Timestamp createdOn;
    protected String updatedBy;
    protected Timestamp updatedOn;
}
