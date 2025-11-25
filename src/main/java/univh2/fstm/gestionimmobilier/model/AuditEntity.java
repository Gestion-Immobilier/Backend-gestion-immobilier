package univh2.fstm.gestionimmobilier.model;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@MappedSuperclass

@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity extends BaseEntity {

    @Column(name = "CREATED_BY", updatable = false)
    @CreatedBy
    protected String createdBy;

    @Column(name = "CREATED_ON", updatable = false)
    @CreatedDate
    protected Timestamp createdOn;

    @Column(name = "UPDATED_BY", insertable = false)
    @LastModifiedBy
    protected String updatedBy;

    @LastModifiedDate
    @Column(name = "UPDATED_ON", insertable = false)
    protected Timestamp updatedOn;
}
