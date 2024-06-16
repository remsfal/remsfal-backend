package de.remsfal.service;

import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.ImmutableApartmentJson;
import de.remsfal.core.json.ImmutableBuildingJson;
import de.remsfal.core.json.ImmutableCommercialJson;
import de.remsfal.core.json.ImmutableGarageJson;
import de.remsfal.core.json.ImmutablePropertyJson;
import de.remsfal.core.json.ImmutableSiteJson;

public class TestData {

    // Default test user
    public static final String USER_ID = TestData.USER_ID_1;
    public static final String USER_TOKEN = TestData.USER_TOKEN_1;
    public static final String USER_EMAIL = TestData.USER_EMAIL_1;
    public static final String USER_FIRST_NAME = TestData.USER_FIRST_NAME_1;
    public static final String USER_LAST_NAME = TestData.USER_LAST_NAME_1;
    
    // Test user 1
    public static final String USER_ID_1 = "b9440c43-b5c0-4951-9c29-000000000001";
    public static final String USER_TOKEN_1 = "tokenOfMaxMustermann";
    public static final String USER_EMAIL_1 = "max.mustermann@example.org";
    public static final String USER_FIRST_NAME_1 = "Max";
    public static final String USER_LAST_NAME_1 = "Mustermann";

    // Test user 2
    public static final String USER_ID_2 = "b9440c43-b5c0-4951-9c29-000000000002";
    public static final String USER_TOKEN_2 = "tokenOfLieschenMueller";
    public static final String USER_EMAIL_2 = "lieschen.mueller@example.org";
    public static final String USER_FIRST_NAME_2 = "Lieschen";
    public static final String USER_LAST_NAME_2 = "Müller";

    // Test user 3
    public static final String USER_ID_3 = "b9440c43-b5c0-4951-9c29-000000000003";
    public static final String USER_TOKEN_3 = "tokenOfOttoNormalverbraucher";
    public static final String USER_EMAIL_3 = "otto.normalverbraucher@example.org";
    public static final String USER_FIRST_NAME_3 = "Otto";
    public static final String USER_LAST_NAME_3 = "Normalverbraucher";

    // Test user 4
    public static final String USER_ID_4 = "b9440c43-b5c0-4951-9c29-000000000004";
    public static final String USER_TOKEN_4 = "tokenOfLassmirandaDennsiewillja";
    public static final String USER_EMAIL_4 = "l.m.r.d-d.s.w.j@simsons.org";
    public static final String USER_FIRST_NAME_4 = "Lassmiranda";
    public static final String USER_LAST_NAME_4 = "Dennsiewillja";

    // Default test address
    public static final String ADDRESS_STREET = TestData.ADDRESS_STREET_1;
    public static final String ADDRESS_CITY = TestData.ADDRESS_CITY_1;
    public static final String ADDRESS_PROVINCE = TestData.ADDRESS_PROVINCE_1;
    public static final String ADDRESS_ZIP = TestData.ADDRESS_ZIP_1;
    public static final String ADDRESS_COUNTRY = TestData.ADDRESS_COUNTRY_1;
    
    public static final ImmutableAddressJson.Builder addressBuilder() {
        return addressBuilder1();
    }

    // Test address 1
    public static final String ADDRESS_STREET_1 = "Maximiliankorso 9901";
    public static final String ADDRESS_CITY_1 = "Berlin";
    public static final String ADDRESS_PROVINCE_1 = "Berlin";
    public static final String ADDRESS_ZIP_1 = "13467";
    public static final String ADDRESS_COUNTRY_1 = "DE";
    
    public static final ImmutableAddressJson.Builder addressBuilder1() {
        return ImmutableAddressJson
            .builder()
            .street(ADDRESS_STREET_1)
            .city(ADDRESS_CITY_1)
            .province(ADDRESS_PROVINCE_1)
            .zip(ADDRESS_ZIP_1)
            .countryCode(ADDRESS_COUNTRY_1);
    }

    // Default test project
    public static final String PROJECT_ID = TestData.PROJECT_ID_1;
    public static final String PROJECT_TITLE = TestData.PROJECT_TITLE_1;
    
    // Test project 1
    public static final String PROJECT_ID_1 = "b9440c43-b5c0-4951-9c28-000000000001";
    public static final String PROJECT_TITLE_1 = "Wohnpark am Nieder Neuendorfer See";

    // Test project 2
    public static final String PROJECT_ID_2 = "b9440c43-b5c0-4951-9c28-000000000002";
    public static final String PROJECT_TITLE_2 = "Mehrfamilienhaus in Friedrichshain";

    // Test project 3
    public static final String PROJECT_ID_3 = "b9440c43-b5c0-4951-9c28-000000000003";
    public static final String PROJECT_TITLE_3 = "Häuser der Familie Müller";

