package de.remsfal.service.entity.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import de.remsfal.core.model.PropertyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROPERTY")
public class PropertyEntity extends AbstractEntity implements PropertyModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;
    
    @Column(name = "TITLE")
    private String title;
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getProjectId() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
