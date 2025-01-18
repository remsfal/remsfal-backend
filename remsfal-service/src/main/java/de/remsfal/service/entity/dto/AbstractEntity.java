package de.remsfal.service.entity.dto;

import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class AbstractEntity extends AbstractMetaDataEntity {

    public abstract void setId(String id);
    
    public void generateId() {
        setId(UUID.randomUUID().toString());
    }

}
