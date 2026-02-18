package de.remsfal.core.json.tenancy;

import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.TenantModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenancyJsonTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID AGREEMENT_ID = UUID.randomUUID();
    private static final LocalDate START = LocalDate.of(2024, 1, 1);

    // --- helper factories ---

    private static AddressJson address(final String street) {
        return ImmutableAddressJson.builder()
            .street(street)
            .city("Berlin")
            .zip("10115")
            .build();
    }

    private static RentModel rent(final UUID unitId) {
        return new RentModel() {
            @Override public UUID getUnitId() { return unitId; }
            @Override public LocalDate getFirstPaymentDate() { return START; }
            @Override public LocalDate getLastPaymentDate() { return null; }
            @Override public BillingCycle getBillingCycle() { return BillingCycle.MONTHLY; }
            @Override public Float getBasicRent() { return null; }
            @Override public Float getOperatingCostsPrepayment() { return null; }
            @Override public Float getHeatingCostsPrepayment() { return null; }
        };
    }

    private static RentalAgreementModel model(
            final List<? extends RentModel> apartmentRents,
            final List<? extends RentModel> commercialRents,
            final List<? extends RentModel> storageRents,
            final List<? extends RentModel> siteRents) {
        return new RentalAgreementModel() {
            @Override public UUID getId() { return AGREEMENT_ID; }
            @Override public UUID getProjectId() { return PROJECT_ID; }
            @Override public List<? extends TenantModel> getTenants() { return Collections.emptyList(); }
            @Override public LocalDate getStartOfRental() { return START; }
            @Override public LocalDate getEndOfRental() { return null; }
            @Override public List<? extends RentModel> getPropertyRents() { return Collections.emptyList(); }
            @Override public List<? extends RentModel> getSiteRents() { return siteRents; }
            @Override public List<? extends RentModel> getBuildingRents() { return Collections.emptyList(); }
            @Override public List<? extends RentModel> getApartmentRents() { return apartmentRents; }
            @Override public List<? extends RentModel> getStorageRents() { return storageRents; }
            @Override public List<? extends RentModel> getCommercialRents() { return commercialRents; }
        };
    }

    private static TenancyJson callValueOf(final RentalAgreementModel m,
            final Map<UUID, AddressJson> unitAddressMap) {
        return TenancyJson.valueOf(m, new HashMap<>(), Map.of(PROJECT_ID, "TestProject"), unitAddressMap);
    }

    // --- valueOf null-guard ---

    @Test
    void valueOf_returnsNull_whenModelIsNull() {
        assertNull(TenancyJson.valueOf(null, new HashMap<>(), new HashMap<>(), new HashMap<>()));
    }

    // --- projectTitle ---

    @Test
    void valueOf_setsProjectTitle_fromMap() {
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of());
        TenancyJson json = TenancyJson.valueOf(m, new HashMap<>(),
            Map.of(PROJECT_ID, "My Project"), new HashMap<>());
        assertEquals("My Project", json.getProjectTitle());
    }

    @Test
    void valueOf_projectTitle_isNull_whenMapIsNull() {
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of());
        TenancyJson json = TenancyJson.valueOf(m, new HashMap<>(), null, new HashMap<>());
        assertNull(json.getProjectTitle());
    }

    @Test
    void valueOf_projectTitle_isNull_whenProjectNotInMap() {
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of());
        TenancyJson json = TenancyJson.valueOf(m, new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertNull(json.getProjectTitle());
    }

    // --- resolveAddress: null map ---

    @Test
    void resolveAddress_returnsNull_whenUnitAddressMapIsNull() {
        RentalAgreementModel m = model(List.of(rent(UUID.randomUUID())), List.of(), List.of(), List.of());
        TenancyJson json = callValueOf(m, null);
        assertNull(json.getAddress());
    }

    // --- resolveAddress: no matching unit in map ---

    @Test
    void resolveAddress_returnsNull_whenNoRentsPresent() {
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of());
        TenancyJson json = callValueOf(m, new HashMap<>());
        assertNull(json.getAddress());
    }

    @Test
    void resolveAddress_returnsNull_whenRentUnitNotInAddressMap() {
        UUID unknownId = UUID.randomUUID();
        RentalAgreementModel m = model(List.of(rent(unknownId)), List.of(), List.of(), List.of());
        TenancyJson json = callValueOf(m, new HashMap<>());
        assertNull(json.getAddress());
    }

    // --- resolveAddress: priority 1 – apartment ---

    @Test
    void resolveAddress_returnsApartmentAddress_whenApartmentRentExists() {
        UUID apartmentId = UUID.randomUUID();
        AddressJson expected = address("Apartment-Str. 1");
        RentalAgreementModel m = model(List.of(rent(apartmentId)), List.of(), List.of(), List.of());
        TenancyJson json = callValueOf(m, Map.of(apartmentId, expected));
        assertEquals(expected, json.getAddress());
    }

    @Test
    void resolveAddress_apartmentWinsOverCommercial() {
        UUID apartmentId = UUID.randomUUID();
        UUID commercialId = UUID.randomUUID();
        AddressJson apartmentAddr = address("Apartment-Str. 1");
        AddressJson commercialAddr = address("Commercial-Str. 2");
        RentalAgreementModel m = model(
            List.of(rent(apartmentId)),
            List.of(rent(commercialId)),
            List.of(), List.of());
        TenancyJson json = callValueOf(m, Map.of(apartmentId, apartmentAddr, commercialId, commercialAddr));
        assertEquals(apartmentAddr, json.getAddress());
    }

    @Test
    void resolveAddress_apartmentWinsOverStorageAndSite() {
        UUID apartmentId = UUID.randomUUID();
        UUID storageId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        AddressJson apartmentAddr = address("Apartment-Str. 1");
        RentalAgreementModel m = model(
            List.of(rent(apartmentId)),
            List.of(),
            List.of(rent(storageId)),
            List.of(rent(siteId)));
        Map<UUID, AddressJson> addrMap = new HashMap<>();
        addrMap.put(apartmentId, apartmentAddr);
        addrMap.put(storageId, address("Storage-Str. 3"));
        addrMap.put(siteId, address("Site-Str. 4"));
        TenancyJson json = callValueOf(m, addrMap);
        assertEquals(apartmentAddr, json.getAddress());
    }

    // --- resolveAddress: priority 2 – commercial ---

    @Test
    void resolveAddress_returnsCommercialAddress_whenNoApartmentRent() {
        UUID commercialId = UUID.randomUUID();
        AddressJson expected = address("Commercial-Str. 2");
        RentalAgreementModel m = model(List.of(), List.of(rent(commercialId)), List.of(), List.of());
        TenancyJson json = callValueOf(m, Map.of(commercialId, expected));
        assertEquals(expected, json.getAddress());
    }

    @Test
    void resolveAddress_commercialWinsOverStorage() {
        UUID commercialId = UUID.randomUUID();
        UUID storageId = UUID.randomUUID();
        AddressJson commercialAddr = address("Commercial-Str. 2");
        RentalAgreementModel m = model(List.of(), List.of(rent(commercialId)), List.of(rent(storageId)), List.of());
        TenancyJson json = callValueOf(m,
            Map.of(commercialId, commercialAddr, storageId, address("Storage-Str. 3")));
        assertEquals(commercialAddr, json.getAddress());
    }

    @Test
    void resolveAddress_skipsApartmentWithoutMapping_fallsBackToCommercial() {
        UUID apartmentId = UUID.randomUUID(); // no address mapping
        UUID commercialId = UUID.randomUUID();
        AddressJson commercialAddr = address("Commercial-Str. 2");
        RentalAgreementModel m = model(
            List.of(rent(apartmentId)),
            List.of(rent(commercialId)),
            List.of(), List.of());
        TenancyJson json = callValueOf(m, Map.of(commercialId, commercialAddr));
        assertEquals(commercialAddr, json.getAddress());
    }

    // --- resolveAddress: priority 3 – storage ---

    @Test
    void resolveAddress_returnsStorageAddress_whenNoApartmentOrCommercialRent() {
        UUID storageId = UUID.randomUUID();
        AddressJson expected = address("Storage-Str. 3");
        RentalAgreementModel m = model(List.of(), List.of(), List.of(rent(storageId)), List.of());
        TenancyJson json = callValueOf(m, Map.of(storageId, expected));
        assertEquals(expected, json.getAddress());
    }

    @Test
    void resolveAddress_storageWinsOverSite() {
        UUID storageId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        AddressJson storageAddr = address("Storage-Str. 3");
        RentalAgreementModel m = model(List.of(), List.of(), List.of(rent(storageId)), List.of(rent(siteId)));
        TenancyJson json = callValueOf(m,
            Map.of(storageId, storageAddr, siteId, address("Site-Str. 4")));
        assertEquals(storageAddr, json.getAddress());
    }

    // --- resolveAddress: priority 4 – site ---

    @Test
    void resolveAddress_returnsSiteAddress_asLastResort() {
        UUID siteId = UUID.randomUUID();
        AddressJson expected = address("Site-Str. 4");
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of(rent(siteId)));
        TenancyJson json = callValueOf(m, Map.of(siteId, expected));
        assertEquals(expected, json.getAddress());
    }

    @Test
    void resolveAddress_skipsSiteWithoutMapping_returnsNull() {
        UUID siteId = UUID.randomUUID(); // no address mapping
        RentalAgreementModel m = model(List.of(), List.of(), List.of(), List.of(rent(siteId)));
        TenancyJson json = callValueOf(m, new HashMap<>());
        assertNull(json.getAddress());
    }

    // --- resolveAddress: first rent in list wins ---

    @Test
    void resolveAddress_returnsFirstApartmentAddressIfMultipleRents() {
        UUID apt1 = UUID.randomUUID();
        UUID apt2 = UUID.randomUUID();
        AddressJson first = address("First-Str. 1");
        AddressJson second = address("Second-Str. 2");
        RentalAgreementModel m = model(List.of(rent(apt1), rent(apt2)), List.of(), List.of(), List.of());
        TenancyJson json = callValueOf(m, Map.of(apt1, first, apt2, second));
        assertEquals(first, json.getAddress());
    }

}
