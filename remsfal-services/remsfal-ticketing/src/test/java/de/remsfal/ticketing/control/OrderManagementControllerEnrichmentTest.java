package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableRentalUnitNodeDataJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.ticketing.ImmutableQuotationRequestJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class OrderManagementControllerEnrichmentTest {

    @Inject
    OrderManagementController controller;

    @InjectMock
    QuotationRequestRepository quotationRequestRepository;

    @Test
    void testEnrichRequestForQuotation_missingRequestId_skipsUpdate() {
        controller.enrichRequestForQuotation(ImmutableIssueEventJson.builder()
            .issueId(UUID.randomUUID())
            .build());

        verify(quotationRequestRepository, never()).findById(any());
        verify(quotationRequestRepository, never()).update(any());
    }

    @Test
    void testEnrichRequestForQuotation_unknownRequest_skipsUpdate() {
        final UUID issueId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();
        when(quotationRequestRepository.findById(any())).thenReturn(Optional.empty());

        controller.enrichRequestForQuotation(ImmutableIssueEventJson.builder()
            .issueId(issueId)
            .quotationRequest(ImmutableQuotationRequestJson.builder().id(requestId).build())
            .build());

        verify(quotationRequestRepository).findById(any());
        verify(quotationRequestRepository, never()).update(any());
    }

    @Test
    void testEnrichRequestForQuotation_updatesResolvedFields() {
        final UUID issueId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();

        final QuotationRequestEntity entity = new QuotationRequestEntity();
        final QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(issueId);
        key.setRequestId(requestId);
        entity.setKey(key);
        when(quotationRequestRepository.findById(any())).thenReturn(Optional.of(entity));
        when(quotationRequestRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        controller.enrichRequestForQuotation(ImmutableIssueEventJson.builder()
            .issueId(issueId)
            .project(ImmutableProjectJson.builder()
                .owner("Mustermann Verwaltung GmbH")
                .careOf("Max Mustermann")
                .billingAddress(ImmutableAddressJson.builder()
                    .street("Musterstraße 1")
                    .zip("10115")
                    .city("Berlin")
                    .province("Berlin")
                    .countryCode("DE")
                    .build())
                .build())
            .placeOfPerformance(ImmutableAddressJson.builder()
                .street("Hauptstraße 5")
                .zip("14467")
                .city("Potsdam")
                .province("Brandenburg")
                .countryCode("DE")
                .build())
            .rentalUnit(ImmutableRentalUnitNodeDataJson.builder()
                .id(UUID.randomUUID())
                .type(UnitType.APARTMENT)
                .title("Wohnung 3.2")
                .location("3. OG links")
                .build())
            .rentalAgreement(ImmutableRentalAgreementJson.builder()
                .addTenants(ImmutableTenantJson.builder()
                    .userId(UUID.randomUUID())
                    .firstName("Erika")
                    .lastName("Musterfrau")
                    .email("erika@example.com")
                    .mobilePhoneNumber("+491701234567")
                    .businessPhoneNumber("030123456")
                    .privatePhoneNumber("0331123456")
                    .build())
                .build())
            .quotationRequest(ImmutableQuotationRequestJson.builder().id(requestId).build())
            .build());

        final ArgumentCaptor<QuotationRequestEntity> captor = ArgumentCaptor.forClass(QuotationRequestEntity.class);
        verify(quotationRequestRepository).update(captor.capture());

        final QuotationRequestEntity updated = captor.getValue();
        assertEquals("Mustermann Verwaltung GmbH", updated.getProjectOwner());
        assertEquals("Max Mustermann", updated.getProjectCareOf());
        assertEquals("Musterstraße 1", updated.getProjectBillingAddress1());
        assertEquals("10115 Berlin", updated.getProjectBillingAddress2());
        assertEquals("Berlin, DE", updated.getProjectBillingAddress3());
        assertEquals("Hauptstraße 5", updated.getPlaceOfPerformanceAddress1());
        assertEquals("14467 Potsdam", updated.getPlaceOfPerformanceAddress2());
        assertEquals("Brandenburg, DE", updated.getPlaceOfPerformanceAddress3());
        assertEquals("Wohnung 3.2", updated.getRentalUnitTitle());
        assertEquals("3. OG links", updated.getRentalUnitLocation());
        assertEquals(1, updated.getTenants().size());
        assertEquals("Erika", updated.getTenants().get(0).getFirstName());
        assertEquals("Musterfrau", updated.getTenants().get(0).getLastName());
        assertEquals("erika@example.com", updated.getTenants().get(0).getEmail());
        assertEquals("+491701234567", updated.getTenants().get(0).getMobilePhoneNumber());
        assertEquals("030123456", updated.getTenants().get(0).getBusinessPhoneNumber());
        assertEquals("0331123456", updated.getTenants().get(0).getPrivatePhoneNumber());
    }

    @Test
    void testEnrichRequestForQuotation_withoutOptionalSections_preservesNullableFields() {
        final UUID issueId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();

        final QuotationRequestEntity entity = new QuotationRequestEntity();
        final QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(issueId);
        key.setRequestId(requestId);
        entity.setKey(key);
        when(quotationRequestRepository.findById(any())).thenReturn(Optional.of(entity));
        when(quotationRequestRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        controller.enrichRequestForQuotation(ImmutableIssueEventJson.builder()
            .issueId(issueId)
            .quotationRequest(ImmutableQuotationRequestJson.builder().id(requestId).build())
            .rentalAgreement(ImmutableRentalAgreementJson.builder().build())
            .build());

        final ArgumentCaptor<QuotationRequestEntity> captor = ArgumentCaptor.forClass(QuotationRequestEntity.class);
        verify(quotationRequestRepository).update(captor.capture());

        final QuotationRequestEntity updated = captor.getValue();
        assertNull(updated.getProjectOwner());
        assertNull(updated.getProjectBillingAddress1());
        assertNull(updated.getPlaceOfPerformanceAddress1());
        assertNull(updated.getRentalUnitTitle());
        assertNull(updated.getTenants());
    }
}
