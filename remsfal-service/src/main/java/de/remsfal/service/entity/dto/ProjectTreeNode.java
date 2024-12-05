package de.remsfal.service.entity.dto;

import de.remsfal.core.model.NodeDataModel;
import de.remsfal.core.model.ProjectTreeNodeModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectTreeNode implements ProjectTreeNodeModel {
    private String key;
    private NodeDataModel data;
    private List<ProjectTreeNodeModel> children = new ArrayList<>();

    public ProjectTreeNode(String key, NodeData data) {
        this.key = key;
        this.data = data;
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

    public NodeDataModel getData() {
        return data;
    }

    public void setData(NodeDataModel data) {
        this.data = data;
    }

    public List<ProjectTreeNodeModel> getChildren() {
        return children;
    }
}
