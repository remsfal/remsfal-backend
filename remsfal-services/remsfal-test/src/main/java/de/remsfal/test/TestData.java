package de.remsfal.test;

import java.time.LocalDate;
import java.util.UUID;

import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.project.ImmutableApartmentJson;
import de.remsfal.core.json.project.ImmutableBuildingJson;
import de.remsfal.core.json.project.ImmutableCommercialJson;
import de.remsfal.core.json.project.ImmutablePropertyJson;
import de.remsfal.core.json.project.ImmutableSiteJson;
import de.remsfal.core.json.project.ImmutableStorageJson;
import de.remsfal.core.json.project.ImmutableTenancyJson;

public class TestData {

    // Test user 1
    public static final UUID USER_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c29-000000000001");
    public static final String USER_TOKEN_1 = "tokenOfMaxMustermann";
    public static final String USER_EMAIL_1 = "max.mustermann@example.org";
    public static final String USER_FIRST_NAME_1 = "Max";
    public static final String USER_LAST_NAME_1 = "Mustermann";

    // Default test user
    public static final UUID USER_ID = TestData.USER_ID_1;
    public static final String USER_TOKEN = TestData.USER_TOKEN_1;
    public static final String USER_EMAIL = TestData.USER_EMAIL_1;
    public static final String USER_FIRST_NAME = TestData.USER_FIRST_NAME_1;
    public static final String USER_LAST_NAME = TestData.USER_LAST_NAME_1;

    // Test user 2
    public static final UUID USER_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c29-000000000002");
    public static final String USER_TOKEN_2 = "tokenOfLieschenMueller";
    public static final String USER_EMAIL_2 = "lieschen.mueller@example.org";
    public static final String USER_FIRST_NAME_2 = "Lieschen";
    public static final String USER_LAST_NAME_2 = "Müller";

    // Test user 3
    public static final UUID USER_ID_3 = UUID.fromString("b9440c43-b5c0-4951-9c29-000000000003");
    public static final String USER_TOKEN_3 = "tokenOfOttoNormalverbraucher";
    public static final String USER_EMAIL_3 = "otto.normalverbraucher@example.org";
    public static final String USER_FIRST_NAME_3 = "Otto";
    public static final String USER_LAST_NAME_3 = "Normalverbraucher";

    // Test user 4
    public static final UUID USER_ID_4 = UUID.fromString("b9440c43-b5c0-4951-9c29-000000000004");
    public static final String USER_TOKEN_4 = "tokenOfLassmirandaDennsiewillja";
    public static final String USER_EMAIL_4 = "l.m.r.d-d.s.w.j@simsons.org";
    public static final String USER_FIRST_NAME_4 = "Lassmiranda";
    public static final String USER_LAST_NAME_4 = "Dennsiewillja";

    // Test address 1
    public static final UUID ADDRESS_ID_1 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000001");
    public static final String ADDRESS_STREET_1 = "Badstraße 12";
    public static final String ADDRESS_CITY_1 = "Berlin";
    public static final String ADDRESS_PROVINCE_1 = "Berlin";
    public static final String ADDRESS_ZIP_1 = "13357";
    public static final String ADDRESS_COUNTRY_1 = "DE";

    // Default test address
    public static final UUID ADDRESS_ID = TestData.ADDRESS_ID_1;
    public static final String ADDRESS_STREET = TestData.ADDRESS_STREET_1;
    public static final String ADDRESS_CITY = TestData.ADDRESS_CITY_1;
    public static final String ADDRESS_PROVINCE = TestData.ADDRESS_PROVINCE_1;
    public static final String ADDRESS_ZIP = TestData.ADDRESS_ZIP_1;
    public static final String ADDRESS_COUNTRY = TestData.ADDRESS_COUNTRY_1;
    
    public static final ImmutableAddressJson.Builder addressBuilder() {
        return addressBuilder1();
    }

