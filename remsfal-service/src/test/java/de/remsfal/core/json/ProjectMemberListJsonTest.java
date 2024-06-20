package de.remsfal.core.json;

import de.remsfal.core.model.ProjectMemberModel;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMemberListJsonTest {

    @Test
    void testValueOfSet() {
        Set<ProjectMemberModel> models = new HashSet<>();
        ProjectMemberModel model1 = new EntityProjectMemberModel("1", "John", "john@example.com", ProjectMemberModel.UserRole.LESSOR);
        models.add(model1);
        ProjectMemberListJson result = ProjectMemberListJson.valueOfSet(models);
        assertEquals(1, result.getMembers().size());
    }

    static class EntityProjectMemberModel implements ProjectMemberModel {
        private String id;
        private String name;
        private String email;
        private UserRole role;
        public EntityProjectMemberModel(String id, String name, String email, UserRole role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
        @Override
        public String getId() {
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
        public UserRole getRole() {
            return role;
        }
    }
}

