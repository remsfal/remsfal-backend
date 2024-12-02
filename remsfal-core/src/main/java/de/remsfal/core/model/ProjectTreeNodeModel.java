package de.remsfal.core.model;

import java.util.List;

public interface ProjectTreeNodeModel {

    String getKey();

    String getType();

    Object getEntity();

    List<ProjectTreeNodeModel> getChildren();
}