    public static final ImmutableAddressJson.Builder addressBuilder1() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_1)
            .city(ADDRESS_CITY_1)
            .province(ADDRESS_PROVINCE_1)
            .zip(ADDRESS_ZIP_1)
            .countryCode(ADDRESS_COUNTRY_1);
    }

    // Test address 2
    public static final UUID ADDRESS_ID_2 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000002");
    public static final String ADDRESS_STREET_2 = "Turmstraße 34";
    public static final String ADDRESS_CITY_2 = "Berlin";
    public static final String ADDRESS_PROVINCE_2 = "Berlin";
    public static final String ADDRESS_ZIP_2 = "10551";
    public static final String ADDRESS_COUNTRY_2 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder2() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_2)
            .city(ADDRESS_CITY_2)
            .province(ADDRESS_PROVINCE_2)
            .zip(ADDRESS_ZIP_2)
            .countryCode(ADDRESS_COUNTRY_2);
    }

    // Test address 3
    public static final UUID ADDRESS_ID_3 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000003");
    public static final String ADDRESS_STREET_3 = "Chausseestraße 101";
    public static final String ADDRESS_CITY_3 = "Berlin";
    public static final String ADDRESS_PROVINCE_3 = "Berlin";
    public static final String ADDRESS_ZIP_3 = "10115";
    public static final String ADDRESS_COUNTRY_3 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder3() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_3)
            .city(ADDRESS_CITY_3)
            .province(ADDRESS_PROVINCE_3)
            .zip(ADDRESS_ZIP_3)
            .countryCode(ADDRESS_COUNTRY_3);
    }

    // Test address 4
    public static final UUID ADDRESS_ID_4 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000004");
    public static final String ADDRESS_STREET_4 = "Elisenstraße 7";
    public static final String ADDRESS_CITY_4 = "München";
    public static final String ADDRESS_PROVINCE_4 = "Bayern";
    public static final String ADDRESS_ZIP_4 = "80335";
    public static final String ADDRESS_COUNTRY_4 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder4() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_4)
            .city(ADDRESS_CITY_4)
            .province(ADDRESS_PROVINCE_4)
            .zip(ADDRESS_ZIP_4)
            .countryCode(ADDRESS_COUNTRY_4);
    }

    // Test address 5
    public static final UUID ADDRESS_ID_5 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000005");
    public static final String ADDRESS_STREET_5 = "Poststraße 3";
    public static final String ADDRESS_CITY_5 = "Leipzig";
    public static final String ADDRESS_PROVINCE_5 = "Sachsen";
    public static final String ADDRESS_ZIP_5 = "04109";
    public static final String ADDRESS_COUNTRY_5 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder5() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_5)
            .city(ADDRESS_CITY_5)
            .province(ADDRESS_PROVINCE_5)
            .zip(ADDRESS_ZIP_5)
            .countryCode(ADDRESS_COUNTRY_5);
    }

    // Test address 6
    public static final UUID ADDRESS_ID_6 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000006");
    public static final String ADDRESS_STREET_6 = "Seestraße 48";
    public static final String ADDRESS_CITY_6 = "Berlin";
    public static final String ADDRESS_PROVINCE_6 = "Berlin";
    public static final String ADDRESS_ZIP_6 = "13353";
    public static final String ADDRESS_COUNTRY_6 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder6() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_6)
            .city(ADDRESS_CITY_6)
            .province(ADDRESS_PROVINCE_6)
            .zip(ADDRESS_ZIP_6)
            .countryCode(ADDRESS_COUNTRY_6);
    }

    // Test address 7
    public static final UUID ADDRESS_ID_7 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000007");
    public static final String ADDRESS_STREET_7 = "Hafenstraße 23";
    public static final String ADDRESS_CITY_7 = "Hamburg";
    public static final String ADDRESS_PROVINCE_7 = "Hamburg";
    public static final String ADDRESS_ZIP_7 = "20359";
    public static final String ADDRESS_COUNTRY_7 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder7() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_7)
            .city(ADDRESS_CITY_7)
            .province(ADDRESS_PROVINCE_7)
            .zip(ADDRESS_ZIP_7)
            .countryCode(ADDRESS_COUNTRY_7);
    }

    // Test address 8
    public static final UUID ADDRESS_ID_8 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000008");
    public static final String ADDRESS_STREET_8 = "Neue Straße 9";
    public static final String ADDRESS_CITY_8 = "Ulm";
    public static final String ADDRESS_PROVINCE_8 = "Baden-Württemberg";
    public static final String ADDRESS_ZIP_8 = "89073";
    public static final String ADDRESS_COUNTRY_8 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder8() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_8)
            .city(ADDRESS_CITY_8)
            .province(ADDRESS_PROVINCE_8)
            .zip(ADDRESS_ZIP_8)
            .countryCode(ADDRESS_COUNTRY_8);
    }

    // Test address 9
    public static final UUID ADDRESS_ID_9 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000009");
    public static final String ADDRESS_STREET_9 = "Münchner Straße 22";
    public static final String ADDRESS_CITY_9 = "Frankfurt am Main";
    public static final String ADDRESS_PROVINCE_9 = "Hessen";
    public static final String ADDRESS_ZIP_9 = "60329";
    public static final String ADDRESS_COUNTRY_9 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder9() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_9)
            .city(ADDRESS_CITY_9)
            .province(ADDRESS_PROVINCE_9)
            .zip(ADDRESS_ZIP_9)
            .countryCode(ADDRESS_COUNTRY_9);
    }

    // Test address 10
    public static final UUID ADDRESS_ID_10 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000010");
    public static final String ADDRESS_STREET_10 = "Berliner Straße 81";
    public static final String ADDRESS_CITY_10 = "Hamburg";
    public static final String ADDRESS_PROVINCE_10 = "Hamburg";
    public static final String ADDRESS_ZIP_10 = "22049";
    public static final String ADDRESS_COUNTRY_10 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder10() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_10)
            .city(ADDRESS_CITY_10)
            .province(ADDRESS_PROVINCE_10)
            .zip(ADDRESS_ZIP_10)
            .countryCode(ADDRESS_COUNTRY_10);
    }

    // Test address 11
    public static final UUID ADDRESS_ID_11 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000011");
    public static final String ADDRESS_STREET_11 = "Theaterstraße 4";
    public static final String ADDRESS_CITY_11 = "Aachen";
    public static final String ADDRESS_PROVINCE_11 = "Nordrhein-Westfalen";
    public static final String ADDRESS_ZIP_11 = "52062";
    public static final String ADDRESS_COUNTRY_11 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder11() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_11)
            .city(ADDRESS_CITY_11)
            .province(ADDRESS_PROVINCE_11)
            .zip(ADDRESS_ZIP_11)
            .countryCode(ADDRESS_COUNTRY_11);
    }

    // Test address 12
    public static final UUID ADDRESS_ID_12 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000012");
    public static final String ADDRESS_STREET_12 = "Opernplatz 1";
    public static final String ADDRESS_CITY_12 = "Frankfurt am Main";
    public static final String ADDRESS_PROVINCE_12 = "Hessen";
    public static final String ADDRESS_ZIP_12 = "60313";
    public static final String ADDRESS_COUNTRY_12 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder12() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_12)
            .city(ADDRESS_CITY_12)
            .province(ADDRESS_PROVINCE_12)
            .zip(ADDRESS_ZIP_12)
            .countryCode(ADDRESS_COUNTRY_12);
    }

    // Test address 13
    public static final UUID ADDRESS_ID_13 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000013");
    public static final String ADDRESS_STREET_13 = "Goethestraße 45";
    public static final String ADDRESS_CITY_13 = "Frankfurt am Main";
    public static final String ADDRESS_PROVINCE_13 = "Hessen";
    public static final String ADDRESS_ZIP_13 = "60313";
    public static final String ADDRESS_COUNTRY_13 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder13() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_13)
            .city(ADDRESS_CITY_13)
            .province(ADDRESS_PROVINCE_13)
            .zip(ADDRESS_ZIP_13)
            .countryCode(ADDRESS_COUNTRY_13);
    }

    // Test address 14
    public static final UUID ADDRESS_ID_14 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000014");
    public static final String ADDRESS_STREET_14 = "Lessingstraße 12";
    public static final String ADDRESS_CITY_14 = "Dresden";
    public static final String ADDRESS_PROVINCE_14 = "Sachsen";
    public static final String ADDRESS_ZIP_14 = "01069";
    public static final String ADDRESS_COUNTRY_14 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder14() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_14)
            .city(ADDRESS_CITY_14)
            .province(ADDRESS_PROVINCE_14)
            .zip(ADDRESS_ZIP_14)
            .countryCode(ADDRESS_COUNTRY_14);
    }

    // Test address 15
    public static final UUID ADDRESS_ID_15 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000015");
    public static final String ADDRESS_STREET_15 = "Schillerstraße 9";
    public static final String ADDRESS_CITY_15 = "Mannheim";
    public static final String ADDRESS_PROVINCE_15 = "Baden-Württemberg";
    public static final String ADDRESS_ZIP_15 = "68165";
    public static final String ADDRESS_COUNTRY_15 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder15() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_15)
            .city(ADDRESS_CITY_15)
            .province(ADDRESS_PROVINCE_15)
            .zip(ADDRESS_ZIP_15)
            .countryCode(ADDRESS_COUNTRY_15);
    }

    // Test address 16
    public static final UUID ADDRESS_ID_16 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000016");
    public static final String ADDRESS_STREET_16 = "Goethestraße 2";
    public static final String ADDRESS_CITY_16 = "München";
    public static final String ADDRESS_PROVINCE_16 = "Bayern";
    public static final String ADDRESS_ZIP_16 = "80336";
    public static final String ADDRESS_COUNTRY_16 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder16() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_16)
            .city(ADDRESS_CITY_16)
            .province(ADDRESS_PROVINCE_16)
            .zip(ADDRESS_ZIP_16)
            .countryCode(ADDRESS_COUNTRY_16);
    }

    // Test address 17
    public static final UUID ADDRESS_ID_17 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000017");
    public static final String ADDRESS_STREET_17 = "Schlossallee 1";
    public static final String ADDRESS_CITY_17 = "Berlin";
    public static final String ADDRESS_PROVINCE_17 = "Berlin";
    public static final String ADDRESS_ZIP_17 = "14059";
    public static final String ADDRESS_COUNTRY_17 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder17() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_17)
            .city(ADDRESS_CITY_17)
            .province(ADDRESS_PROVINCE_17)
            .zip(ADDRESS_ZIP_17)
            .countryCode(ADDRESS_COUNTRY_17);
    }

    // Test address 18
    public static final UUID ADDRESS_ID_18 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000018");
    public static final String ADDRESS_STREET_18 = "Parkstraße 6";
    public static final String ADDRESS_CITY_18 = "Potsdam";
    public static final String ADDRESS_PROVINCE_18 = "Brandenburg";
    public static final String ADDRESS_ZIP_18 = "14482";
    public static final String ADDRESS_COUNTRY_18 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder18() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_18)
            .city(ADDRESS_CITY_18)
            .province(ADDRESS_PROVINCE_18)
            .zip(ADDRESS_ZIP_18)
            .countryCode(ADDRESS_COUNTRY_18);
    }

    // Test address 19 (Bonus: Museumstraße)
    public static final UUID ADDRESS_ID_19 = UUID.fromString("00550c43-b5c0-4951-9c29-000000000019");
    public static final String ADDRESS_STREET_19 = "Museumstraße 5";
    public static final String ADDRESS_CITY_19 = "Bonn";
    public static final String ADDRESS_PROVINCE_19 = "Nordrhein-Westfalen";
    public static final String ADDRESS_ZIP_19 = "53111";
    public static final String ADDRESS_COUNTRY_19 = "DE";

    public static final ImmutableAddressJson.Builder addressBuilder19() {
        return ImmutableAddressJson.builder()
            .street(ADDRESS_STREET_19)
            .city(ADDRESS_CITY_19)
            .province(ADDRESS_PROVINCE_19)
            .zip(ADDRESS_ZIP_19)
            .countryCode(ADDRESS_COUNTRY_19);
    }


    // Test project 1
    public static final UUID PROJECT_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c28-000000000001");
    public static final String PROJECT_TITLE_1 = "Wohnpark am Nieder Neuendorfer See";

    // Default test project
    public static final UUID PROJECT_ID = TestData.PROJECT_ID_1;
    public static final String PROJECT_TITLE = TestData.PROJECT_TITLE_1;

    // Test project 2
    public static final UUID PROJECT_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c28-000000000002");
    public static final String PROJECT_TITLE_2 = "Mehrfamilienhaus in Friedrichshain";

    // Test project 3
    public static final UUID PROJECT_ID_3 = UUID.fromString("b9440c43-b5c0-4951-9c28-000000000003");
    public static final String PROJECT_TITLE_3 = "Häuser der Familie Müller";

    // Test project 4
    public static final UUID PROJECT_ID_4 = UUID.fromString("b9440c43-b5c0-4951-9c28-000000000004");
    public static final String PROJECT_TITLE_4 = "Schall und Rauch GmbH & Co. KG";

    // Test project 5
    public static final UUID PROJECT_ID_5 = UUID.fromString("b9440c43-b5c0-4951-9c28-000000000005");
    public static final String PROJECT_TITLE_5 = "Eigentümergemeinschaft Bundesallee 88 / Berliner Straße 69";

    // Test property 1
    public static final UUID PROPERTY_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c27-000000000001");
    public static final String PROPERTY_TITLE_1 = "Kleinesiedlung";
    public static final String PROPERTY_LOCATION_1 = ADDRESS_STREET_18
        + ", " + ADDRESS_ZIP_18 + " " + ADDRESS_CITY_18;
    public static final String PROPERTY_DESCRIPTION_1 = "Example description of Kleinesiedlung";
    public static final String PROPERTY_LAND_REGISTRY_1 = "Amtsgericht Schönestadt";
    public static final String PROPERTY_CADASTRAL_DESTRICT_1 = "Grundbuch von Kleinesiedlung";
    public static final String PROPERTY_SHEET_NUMBER_1 = "4711";
    public static final Integer PROPERTY_PLOT_NUMBER_1 = 1;
    public static final String PROPERTY_CADASTRAL_SECTION_1 = "48";
    public static final String PROPERTY_PLOT_1 = "12";
    public static final String PROPERTY_ECONOMY_TYPE_1 = "Gebäude- und Freifläche";
    public static final Integer PROPERTY_PLOT_AREA_1 = 1234;

    // Default test property
    public static final UUID PROPERTY_ID = TestData.PROPERTY_ID_1;
    public static final String PROPERTY_TITLE = TestData.PROPERTY_TITLE_1;
    public static final String PROPERTY_LOCATION = TestData.PROPERTY_LOCATION_1;
    public static final String PROPERTY_DESCRIPTION = TestData.PROPERTY_DESCRIPTION_1;
    public static final String PROPERTY_LAND_REGISTRY = TestData.PROPERTY_LAND_REGISTRY_1;
    public static final String PROPERTY_CADASTRAL_DESTRICT = TestData.PROPERTY_CADASTRAL_DESTRICT_1;
    public static final String PROPERTY_SHEET_NUMBER = TestData.PROPERTY_SHEET_NUMBER_1;
    public static final Integer PROPERTY_PLOT_NUMBER = TestData.PROPERTY_PLOT_NUMBER_1;
    public static final String PROPERTY_CADASTRAL_SECTION = TestData.PROPERTY_CADASTRAL_SECTION_1;
    public static final String PROPERTY_PLOT = TestData.PROPERTY_PLOT_1;
    public static final String PROPERTY_ECONOMY_TYPE = TestData.PROPERTY_ECONOMY_TYPE_1;
    public static final Integer PROPERTY_PLOT_AREA = TestData.PROPERTY_PLOT_AREA_1;
    
    public static final ImmutablePropertyJson.Builder propertyBuilder() {
        return propertyBuilder1();
    }

    public static final ImmutablePropertyJson.Builder propertyBuilder1() {
        return ImmutablePropertyJson
            .builder()
            .title(PROPERTY_TITLE_1)
            .location(PROPERTY_LOCATION_1)
            .description(PROPERTY_DESCRIPTION_1)
            .landRegistry(PROPERTY_LAND_REGISTRY_1)
            .cadastralDistrict(PROPERTY_CADASTRAL_DESTRICT_1)
            .sheetNumber(PROPERTY_SHEET_NUMBER_1)
            .plotNumber(PROPERTY_PLOT_NUMBER_1)
            .cadastralSection(PROPERTY_CADASTRAL_SECTION_1)
            .plot(PROPERTY_PLOT_1)
            .economyType(PROPERTY_ECONOMY_TYPE_1)
            .plotArea(PROPERTY_PLOT_AREA_1);
    }

    // Test property 2
    public static final UUID PROPERTY_ID_2 = UUID.fromString("8b4f2703-94ca-490f-ae08-a787c716415f");
    public static final String PROPERTY_TITLE_2 = "Test-Siedling Bremen";
    public static final String PROPERTY_LOCATION_2 = ADDRESS_STREET_19
        + ", " + ADDRESS_ZIP_19 + " " + ADDRESS_CITY_19;
    public static final String PROPERTY_DESCRIPTION_2 = "Example description of Test-Siedling";
    public static final String PROPERTY_LAND_REGISTRY_2 = "Amtsgericht Bremen";
    public static final String PROPERTY_CADASTRAL_DESTRICT_2 = "Grundbuch von Bremen";
    public static final String PROPERTY_SHEET_NUMBER_2 = "4766";
    public static final Integer PROPERTY_PLOT_NUMBER_2 = 99;
    public static final String PROPERTY_CADASTRAL_SECTION_2 = "448";
    public static final String PROPERTY_PLOT_2 = "132";
    public static final String PROPERTY_ECONOMY_TYPE_2 = "Gebäude- und Freifläche";
    public static final Integer PROPERTY_PLOT_AREA_2 = 4444;

    // Default test site
    public static final UUID SITE_ID = TestData.SITE_ID_1;
    public static final String SITE_TITLE = TestData.SITE_TITLE_1;
    public static final String SITE_DESCRIPTION = TestData.SITE_DESCRIPTION_1;
    public static final Float SITE_OUTDOOR_AREA = TestData.SITE_OUTDOOR_AREA_1;

    public static final ImmutableSiteJson.Builder siteBuilder() {
        return siteBuilder1();
    }

    // Test site 1
    public static final UUID SITE_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c26-000000000001");
    public static final String SITE_TITLE_1 = "PKW Stellplatz";
    public static final String SITE_DESCRIPTION_1 = "Stellplatz mit Carport";
    public static final Float SITE_OUTDOOR_AREA_1 = 13.4f;
    
    public static final ImmutableSiteJson.Builder siteBuilder1() {
        return ImmutableSiteJson
            .builder()
            .title(SITE_TITLE_1)
            .description(SITE_DESCRIPTION_1)
            .outdoorArea(SITE_OUTDOOR_AREA_1);
    }

    // Default test building
    public static final UUID BUILDING_ID = TestData.BUILDING_ID_1;
    public static final String BUILDING_TITLE = TestData.BUILDING_TITLE_1;
    public static final String BUILDING_DESCRIPTION = TestData.BUILDING_DESCRIPTION_1;
    public static final Float BUILDING_LIVING_SPACE = TestData.BUILDING_LIVING_SPACE_1;
    public static final Float BUILDING_USABLE_SPACE = TestData.BUILDING_USABLE_SPACE_1;
    public static final Float BUILDING_HEATING_SPACE = TestData.BUILDING_HEATING_SPACE_1;

    public static final ImmutableBuildingJson.Builder buildingBuilder() {
        return buildingBuilder1();
    }

    // Test building 1
    public static final UUID BUILDING_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c25-000000000001");
    public static final String BUILDING_TITLE_1 = "Maximiliankorso (Fam. Rudolf)";
    public static final String BUILDING_DESCRIPTION_1 = "Flachbau mit zwei Stockwerken";
    public static final Float BUILDING_LIVING_SPACE_1 = 87.46f;
    public static final Float BUILDING_USABLE_SPACE_1 = 53.9f;
    public static final Float BUILDING_HEATING_SPACE_1 = 103.22f;

    public static final ImmutableBuildingJson.Builder buildingBuilder1() {
        return ImmutableBuildingJson
            .builder()
            .title(BUILDING_TITLE_1)
            .description(BUILDING_DESCRIPTION_1)
            .livingSpace(BUILDING_LIVING_SPACE_1)
            .usableSpace(BUILDING_USABLE_SPACE_1)
            .heatingSpace(BUILDING_HEATING_SPACE_1);
    }
      
    // Test building 2
    public static final UUID BUILDING_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c25-000000000002");
    public static final String BUILDING_TITLE_2 = "Bavariaplatz (Fam. Müller)";
    public static final String BUILDING_DESCRIPTION_2 = "Einfamilienhaus mit Garten";
    public static final Float BUILDING_GROSS_FLOOR_AREA_2 = 430.5f;
    public static final Float BUILDING_NET_FLOOR_AREA_2 = 400.0f;
    public static final Float BUILDING_CONSTRUCTION_FLOOR_AREA_2 = 70.5f;
    public static final Float BUILDING_HEATING_SPACE_2 = 420.75f;

    public static final ImmutableBuildingJson.Builder buildingBuilder2() {
        return ImmutableBuildingJson
            .builder()
            .title(BUILDING_TITLE_2)
            .description(BUILDING_DESCRIPTION_2)
            .grossFloorArea(BUILDING_GROSS_FLOOR_AREA_2)
            .netFloorArea(BUILDING_NET_FLOOR_AREA_2)
            .constructionFloorArea(BUILDING_CONSTRUCTION_FLOOR_AREA_2)
            .heatingSpace(BUILDING_HEATING_SPACE_2);
    }

    // Default test apartment
    public static final UUID APARTMENT_ID = TestData.APARTMENT_ID_1;
    public static final String APARTMENT_TITLE = TestData.APARTMENT_TITLE_1;
    public static final String APARTMENT_LOCATION = TestData.APARTMENT_LOCATION_1;
    public static final String APARTMENT_DESCRIPTION = TestData.APARTMENT_DESCRIPTION_1;
    public static final Float APARTMENT_LIVING_SPACE = TestData.APARTMENT_LIVING_SPACE_1;
    public static final Float APARTMENT_USABLE_SPACE = TestData.APARTMENT_USABLE_SPACE_1;
    public static final Float APARTMENT_HEATING_SPACE = TestData.APARTMENT_HEATING_SPACE_1;

    public static final ImmutableApartmentJson.Builder apartmentBuilder() {
        return apartmentBuilder1();
    }
    
    // Test apartment 1
    public static final UUID APARTMENT_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c24-000000000001");
    public static final String APARTMENT_TITLE_1 = "2 Zimmerwohnung 1.OG rechts";
    public static final String APARTMENT_LOCATION_1 = "1. OG rechts";
    public static final String APARTMENT_DESCRIPTION_1 = "Frisch renoviert, Fliesen im Flur, Parkett im Wohnzimmer";
    public static final Float APARTMENT_LIVING_SPACE_1 = 77.36f;
    public static final Float APARTMENT_USABLE_SPACE_1 = 0f;
    public static final Float APARTMENT_HEATING_SPACE_1 = 77.36f;

    public static final ImmutableApartmentJson.Builder apartmentBuilder1() {
        return ImmutableApartmentJson
        .builder()
        .title(APARTMENT_TITLE_1)
        .location(APARTMENT_LOCATION_1)
        .description(APARTMENT_DESCRIPTION_1)
        .livingSpace(APARTMENT_LIVING_SPACE_1)
        .usableSpace(APARTMENT_USABLE_SPACE_1)
        .heatingSpace(APARTMENT_HEATING_SPACE_1);
    }

    // Test apartment 2
    public static final UUID APARTMENT_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c24-000000000002");
    public static final String APARTMENT_TITLE_2 = "3 Zimmerwohnung 1.OG links";
    public static final String APARTMENT_LOCATION_2 = "1. OG links";
    public static final String APARTMENT_DESCRIPTION_2 = "2015 renoviert, Fliesen im Flur, Laminat im Wohnzimmer";
    public static final Float APARTMENT_LIVING_SPACE_2 = 87.36f;
    public static final Float APARTMENT_USABLE_SPACE_2 = 0f;
    public static final Float APARTMENT_HEATING_SPACE_2 = 87.36f;

    public static final ImmutableApartmentJson.Builder apartmentBuilder2() {
        return ImmutableApartmentJson
        .builder()
        .title(APARTMENT_TITLE_2)
        .location(APARTMENT_LOCATION_2)
        .description(APARTMENT_DESCRIPTION_2)
        .livingSpace(APARTMENT_LIVING_SPACE_2)
        .usableSpace(APARTMENT_USABLE_SPACE_2)
        .heatingSpace(APARTMENT_HEATING_SPACE_2);
    }

    // Default test commercial
    public static final UUID COMMERCIAL_ID = TestData.COMMERCIAL_ID_1;
    public static final String COMMERCIAL_TITLE = TestData.COMMERCIAL_TITLE_1;
    public static final String COMMERCIAL_LOCATION = TestData.COMMERCIAL_LOCATION_1;
    public static final String COMMERCIAL_DESCRIPTION = TestData.COMMERCIAL_DESCRIPTION_1;
    public static final Float COMMERCIAL_NET_FLOOR_AREA = TestData.COMMERCIAL_NET_FLOOR_AREA_1;
    public static final Float COMMERCIAL_HEATING_SPACE = TestData.COMMERCIAL_HEATING_SPACE_1;
    
    public static final ImmutableCommercialJson.Builder commercialBuilder() {
        return commercialBuilder1();
    }

    // Test commercial 1
    public static final UUID COMMERCIAL_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c23-000000000001");
    public static final String COMMERCIAL_TITLE_1 = "Bäckerei Lempke";
    public static final String COMMERCIAL_LOCATION_1 = "EG links";
    public static final String COMMERCIAL_DESCRIPTION_1 = "Bäckerei mit Tischen vor dem Haus";
    public static final Float COMMERCIAL_NET_FLOOR_AREA_1 = 423.92f;
    public static final Float COMMERCIAL_HEATING_SPACE_1 = 204.27f;

    public static final ImmutableCommercialJson.Builder commercialBuilder1() {
        return ImmutableCommercialJson
        .builder()
        .title(COMMERCIAL_TITLE_1)
        .location(COMMERCIAL_LOCATION_1)
        .description(COMMERCIAL_DESCRIPTION_1)
        .netFloorArea(COMMERCIAL_NET_FLOOR_AREA_1)
        .heatingSpace(COMMERCIAL_HEATING_SPACE_1);
    }

    // Test commercial 2
    public static final UUID COMMERCIAL_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c23-000000000002");
    public static final String COMMERCIAL_TITLE_2 = "Bäckerei Ekpmel";
    public static final String COMMERCIAL_LOCATION_2 = "EG rechts";
    public static final String COMMERCIAL_DESCRIPTION_2 = "Bäckerei mit Tischen hinter dem Haus";
    public static final Float COMMERCIAL_USABLE_FLOOR_AREA_2 = 450.92f;
    public static final Float COMMERCIAL_TECHNICAL_SERVICE_AREA_2 = 100.9f;
    public static final Float COMMERCIAL_TRAFFIC_AREA_2 = 53.9f;
    public static final Float COMMERCIAL_HEATING_SPACE_2 = 134.27f;

    public static final ImmutableCommercialJson.Builder commercialBuilder2() {
        return ImmutableCommercialJson
                .builder()
                .title(COMMERCIAL_TITLE_2)
                .location(COMMERCIAL_LOCATION_2)
                .description(COMMERCIAL_DESCRIPTION_2)
                .usableFloorArea(COMMERCIAL_USABLE_FLOOR_AREA_2)
                .technicalServicesArea(COMMERCIAL_TECHNICAL_SERVICE_AREA_2)
                .trafficArea(COMMERCIAL_TRAFFIC_AREA_2)
                .heatingSpace(COMMERCIAL_HEATING_SPACE_2);
    }

    // Default test storage
    public static final UUID STORAGE_ID = TestData.STORAGE_ID_1;
    public static final String STORAGE_TITLE = TestData.STORAGE_TITLE_1;
    public static final String STORAGE_LOCATION = TestData.STORAGE_LOCATION_1;
    public static final String STORAGE_DESCRIPTION = TestData.STORAGE_DESCRIPTION_1;
    public static final Float STORAGE_USABLE_SPACE = TestData.STORAGE_USABLE_SPACE_1;
    public static final Float STORAGE_RENT = TestData.STORAGE_RENT_1;
    
    public static final ImmutableStorageJson.Builder storageBuilder() {
        return storageBuilder1();
    }

    // Test storage 1
    public static final UUID STORAGE_ID_1 = UUID.fromString("b9440c43-b5c0-4951-9c22-000000000001");
    public static final String STORAGE_TITLE_1 = "Tiefgarage 1";
    public static final String STORAGE_LOCATION_1 = "1";
    public static final String STORAGE_DESCRIPTION_1 = "Tiefgarage 1 links";
    public static final Float STORAGE_USABLE_SPACE_1 = 12.8f;
    public static final Float STORAGE_RENT_1 = 80f;

    public static final ImmutableStorageJson.Builder storageBuilder1() {
        return ImmutableStorageJson
        .builder()
        .title(STORAGE_TITLE_1)
        .location(STORAGE_LOCATION_1)
        .description(STORAGE_DESCRIPTION_1)
        .usableSpace(STORAGE_USABLE_SPACE_1);
    }

    // Test storage 2
    public static final UUID STORAGE_ID_2 = UUID.fromString("b9440c43-b5c0-4951-9c22-000000000002");
    public static final String STORAGE_TITLE_2 = "Tiefgarage 2";
    public static final String STORAGE_LOCATION_2 = "2";
    public static final String STORAGE_DESCRIPTION_2 = "Tiefgarage 2 rechts";
    public static final Float STORAGE_USABLE_SPACE_2 = 12.8f;
    public static final Float STORAGE_RENT_2 = 80f;

    public static final ImmutableStorageJson.Builder storageBuilder2() {
        return ImmutableStorageJson
        .builder()
        .title(STORAGE_TITLE_2)
        .location(STORAGE_LOCATION_2)
        .description(STORAGE_DESCRIPTION_2)
        .usableSpace(STORAGE_USABLE_SPACE_2);
    }

    // Default test tenancy
    public static final UUID TENANCY_ID = TestData.TENANCY_ID_1;
    public static final String TENANCY_START = TestData.TENANCY_START_1;
    public static final String TENANCY_END = TestData.TENANCY_END_1;

    public static final ImmutableTenancyJson.Builder tenancyBuilder() {
        return tenancyBuilder1();
    }

    // Test tenancy 1
    public static final UUID TENANCY_ID_1 = UUID.fromString("aaaaac43-b5c0-4951-9c22-000000000001");
    public static final String TENANCY_START_1 = "2007-12-01";
    public static final String TENANCY_END_1 = "2025-01-30";

    // Test tenancy 2
    public static final UUID TENANCY_ID_2 = UUID.fromString("bbbbbc43-b5c0-4951-9c22-000000000002");
    public static final String TENANCY_START_2 = "2010-05-15";
    public static final String TENANCY_END_2 = "2030-04-23";

    // Test tenancy 3
    public static final UUID TENANCY_ID_3 = UUID.fromString("cccccc43-b5c0-4951-9c22-000000000003");
    public static final String TENANCY_START_3 = "2016-07-01";
    public static final String TENANCY_END_3 = "2022-12-31";

    public static final ImmutableTenancyJson.Builder tenancyBuilder1() {
        return ImmutableTenancyJson
                .builder()
                .startOfRental(LocalDate.parse(TENANCY_START_1))
                .endOfRental(LocalDate.parse(TENANCY_END_1));
    }

    public static final ImmutableTenancyJson.Builder tenancyBuilder2() {
        return ImmutableTenancyJson
                .builder()
                .startOfRental(LocalDate.parse(TENANCY_START_2))
                .endOfRental(LocalDate.parse(TENANCY_END_2));
    }

    // Default test task
    public static final String TASK_TITLE = TestData.TASK_TITLE_1;
    public static final String TASK_DESCRIPTION = TestData.TASK_DESCRIPTION_1;
    
    // Test task 1
    public static final String TASK_TITLE_1 = "Heizung funktioniert nicht";
    public static final String TASK_DESCRIPTION_1 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen dringenden Heizungsausfall in meiner Wohnung melden. "
        + "Seit dem 23.12.2023 funktioniert die Heizung nicht mehr, "
        + "und die Wohnung wird nicht mehr ausreichend beheizt. Dies ist besonders kritisch, "
        + "da die Außentemperaturen derzeit sehr niedrig sind und die Wohnung ohne funktionierende "
        + "Heizung unangenehm kalt ist.Ich bitte Sie daher dringend, einen Techniker zu schicken, "
        + "um das Problem so schnell wie möglich zu beheben. Ein schneller Eingriff ist notwendig, "
        + "um die Heizung wieder in Betrieb zu nehmen und weiteren Schaden zu vermeiden.\\n"
        + "\\n"
        + "Für Rückfragen stehe ich Ihnen gerne zur Verfügung. Bitte lassen Sie mich wissen, "
        + "wann der Techniker kommen kann.\\n"
        + "\\n"
        + "Vielen Dank für Ihre schnelle Hilfe.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Test task 2
    public static final String TASK_TITLE_2 = "Licht im Treppenhaus defekt";
    public static final String TASK_DESCRIPTION_2 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen dringenden Ausfall der Beleuchtung im Treppenhaus unseres Gebäudes melden. "
        + "Seit dem 8.Mai 2023 funktioniert das Licht im Treppenhaus nicht mehr, was zu erheblichen Unannehmlichkeiten "
        + "und Sicherheitsrisiken für alle Bewohner führt. Ich bitte Sie daher dringend, einen Techniker zu schicken, "
        + "um den Beleuchtungsausfall so schnell wie möglich zu beheben.\\n"
        + "\\n"
        + "Vielen Dank im Voraus.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";


}