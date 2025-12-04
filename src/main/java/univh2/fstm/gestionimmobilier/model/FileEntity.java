package univh2.fstm.gestionimmobilier.model;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class FileEntity extends AuditEntity {

    @Column(name = "UUID")
    private String uuid;

    @Column(name = "NAME",length = 1000)
    private String name;

    @Column(name = "DOWNLOAD_URI",length = 1000)
    private String downloadUri;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SIZE")
    private Long size;

}
