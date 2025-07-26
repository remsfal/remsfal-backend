package de.remsfal.chat;

import java.time.LocalDate;
import de.remsfal.core.json.project.ImmutableTenancyJson;

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


    // Default test tenancy
    public static final String TENANCY_ID = TestData.TENANCY_ID_1;
    public static final String TENANCY_START = TestData.TENANCY_START_1;
    public static final String TENANCY_END = TestData.TENANCY_END_1;

    public static final ImmutableTenancyJson.Builder tenancyBuilder() {
        return tenancyBuilder1();
    }

    // Test tenancy 1
    public static final String TENANCY_ID_1 = "aaaaac43-b5c0-4951-9c22-000000000001";
    public static final String TENANCY_START_1 = "2007-12-01";
    public static final String TENANCY_END_1 = "2025-01-30";

    // Test tenancy 2
    public static final String TENANCY_ID_2 = "bbbbbc43-b5c0-4951-9c22-000000000001";
    public static final String TENANCY_START_2 = "2010-05-03";
    public static final String TENANCY_END_2 = "2030-04-23";

    public static final ImmutableTenancyJson.Builder tenancyBuilder1() {
        return ImmutableTenancyJson
                .builder()
                .id(TENANCY_ID_1)
                .startOfRental(LocalDate.parse(TENANCY_START_1))
                .endOfRental(LocalDate.parse(TENANCY_END_1));
    }

    public static final ImmutableTenancyJson.Builder tenancyBuilder2() {
        return ImmutableTenancyJson
                .builder()
                .id(TENANCY_ID_2)
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
        + "Für Rückfragen stehe ich Ihnen gerne zur Verfügung. Bitte lassen Sie mich wissen, wann der Techniker kommen kann.\\n"
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
