package de.remsfal.ticketing;

import java.util.Map;
import java.util.UUID;

import de.remsfal.test.TestData;

public class TicketingTestData extends TestData {

    // Test user credentials with project roles for testing
    public static final Map<String, String> MANAGER_PROJECT_ROLES = Map.of(
        PROJECT_ID_1.toString(), "MANAGER",
        PROJECT_ID_2.toString(), "MANAGER"
    );
    
    public static final Map<String, String> TENANT_PROJECT_ROLES = Map.of(
        AGREEMENT_ID.toString(), PROJECT_ID.toString()
    );

    // Files
    public static final String FILE_PNG_PATH = "test-image.png";
    public static final String FILE_PNG_TYPE = "image/png";

    // Attachment test data
    public static final UUID ATTACHMENT_ID_1 = UUID.fromString("aa000000-0000-0000-0000-000000000001");
    public static final String ATTACHMENT_FILE_PATH_1 = "Leckage-Steigleitung.jpg";
    public static final String ATTACHMENT_FILE_TYPE_1 = "image/jpeg";

    public static final UUID ATTACHMENT_ID_2 = UUID.fromString("aa000000-0000-0000-0000-000000000002");
    public static final String ATTACHMENT_FILE_PATH_2 = "Wasserschaden_Keller-links.jpeg";
    public static final String ATTACHMENT_FILE_TYPE_2 = "image/jpeg";

    public static final UUID ATTACHMENT_ID_3 = UUID.fromString("aa000000-0000-0000-0000-000000000003");
    public static final String ATTACHMENT_FILE_PATH_3 = "Wasserschaden_Keller-rechts.jpeg";
    public static final String ATTACHMENT_FILE_TYPE_3 = "image/jpeg";

    public static final UUID ATTACHMENT_ID_4 = UUID.fromString("aa000000-0000-0000-0000-000000000004");
    public static final String ATTACHMENT_FILE_PATH_4 = "Wasserschaden_Ursache.mp4";
    public static final String ATTACHMENT_FILE_TYPE_4 = "video/mp4";

    // Test issue 1
    public static final UUID ISSUE_ID_1 = UUID.fromString("5b111b34-1073-4f48-a79d-f19b17e7d56b");
    public static final String ISSUE_TITLE_1 = "Heizung funktioniert nicht";
    public static final String ISSUE_DESCRIPTION_1 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen dringenden Heizungsausfall in meiner Wohnung melden. "
        + "Seit dem 23.12.2023 funktioniert die Heizung nicht mehr, "
        + "und die Wohnung wird nicht mehr ausreichend beheizt. Dies ist besonders kritisch, "
        + "da die Außentemperaturen derzeit sehr niedrig sind und die Wohnung ohne funktionierende "
        + "Heizung unangenehm kalt ist. Ich bitte Sie daher dringend, einen Techniker zu schicken, "
        + "um das Problem so schnell wie möglich zu beheben. Ein schneller Eingriff ist notwendig, "
        + "um die Heizung wieder in Betrieb zu nehmen und weiteren Schaden zu vermeiden.\\n"
        + "\\n"
        + "Für Rückfragen stehe ich Ihnen gerne zur Verfügung. Bitte lassen Sie mich wissen, "
        + "wann der Techniker kommen kann.\\n"
        + "\\n"
        + "Vielen Dank für Ihre schnelle Hilfe.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Default test issue
    public static final UUID ISSUE_ID = TicketingTestData.ISSUE_ID_1;
    public static final String ISSUE_TITLE = TicketingTestData.ISSUE_TITLE_1;
    public static final String ISSUE_DESCRIPTION = TicketingTestData.ISSUE_DESCRIPTION_1;
    
    // Test issue 2
    public static final UUID ISSUE_ID_2 = UUID.fromString("4b8cd355-ad07-437a-9e71-a4e2e3624957");
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
    public static final UUID ISSUE_ID_3 = UUID.fromString("7c9de466-be18-448b-af82-b5f3f8736068");
    public static final String ISSUE_TITLE_3 = "Wasserschaden im Keller";
    public static final String ISSUE_DESCRIPTION_3 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit möchte ich einen Wasserschaden im Keller melden. "
        + "Seit gestern Abend tritt Wasser unter der einen Kellertür hervor und hat bereits den Flur überflutet. "
        + "Ich habe keinen Schlüssel zu diesem Kellerraum und kann auch nicht den Hauptwasserhahn zugedreht. "
        + "Wir brauchen Hilfe, bevor weitere Schäden entstehen.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Test issue 4
    public static final UUID ISSUE_ID_4 = UUID.fromString("8d0ef577-cc14-4e37-bf93-d30e06af847c");
    public static final String ISSUE_TITLE_4 = "Kündigung der Wohnung";
    public static final String ISSUE_DESCRIPTION_4 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "hiermit kündige ich das Mietverhältnis für die oben genannte Wohnung fristgerecht "
        + "zum nächstmöglichen Termin gemäß den vertraglichen Vereinbarungen. "
        + "Ich bitte Sie, mir den Empfang dieser Kündigung schriftlich zu bestätigen "
        + "und mir die weiteren Schritte für die Wohnungsübergabe mitzuteilen.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Test issue 5
    public static final UUID ISSUE_ID_5 = UUID.fromString("9e1fa688-dd25-4f48-ae84-e41f17ba958d");
    public static final String ISSUE_TITLE_5 = "Anfrage zur Wohnungsgröße";
    public static final String ISSUE_DESCRIPTION_5 = "Sehr geehrte Damen und Herren,\\n"
        + "\\n"
        + "ich wende mich an Sie mit einer Frage bezüglich meiner aktuellen Wohnung. "
        + "Könnten Sie mir bitte mitteilen, wie groß die Gesamtfläche meiner Wohnung in Quadratmetern ist? "
        + "Diese Information benötige ich für persönliche Planungszwecke.\\n"
        + "\\n"
        + "Vielen Dank für Ihre Hilfe.\\n"
        + "\\n"
        + "Mit freundlichen Grüßen";

    // Chat session test data
    public static final UUID CHAT_SESSION_ID_1 = UUID.fromString("64ab9ef0-25ef-4a1c-81c9-5963f7c7d211");
    public static final UUID CHAT_SESSION_ID_2 = UUID.fromString("30444d17-56a9-4275-a9a8-e4fb7305359a");

    // Chat message test data
    public static final UUID CHAT_MESSAGE_ID_1 = UUID.fromString("b9854462-abb8-4213-8b15-be9290a19959");
    public static final UUID CHAT_MESSAGE_ID_2 = UUID.fromString("3f72a368-48bd-405e-976f-51a5c417a5c2");
    public static final UUID CHAT_MESSAGE_ID_3 = UUID.fromString("42817454-dc1e-476e-93d5-e073b424f191");

}
