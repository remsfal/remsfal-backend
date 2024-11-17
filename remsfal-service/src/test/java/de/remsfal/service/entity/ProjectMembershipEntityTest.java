package de.remsfal.service.entity;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static de.remsfal.service.TestData.PROJECT_ID_1;
import static de.remsfal.service.TestData.PROJECT_ID_2;
import static de.remsfal.service.TestData.USER_EMAIL;
import static de.remsfal.service.TestData.USER_FIRST_NAME;
import static de.remsfal.service.TestData.USER_ID_1;
import static de.remsfal.service.TestData.USER_ID_2;
import static de.remsfal.service.TestData.USER_LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


@QuarkusTest
public class ProjectMembershipEntityTest {

    private ProjectMembershipEntity entity1;
    private ProjectMembershipEntity entity2;
    private ProjectEntity project1;
    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    //entity1 is equal to entity2
    public void setUp () {
        project1 = new ProjectEntity();
        user1 = new UserEntity();
        user2 = new UserEntity();
        project1.setId(PROJECT_ID_1);
        user1.setId(USER_ID_1);
        user2.setId(USER_ID_2);

        entity1 = new ProjectMembershipEntity();
        entity1.setProject(project1);
        entity1.setUser(user1);
        entity1.setRole(ProjectMemberModel.UserRole.MANAGER);

        entity2 = new ProjectMembershipEntity();
        entity2.setProject(project1);
        entity2.setUser(user1);
        entity2.setRole(ProjectMemberModel.UserRole.MANAGER);
    }

    //Tests if equality of ProjectMember objects is based on equality of project, user & role
    @Test
    @DisplayName("Tests two equal objects")
    public void testEqualsSameValues () {
        assertEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different project)")
    public void testEqualsDifferentProjects () {
        ProjectEntity project2 = new ProjectEntity();
        project2.setId(PROJECT_ID_2);
        entity2.setProject(project2);

        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different user)")
    public void testEqualsDifferentUsers () {
        entity2.setUser(user2);

        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Test two unequal objects (different role)")
    public void testEqualsDifferentRoles () {
        entity2.setRole(ProjectMemberModel.UserRole.LESSOR);

        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests if the correct String is being generated")
    public void testToString() {
        ProjectMembershipEntity entity3 = new ProjectMembershipEntity();
        entity3.setUser(user2);
        entity3.setRole(ProjectMemberModel.UserRole.LESSOR);

        user2.setEmail(USER_EMAIL);
        user2.setFirstName(USER_FIRST_NAME);
        user2.setLastName(USER_LAST_NAME);

        String expectedString = "ProjectMembershipEntity: {id="+entity3.getId()+", email="+user2.getEmail()+
                ", name="+user2.getName()+", role="+entity3.getRole()+"}";
        assertEquals(expectedString, entity3.toString());
    }

    @Test
    @DisplayName("Tests if setId() throws IllegalArgumentException if manually set")
    public void testSetId () {
        assertThrows(IllegalArgumentException.class, () -> entity1.setId("Id"));
    }
}