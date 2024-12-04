package de.remsfal.service.entity.dto;

import de.remsfal.core.model.ProjectTreeNodeModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectTreeNode implements ProjectTreeNodeModel {
    private String key;
    private String type;
    private String title;
    private String description;
    private String tenant;
    private float usableSpace;
    private List<ProjectTreeNodeModel> children = new ArrayList<>();

    public ProjectTreeNode(
            String key,
            String type,
            String title,
            String description,
            String tenant,
            float usableSpace) {
        this.key = key;
        this.type = type;
        this.title = title;
        this.description = description;
        this.tenant = tenant;
        this.usableSpace = usableSpace;
    }

    public void addChild(ProjectTreeNode child) {
        this.children.add(child);
    }

    // Getters and Setters
    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @Override
    public float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(float usableSpace) {
        this.usableSpace = usableSpace;
    }

    public List<ProjectTreeNodeModel> getChildren() {
        return children;
    }
}

