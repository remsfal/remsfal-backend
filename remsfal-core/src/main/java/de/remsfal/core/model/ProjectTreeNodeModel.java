package de.remsfal.core.model;

import java.util.List;

public interface ProjectTreeNodeModel {

    String getKey();

    NodeDataModel getData();

    List<ProjectTreeNodeModel> getChildren();
}
