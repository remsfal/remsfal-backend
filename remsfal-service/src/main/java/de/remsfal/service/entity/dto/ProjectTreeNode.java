package de.remsfal.service.entity.dto;

import de.remsfal.core.model.ProjectTreeNodeModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectTreeNode implements ProjectTreeNodeModel {
    private String key;
    private String type;
    private Object entity;
    private List<ProjectTreeNodeModel> children = new ArrayList<>();

    public ProjectTreeNode(String key, String type, Object entity) {
        this.key = key;
        this.type = type;
        this.entity = entity;
    }

    public void addChild(ProjectTreeNode child) {
        this.children.add(child);
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public Object getEntity() {
        return entity;
    }

    public List<ProjectTreeNodeModel> getChildren() {
        return children;
    }
}