    // Test project 4
    public static final String PROJECT_ID_4 = "b9440c43-b5c0-4951-9c28-000000000004";
    public static final String PROJECT_TITLE_4 = "Schall und Rauch GmbH & Co. KG";

    // Test project 5
    public static final String PROJECT_ID_5 = "b9440c43-b5c0-4951-9c28-000000000005";
    public static final String PROJECT_TITLE_5 = "Eigentümergemeinschaft Bundesallee 88 / Berliner Straße 69";

    // Default test property
    public static final String PROPERTY_ID = TestData.PROPERTY_ID_1;
    public static final String PROPERTY_TITLE = TestData.PROPERTY_TITLE_1;
    public static final String PROPERTY_REG_ENTRY = TestData.PROPERTY_REG_ENTRY_1;
    public static final String PROPERTY_DESCRIPTION = TestData.PROPERTY_DESCRIPTION_1;
    public static final Integer PROPERTY_PLOT_AREA = TestData.PROPERTY_PLOT_AREA_1;
    
    public static final ImmutablePropertyJson.Builder propertyBuilder() {
        return propertyBuilder1();
    }
    
    // Test property 1
    public static final String PROPERTY_ID_1 = "b9440c43-b5c0-4951-9c27-000000000001";
    public static final String PROPERTY_TITLE_1 = "Kleinesiedlung";
    public static final String PROPERTY_REG_ENTRY_1 = "Amtsgericht Schönestadt, Grundbuch von Kleinesiedlung, Blatt 4711";
    public static final String PROPERTY_DESCRIPTION_1 = "Example description of Kleinesiedlung";
    public static final Integer PROPERTY_PLOT_AREA_1 = 1234;
    
    public static final ImmutablePropertyJson.Builder propertyBuilder1() {
        return ImmutablePropertyJson
            .builder()
            .id(PROPERTY_ID_1)
            .title(PROPERTY_TITLE_1)
            .landRegisterEntry(PROPERTY_REG_ENTRY_1)
            .description(PROPERTY_DESCRIPTION_1)
            .plotArea(PROPERTY_PLOT_AREA_1);
    }

    // Default test site
    public static final String SITE_ID = TestData.SITE_ID_1;
    public static final String SITE_TITLE = TestData.SITE_TITLE_1;
    public static final String SITE_DESCRIPTION = TestData.SITE_DESCRIPTION_1;
    public static final Float SITE_USABLE_SPACE = TestData.SITE_USABLE_SPACE_1;
    public static final Float SITE_RENT = TestData.SITE_RENT_1;

    public static final ImmutableSiteJson.Builder siteBuilder() {
        return siteBuilder1();
    }

    // Test site 1
    public static final String SITE_ID_1 = "b9440c43-b5c0-4951-9c26-000000000001";
    public static final String SITE_TITLE_1 = "PKW Stellplatz";
    public static final String SITE_DESCRIPTION_1 = "Stellplatz mit Carport";
    public static final Float SITE_USABLE_SPACE_1 = 13.4f;
    public static final Float SITE_RENT_1 = 75f;
    
    public static final ImmutableSiteJson.Builder siteBuilder1() {
        return ImmutableSiteJson
            .builder()
            .id(SITE_ID_1)
            .title(SITE_TITLE_1)
            .description(SITE_DESCRIPTION_1)
            .usableSpace(SITE_USABLE_SPACE_1)
            .rent(SITE_RENT_1);
    }

    // Default test building
    public static final String BUILDING_ID = TestData.BUILDING_ID_1;
    public static final String BUILDING_TITLE = TestData.BUILDING_TITLE_1;
    public static final String BUILDING_DESCRIPTION = TestData.BUILDING_DESCRIPTION_1;
    public static final Float BUILDING_LIVING_SPACE = TestData.BUILDING_LIVING_SPACE_1;
    public static final Float BUILDING_COMMERCIAL_SPACE = TestData.BUILDING_COMMERCIAL_SPACE_1;
    public static final Float BUILDING_USABLE_SPACE = TestData.BUILDING_USABLE_SPACE_1;
    public static final Float BUILDING_HEATING_SPACE = TestData.BUILDING_HEATING_SPACE_1;
    public static final Float BUILDING_RENT = TestData.BUILDING_RENT_1;

    public static final ImmutableBuildingJson.Builder buildingBuilder() {
        return buildingBuilder1();
    }

    // Test building 1
    public static final String BUILDING_ID_1 = "b9440c43-b5c0-4951-9c25-000000000001";
    public static final String BUILDING_TITLE_1 = "Maximiliankorso (Fam. Rudolf)";
    public static final String BUILDING_DESCRIPTION_1 = "Flachbau mit zwei Stockwerken";
    public static final Float BUILDING_LIVING_SPACE_1 = 87.46f;
    public static final Float BUILDING_COMMERCIAL_SPACE_1 = 103.22f;
    public static final Float BUILDING_USABLE_SPACE_1 = 53.9f;
    public static final Float BUILDING_HEATING_SPACE_1 = 103.22f;
    public static final Float BUILDING_RENT_1 = 759.50f;

