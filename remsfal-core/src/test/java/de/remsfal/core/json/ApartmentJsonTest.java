package de.remsfal.core.json;

import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.json.project.TenancyJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.TenancyModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApartmentJsonTest {

    @Test
    void testValueOf() {
        ApartmentModel apartmentModel = new ApartmentModelEntity("b9440c43-b5c0-4951-9c24-000000000001",
                "2 Zimmerwohnung 1.OG rechts",
                "1. OG rechts",
                "Frisch renoviert, Fliesen im Flur, Parkett im Wohnzimmer",
                77.36f, 0f, 77.36f,
                null); //null because method has to be tested in it's assigned test class

        ApartmentJson apartmentJson = ApartmentJson.valueOf(apartmentModel);
        String apartmentJsonString = "ApartmentJson{id=b9440c43-b5c0-4951-9c24-000000000001," +
                " title=2 Zimmerwohnung 1.OG rechts, location=1. OG rechts, description=Frisch renoviert," +
                " Fliesen im Flur, Parkett im Wohnzimmer, livingSpace=77.36, usableSpace=0.0," +
                " heatingSpace=77.36, tenancy=null}";

        assertEquals(apartmentJsonString, apartmentJson.toString());
    }

    class TenancyModelEntity implements TenancyModel {
        private String id;
        private LocalDate start;
        private LocalDate end;
        private List<? extends RentModel> rent;
        private CustomerModel tenant;

        public TenancyModelEntity(String id, LocalDate start, LocalDate end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getId() {
            return id;
        }

        public LocalDate getStartOfRental() {
            return start;
        }

        public LocalDate getEndOfRental() {
            return end;
        }

        public List<? extends RentModel> getRent() {
            return rent;
        }

        public CustomerModel getTenant() {
            return tenant;
        }
    }

    class ApartmentModelEntity implements ApartmentModel {
        private String id;
        private String title;
        private String location;
        private String description;
        private Float livingSpace;
        private Float usableSpace;
        private Float heatingSpace;
        private TenancyModel tenancy;

        public ApartmentModelEntity(String id, String title, String location, String description,
                                    Float livingSpace, Float usableSpace, Float heatingSpace, TenancyModel tenancy) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.description = description;
            this.livingSpace = livingSpace;
            this.usableSpace = usableSpace;
            this.heatingSpace = heatingSpace;
            this.tenancy = tenancy;
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

        @Override
        public TenancyModel getTenancy() {
            return tenancy;
        }
    }
}
