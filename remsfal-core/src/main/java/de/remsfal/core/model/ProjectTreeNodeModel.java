package de.remsfal.core.model;

import java.util.List;

public interface ProjectTreeNodeModel {

    String getKey();

    String getType();

    String getTitle();

    String getDescription();

    String getTenant();

    float getUsableSpace();

    List<ProjectTreeNodeModel> getChildren();
}
