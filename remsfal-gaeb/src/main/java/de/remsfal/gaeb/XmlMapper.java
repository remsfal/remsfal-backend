package de.remsfal.gaeb;

import java.io.StringWriter;

import de.remsfal.gaeb.da94.ObjectFactory;
import de.remsfal.gaeb.da94.TgGAEB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class XmlMapper {

    public String marshal(TgGAEB xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(TgGAEB.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            ObjectFactory factory = new ObjectFactory();
            JAXBElement<TgGAEB> element = factory.createGAEB(xml);
            marshaller.marshal(element, sw);

            return sw.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to marshal GAEB XML", e);
        }
    }

}
