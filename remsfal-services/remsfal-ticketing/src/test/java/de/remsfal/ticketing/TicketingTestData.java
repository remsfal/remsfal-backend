package de.remsfal.ticketing;

import java.util.Map;
import java.util.UUID;

import de.remsfal.test.TestData;

public class TicketingTestData extends TestData {

    // Files
    public static final String FILE_PNG_PATH = "test-image.png";
    public static final String FILE_PNG_TYPE = "image/png";

    // Issue test data
    public static final UUID ISSUE_ID_1 = UUID.fromString("5b111b34-1073-4f48-a79d-f19b17e7d56b");
    public static final UUID ISSUE_ID_2 = UUID.fromString("4b8cd355-ad07-437a-9e71-a4e2e3624957");
    public static final UUID ISSUE_ID_3 = UUID.fromString("7c9de466-be18-448b-af82-b5f3f8736068");

    // Default test issue
    public static final String ISSUE_TITLE = TicketingTestData.ISSUE_TITLE_1;
    public static final String ISSUE_DESCRIPTION = TicketingTestData.ISSUE_DESCRIPTION_1;
    
    // Test issue 1
    public static final String ISSUE_TITLE_1 = "Heizung funktioniert nicht";
    public static final String ISSUE_DESCRIPTION_1 = "Sehr geehrte Damen und Herren,\\n"
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

    // Test issue 2
    public static final String ISSUE_TITLE_2 = "Licht im Treppenhaus defekt";
    public static final String ISSUE_DESCRIPTION_2 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen dringenden Ausfall der Beleuchtung im Treppenhaus unseres Gebäudes melden. "
        + "Seit dem 8.Mai 2023 funktioniert das Licht im Treppenhaus nicht mehr, was zu erheblichen Unannehmlichkeiten "
        + "und Sicherheitsrisiken für alle Bewohner führt. Ich bitte Sie daher dringend, einen Techniker zu schicken, "
        + "um den Beleuchtungsausfall so schnell wie möglich zu beheben.\\n"
        + "\\n"
        + "Vielen Dank im Voraus.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Test issue 3
    public static final String ISSUE_TITLE_3 = "Wasserschaden in der Küche";
    public static final String ISSUE_DESCRIPTION_3 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen Wasserschaden in meiner Küche melden. "
        + "Seit gestern Abend tritt Wasser aus der Spülmaschine aus und hat bereits den Küchenboden überflutet. "
        + "Ich habe die Spülmaschine ausgeschaltet und den Hauptwasserhahn zugedreht, aber es ist dringend erforderlich, "
        + "dass ein Techniker das Problem behebt, bevor weitere Schäden entstehen.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Legacy task references for backward compatibility
    public static final String TASK_TITLE = ISSUE_TITLE;
    public static final String TASK_DESCRIPTION = ISSUE_DESCRIPTION;
    public static final String TASK_TITLE_1 = ISSUE_TITLE_1;
    public static final String TASK_DESCRIPTION_1 = ISSUE_DESCRIPTION_1;
    public static final String TASK_TITLE_2 = ISSUE_TITLE_2;
    public static final String TASK_DESCRIPTION_2 = ISSUE_DESCRIPTION_2;

    // Test user credentials with project roles for testing
    public static final Map<String, String> MANAGER_PROJECT_ROLES = Map.of(
        PROJECT_ID.toString(), "MANAGER"
    );
    
    public static final Map<String, String> TENANT_PROJECT_ROLES = Map.of(
        TENANCY_ID.toString(), PROJECT_ID.toString()
    );

    // Chat session test data
    public static final String CHAT_SESSION_ID_1 = "64ab9ef0-25ef-4a1c-81c9-5963f7c7d211";
    public static final String CHAT_SESSION_ID_2 = "30444d17-56a9-4275-a9a8-e4fb7305359a";
    public static final UUID CHAT_SESSION_ID_1_UUID = UUID.fromString(CHAT_SESSION_ID_1);
    public static final UUID CHAT_SESSION_ID_2_UUID = UUID.fromString(CHAT_SESSION_ID_2);

    // Chat message test data
    public static final String CHAT_MESSAGE_ID_1 = "b9854462-abb8-4213-8b15-be9290a19959";
    public static final String CHAT_MESSAGE_ID_2 = "3f72a368-48bd-405e-976f-51a5c417a5c2";
    public static final String CHAT_MESSAGE_ID_3 = "42817454-dc1e-476e-93d5-e073b424f191";
    public static final UUID CHAT_MESSAGE_ID_1_UUID = UUID.fromString(CHAT_MESSAGE_ID_1);
    public static final UUID CHAT_MESSAGE_ID_2_UUID = UUID.fromString(CHAT_MESSAGE_ID_2);
    public static final UUID CHAT_MESSAGE_ID_3_UUID = UUID.fromString(CHAT_MESSAGE_ID_3);

}
