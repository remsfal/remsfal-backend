# REMSFAL GAEB XML Binding Library

This library provides XML bindings for the GAEB DA XML standard version 3.3. GAEB (Gemeinsamer Ausschuss Elektronik im Bauwesen) is a German standard for electronic data exchange in the construction industry.

## Overview

This module generates Java classes from the official GAEB DA XML Schema definitions (XSD) using JAXB. These classes enable reading and writing GAEB XML files in various exchange formats.

## Supported GAEB Formats

### Handel / Trading (DA93-DA97)
- **DA93**: Request for Quotation
- **DA94**: Offer/Bid
- **DA96**: Order/Contract Award
- **DA97**: Order Confirmation

### Leistungsverzeichnis / Bill of Quantities (DA80-DA87)
- **DA80**: Tender - Standard Performance Specification
- **DA81**: Tender
- **DA82**: Bill of Quantities with Alternative Items
- **DA83**: Bill of Quantities
- **DA84**: Price Calculation / Price Update
- **DA85**: Price Calculation for Standard Performance Specification
- **DA86**: Bill of Quantities
- **DA87**: Price Calculation/Update for Standard Performance Specification

### Kosten und Kalkulation / Costs and Calculation (DA50-DA52)
- **DA50**: Elements
- **DA50.1**: Expenditure Values
- **DA51**: Cost Items
- **DA51.1**: Expenditure Values - Cost Items
- **DA52**: Cost Estimate, Cost Calculation, Cost Projection

### Zeitvertrag / Time Contract (DA83Z-DA86ZR)
- **DA83Z**: Bill of Quantities Time Contract
- **DA84Z**: Price Calculation Time Contract
- **DA86ZE**: Work Report Own Services
- **DA86ZR**: Work Report Extra Work

### Rechnung / Invoice (DA89, DA89B)
- **DA89**: Invoice
- **DA89B**: Attached Invoice

### Mengenermittlung / Quantity Calculation (DA31)
- **DA31**: Quantity Calculation (version 2023-01)

## Technical Details

### Code Generation

Java classes are automatically generated from XSD files during Maven build:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>4.0.0</version>
</plugin>
```

### Package Structure

Each GAEB format is generated into its own Java package:

- `de.remsfal.gaeb.da80` to `de.remsfal.gaeb.da87` - Bill of Quantities
- `de.remsfal.gaeb.da93` to `de.remsfal.gaeb.da97` - Trading
- `de.remsfal.gaeb.da50`, `da51`, `da52` - Costs and Calculation
- `de.remsfal.gaeb.da83z`, `da84z`, `da86ze`, `da86zr` - Time Contract
- `de.remsfal.gaeb.da89`, `da89b` - Invoice
- `de.remsfal.gaeb.da31` - Quantity Calculation

### JAXB Bindings

Custom JAXB bindings are configured via `src/main/xjb/bindings-all.xjb` to:
- Define package names
- Resolve name conflicts
- Customize factory methods

## Usage

### Add Dependency

```xml
<dependency>
    <groupId>de.remsfal</groupId>
    <artifactId>remsfal-gaeb</artifactId>
    <version>1.0.11-SNAPSHOT</version>
</dependency>
```

### Create GAEB Document

```java
import de.remsfal.gaeb.XmlMapper;
import de.remsfal.gaeb.da94.ObjectFactory;
import de.remsfal.gaeb.da94.TgGAEB;

// Use ObjectFactory to create GAEB objects
ObjectFactory factory = new ObjectFactory();
TgGAEB gaebDoc = factory.createTgGAEB();

// Set project information
gaebDoc.setPrjInfo(factory.createTgPrjInfo());
gaebDoc.getPrjInfo().setNamePrj("Sample Project");

// Serialize to XML
XmlMapper mapper = new XmlMapper();
mapper.print(gaebDoc);
```

### Read GAEB Document

```java
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import de.remsfal.gaeb.da94.TgGAEB;
import java.io.File;

JAXBContext context = JAXBContext.newInstance(TgGAEB.class);
Unmarshaller unmarshaller = context.createUnmarshaller();
TgGAEB gaebDoc = (TgGAEB) unmarshaller.unmarshal(new File("offer.xml"));
```

## Build

### Build the Project

```bash
mvn clean install
```

This performs the following steps:
1. XSD files are transformed into Java classes via JAXB
2. Generated classes are written to `target/generated-sources/jaxb`
3. Jandex index is created for CDI integration
4. JAR artifact is built

### Run Tests

```bash
mvn test
```

## Notes

- Generated Java classes should not be manually edited
- Changes to XSD files or bindings require a clean build
- XSD definitions are based on the official GAEB specification 3.3
- Some beta formats (Price Comparison and Room Book) are commented out

## References

- [GAEB e.V. - Official Website](https://www.gaeb.de/)
- Technical Documentation: `Fachdokumentation_GAEB-DA-XML_3.3_2023-01.pdf`
