package univh2.fstm.gestionimmobilier.model;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class FileEntity extends AuditEntity {

    @Column(name = "UUID")
    private String uuid;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DOWNLOAD_URI")
    private String downloadUri;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SIZE")
    private Long size;

}
