package de.remsfal.gaeb;

import java.io.StringWriter;

import de.remsfal.gaeb.da94.ObjectFactory;
import de.remsfal.gaeb.da94.TgGAEB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class XmlMapper {

    public void print(TgGAEB xml) {
        try {
            // 1. Kontext erstellen: Er kennt alle Klassen, die er serialisieren soll.
            JAXBContext context;
            context = JAXBContext.newInstance(TgGAEB.class);
            // 2. Marshaller erstellen
            Marshaller marshaller = context.createMarshaller();
            // Optional: Für eine schön formatierte (eingerückte) Ausgabe
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Optional: XML-Deklaration (z.B. <?xml version="1.0" encoding="UTF-8" standalone="yes"?>) weglassen
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            // 3. StringWriter verwenden, um die Ausgabe abzufangen
            StringWriter sw = new StringWriter();

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<TgGAEB> e = factory.createGAEB(xml);
            // 4. Marshalling durchführen
            marshaller.marshal(e, sw);

            // 5. String-Inhalt abrufen
            String xmlString = sw.toString();

            System.out.println(xmlString);
            // System.out.println("XML: " + xml);
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
