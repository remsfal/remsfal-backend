package de.remsfal.core.json;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.project.ProjectMemberListJson;
import de.remsfal.core.model.project.ProjectMemberModel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectMemberListJsonTest {

    @Test
    void testValueOfSet() {
        Set<ProjectMemberModel> models = new HashSet<>();
        ProjectMemberModel model1 = new EntityProjectMemberModel(
            UUID.randomUUID(), "John", "john@example.com", true, ProjectMemberModel.MemberRole.LESSOR);
        models.add(model1);
        ProjectMemberListJson result = ProjectMemberListJson.valueOfSet(models);
        assertEquals(1, result.getMembers().size());
    }

    static class EntityProjectMemberModel implements ProjectMemberModel {
        private UUID id;
        private String name;
        private String email;
        private boolean isActive;
        private MemberRole role;
        public EntityProjectMemberModel(UUID id, String name, String email, boolean isActive, MemberRole role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.isActive = isActive;
            this.role = role;
        }
        @Override
        public UUID getId() {
            return id;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public String getEmail() {
            return email;
        }
        @Override
        public MemberRole getRole() {
            return role;
        }
        @Override
        public Boolean isActive() {
            return isActive;
        }
    }
}
