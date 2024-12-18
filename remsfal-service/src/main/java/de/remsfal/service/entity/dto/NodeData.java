package de.remsfal.service.entity.dto;

import de.remsfal.core.model.NodeDataModel;

public class NodeData implements NodeDataModel {
    private String type;
    private String title;
    private String description;
    private String tenant;
    private float usableSpace;

    public NodeData(String type, String title, String description, String tenant, float usableSpace) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.tenant = tenant;
        this.usableSpace = usableSpace;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(float usableSpace) {
        this.usableSpace = usableSpace;
    }
}