    public static final ImmutableBuildingJson.Builder buildingBuilder1() {
        return ImmutableBuildingJson
            .builder()
            .id(TestData.BUILDING_ID_1)
            .title(TestData.BUILDING_TITLE_1)
            .description(TestData.BUILDING_DESCRIPTION_1)
            .livingSpace(TestData.BUILDING_LIVING_SPACE_1)
            .commercialSpace(TestData.BUILDING_COMMERCIAL_SPACE_1)
            .usableSpace(TestData.BUILDING_USABLE_SPACE_1)
            .heatingSpace(TestData.BUILDING_HEATING_SPACE_1)
            .rent(TestData.BUILDING_RENT_1);
    }

    // Default test apartment
    public static final String APARTMENT_ID = TestData.APARTMENT_ID_1;
    public static final String APARTMENT_TITLE = TestData.APARTMENT_TITLE_1;
    public static final String APARTMENT_LOCATION = TestData.APARTMENT_LOCATION_1;
    public static final String APARTMENT_DESCRIPTION = TestData.APARTMENT_DESCRIPTION_1;
    public static final Float APARTMENT_LIVING_SPACE = TestData.APARTMENT_LIVING_SPACE_1;
    public static final Float APARTMENT_USABLE_SPACE = TestData.APARTMENT_USABLE_SPACE_1;
    public static final Float APARTMENT_HEATING_SPACE = TestData.APARTMENT_HEATING_SPACE_1;
    public static final Float APARTMENT_RENT = TestData.APARTMENT_RENT_1;

    public static final ImmutableApartmentJson.Builder apartmentBuilder() {
        return apartmentBuilder1();
    }
    
    // Test apartment 1
    public static final String APARTMENT_ID_1 = "b9440c43-b5c0-4951-9c24-000000000001";
    public static final String APARTMENT_TITLE_1 = "2 Zimmerwohnung 1.OG rechts";
    public static final String APARTMENT_LOCATION_1 = "1. OG rechts";
    public static final String APARTMENT_DESCRIPTION_1 = "Frisch renoviert, Fliesen im Flur, Parkett im Wohnzimmer";
    public static final Float APARTMENT_LIVING_SPACE_1 = 77.36f;
    public static final Float APARTMENT_USABLE_SPACE_1 = 0f;
    public static final Float APARTMENT_HEATING_SPACE_1 = 77.36f;
    public static final Float APARTMENT_RENT_1 = 498.90f;

    public static final ImmutableApartmentJson.Builder apartmentBuilder1() {
        return ImmutableApartmentJson
        .builder()
        .id(APARTMENT_ID_1)
        .title(APARTMENT_TITLE_1)
        .location(APARTMENT_LOCATION_1)
        .description(APARTMENT_DESCRIPTION_1)
        .livingSpace(APARTMENT_LIVING_SPACE_1)
        .usableSpace(APARTMENT_USABLE_SPACE_1)
        .heatingSpace(APARTMENT_HEATING_SPACE_1)
        .rent(APARTMENT_RENT_1);
    }

    // Test apartment 1
    public static final String APARTMENT_ID_2 = "b9440c43-b5c0-4951-9c24-000000000002";
    public static final String APARTMENT_TITLE_2 = "3 Zimmerwohnung 1.OG links";
    public static final String APARTMENT_LOCATION_2 = "1. OG links";
    public static final String APARTMENT_DESCRIPTION_2 = "2015 renoviert, Fliesen im Flur, Laminat im Wohnzimmer";
    public static final Float APARTMENT_LIVING_SPACE_2 = 87.36f;
    public static final Float APARTMENT_USABLE_SPACE_2 = 0f;
    public static final Float APARTMENT_HEATING_SPACE_2 = 87.36f;
    public static final Float APARTMENT_RENT_2 = 598.90f;

    public static final ImmutableApartmentJson.Builder apartmentBuilder2() {
        return ImmutableApartmentJson
        .builder()
        .id(APARTMENT_ID_2)
        .title(APARTMENT_TITLE_2)
        .location(APARTMENT_LOCATION_2)
        .description(APARTMENT_DESCRIPTION_2)
        .livingSpace(APARTMENT_LIVING_SPACE_2)
        .usableSpace(APARTMENT_USABLE_SPACE_2)
        .heatingSpace(APARTMENT_HEATING_SPACE_2)
        .rent(APARTMENT_RENT_2);
    }

