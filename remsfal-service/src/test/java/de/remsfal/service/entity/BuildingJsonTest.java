package de.remsfal.service.entity;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class BuildingJsonTest extends AbstractTest {
    @Test
    void testValueOfWithNullModel() {
        BuildingModel model = null;
        BuildingJson buildingJson = BuildingJson.valueOf(model);
        Assertions.assertNull(buildingJson);
    }

    @Test
    void testValueOfWithNullDescription() {
        BuildingModel model = mock(BuildingModel.class);
        when(model.getId()).thenReturn("123");
        when(model.getTitle()).thenReturn("Test Building");
        when(model.getAddress()).thenReturn(mock(AddressModel.class));
        when(model.getDescription()).thenReturn(null);
        when(model.getLivingSpace()).thenReturn(100.0f);
        when(model.getCommercialSpace()).thenReturn(50.0f);
        when(model.getUsableSpace()).thenReturn(150.0f);
        when(model.getHeatingSpace()).thenReturn(75.0f);
        when(model.isDifferentHeatingSpace()).thenReturn(true);

        BuildingJson buildingJson = BuildingJson.valueOf(model);
        Assertions.assertEquals("", buildingJson.getDescription());
    }

    @Test
    void testValueOfWithNullLivingSpace() {
        BuildingModel model = mock(BuildingModel.class);
        when(model.getId()).thenReturn("123");
        when(model.getTitle()).thenReturn("Test Building");
        when(model.getAddress()).thenReturn(mock(AddressModel.class));
        when(model.getDescription()).thenReturn("Test description");
        when(model.getLivingSpace()).thenReturn(null);
        when(model.getCommercialSpace()).thenReturn(50.0f);
        when(model.getUsableSpace()).thenReturn(150.0f);
        when(model.getHeatingSpace()).thenReturn(75.0f);
        when(model.isDifferentHeatingSpace()).thenReturn(true);
        BuildingJson buildingJson = BuildingJson.valueOf(model);
        Assertions.assertEquals(0.0f, buildingJson.getLivingSpace());
    }
}
