package de.remsfal.core.json;

import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.model.project.ApartmentModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApartmentJsonTest {

    @Test
    void testValueOf() {
        ApartmentModel apartmentModel = new ApartmentModelEntity("b9440c43-b5c0-4951-9c24-000000000001",
                "2 Zimmerwohnung 1.OG rechts",
                "1. OG rechts",
                "Frisch renoviert, Fliesen im Flur, Parkett im Wohnzimmer",
                77.36f, 0f, 77.36f);

        ApartmentJson apartmentJson = ApartmentJson.valueOf(apartmentModel);
        String apartmentJsonString = "ApartmentJson{type=APARTMENT, space=77.36, "
            + "id=b9440c43-b5c0-4951-9c24-000000000001, title=2 Zimmerwohnung 1.OG rechts, "
            + "location=1. OG rechts, description=Frisch renoviert, Fliesen im Flur, Parkett im Wohnzimmer, "
            + "livingSpace=77.36, usableSpace=0.0, heatingSpace=77.36}";

        assertEquals(apartmentJsonString, apartmentJson.toString());
        assertNotNull(apartmentJson);
        assertEquals(apartmentModel.getId(), apartmentJson.getId());
        assertEquals(apartmentModel.getTitle(), apartmentJson.getTitle());
        assertEquals(apartmentModel.getLocation(), apartmentJson.getLocation());
        assertEquals(apartmentModel.getDescription(), apartmentJson.getDescription());
        assertEquals(apartmentModel.getLivingSpace(), apartmentJson.getLivingSpace());
        assertEquals(apartmentModel.getUsableSpace(), apartmentJson.getUsableSpace());
        assertEquals(apartmentModel.getHeatingSpace(), apartmentJson.getHeatingSpace());
    }

    static class ApartmentModelEntity implements ApartmentModel {
        private final String id;
        private final String title;
        private final String location;
        private final String description;
        private final Float livingSpace;
        private final Float usableSpace;
        private final Float heatingSpace;

        public ApartmentModelEntity(String id, String title, String location, String description,
                                    Float livingSpace, Float usableSpace, Float heatingSpace) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.description = description;
            this.livingSpace = livingSpace;
            this.usableSpace = usableSpace;
            this.heatingSpace = heatingSpace;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getLocation() {
            return location;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Float getLivingSpace() {
            return livingSpace;
        }

        @Override
        public Float getUsableSpace() {
            return usableSpace;
        }

        @Override
        public Float getHeatingSpace() {
            return heatingSpace;
        }
    }
}