    // Default test commercial
    public static final String COMMERCIAL_ID = TestData.COMMERCIAL_ID_1;
    public static final String COMMERCIAL_TITLE = TestData.COMMERCIAL_TITLE_1;
    public static final String COMMERCIAL_LOCATION = TestData.COMMERCIAL_LOCATION_1;
    public static final String COMMERCIAL_DESCRIPTION = TestData.COMMERCIAL_DESCRIPTION_1;
    public static final Float COMMERCIAL_COMMERCIAL_SPACE = TestData.COMMERCIAL_COMMERCIAL_SPACE_1;
    public static final Float COMMERCIAL_USABLE_SPACE = TestData.COMMERCIAL_USABLE_SPACE_1;
    public static final Float COMMERCIAL_HEATING_SPACE = TestData.COMMERCIAL_HEATING_SPACE_1;
    public static final Float COMMERCIAL_RENT = TestData.COMMERCIAL_RENT_1;
    
    public static final ImmutableCommercialJson.Builder commercialBuilder() {
        return commercialBuilder1();
    }

    // Test commercial 1
    public static final String COMMERCIAL_ID_1 = "b9440c43-b5c0-4951-9c23-000000000001";
    public static final String COMMERCIAL_TITLE_1 = "Bäckerei Lempke";
    public static final String COMMERCIAL_LOCATION_1 = "EG links";
    public static final String COMMERCIAL_DESCRIPTION_1 = "Bäckerei mit Tischen vor dem Haus";
    public static final Float COMMERCIAL_COMMERCIAL_SPACE_1 = 423.92f;
    public static final Float COMMERCIAL_USABLE_SPACE_1 = 53.9f;
    public static final Float COMMERCIAL_HEATING_SPACE_1 = 204.27f;
    public static final Float COMMERCIAL_RENT_1 = 3799.80f;

    public static final ImmutableCommercialJson.Builder commercialBuilder1() {
        return ImmutableCommercialJson
        .builder()
        .id(COMMERCIAL_ID_1)
        .title(COMMERCIAL_TITLE_1)
        .location(COMMERCIAL_LOCATION_1)
        .description(COMMERCIAL_DESCRIPTION_1)
        .commercialSpace(COMMERCIAL_COMMERCIAL_SPACE_1)
        .usableSpace(COMMERCIAL_USABLE_SPACE_1)
        .heatingSpace(COMMERCIAL_HEATING_SPACE_1)
        .rent(COMMERCIAL_RENT_1);
    }

    // Default test garage
    public static final String GARAGE_ID = TestData.GARAGE_ID_1;
    public static final String GARAGE_TITLE = TestData.GARAGE_TITLE_1;
    public static final String GARAGE_LOCATION = TestData.GARAGE_LOCATION_1;
    public static final String GARAGE_DESCRIPTION = TestData.GARAGE_DESCRIPTION_1;
    public static final Float GARAGE_USABLE_SPACE = TestData.GARAGE_USABLE_SPACE_1;
    public static final Float GARAGE_RENT = TestData.GARAGE_RENT_1;
    
    public static final ImmutableGarageJson.Builder garageBuilder() {
        return garageBuilder1();
    }

    // Test garage 1
    public static final String GARAGE_ID_1 = "b9440c43-b5c0-4951-9c22-000000000001";
    public static final String GARAGE_TITLE_1 = "Tiefgarage 1";
    public static final String GARAGE_LOCATION_1 = "1. Garage links";
    public static final String GARAGE_DESCRIPTION_1 = "Tiefgarage";
    public static final Float GARAGE_USABLE_SPACE_1 = 12.8f;
    public static final Float GARAGE_RENT_1 = 80f;

    public static final ImmutableGarageJson.Builder garageBuilder1() {
        return ImmutableGarageJson
        .builder()
        .id(GARAGE_ID_1)
        .title(GARAGE_TITLE_1)
        .location(GARAGE_LOCATION_1)
        .description(GARAGE_DESCRIPTION_1)
        .usableSpace(GARAGE_USABLE_SPACE_1)
        .rent(GARAGE_RENT_1);
    }

    // Test garage 2
    public static final String GARAGE_ID_2 = "b9440c43-b5c0-4951-9c22-000000000002";
    public static final String GARAGE_TITLE_2 = "Tiefgarage 2";
    public static final String GARAGE_LOCATION_2 = "2. Garage rechts";
    public static final String GARAGE_DESCRIPTION_2 = "Tiefgarage";
    public static final Float GARAGE_USABLE_SPACE_2 = 12.8f;
    public static final Float GARAGE_RENT_2 = 80f;

    public static final ImmutableGarageJson.Builder garageBuilder2() {
        return ImmutableGarageJson
        .builder()
        .id(GARAGE_ID_2)
        .title(GARAGE_TITLE_2)
        .location(GARAGE_LOCATION_2)
        .description(GARAGE_DESCRIPTION_2)
        .usableSpace(GARAGE_USABLE_SPACE_2)
        .rent(GARAGE_RENT_2);
    }

}
