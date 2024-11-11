package de.remsfal.service.entity;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        project1.setId("1");
        user1.setId("1");
        user2.setId("2");

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
        project2.setId("2");
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
        entity3.setProject(project1);
        entity3.setUser(user2);
        entity3.setRole(ProjectMemberModel.UserRole.LESSOR);

        user2.setEmail("blabla");
        user2.setFirstName("Alex");
        user2.setLastName("Muster");

        String entity1String = "ProjectMembershipEntity: {id=2, email=blabla, name=Alex Muster, role=LESSOR}";
        assertEquals(entity1String, entity3.toString());
    }

    @Test
    @DisplayName("Tests if setId() throws IllegalArgumentException")
    public void testSetId () {
        assertThrows(IllegalArgumentException.class, () -> entity1.setId("test"));
    }
}