package de.remsfal.core.json;

import org.junit.jupiter.api.Test;

import de.remsfal.gaeb.XmlMapper;
import de.remsfal.gaeb.da94.ObjectFactory;
import de.remsfal.gaeb.da94.TgGAEB;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlMapperTest {

    @Test
    void testValueOf() {
        ObjectFactory factory = new ObjectFactory();
        TgGAEB doc = factory.createTgGAEB();
        doc.setPrjInfo(factory.createTgPrjInfo());
        doc.getPrjInfo().setNamePrj("Sample Project");

        XmlMapper mapper = new XmlMapper();
        String xml = mapper.marshal(doc);

        assertNotNull(xml);
        assertTrue(xml.contains("Sample Project"));
    }
}