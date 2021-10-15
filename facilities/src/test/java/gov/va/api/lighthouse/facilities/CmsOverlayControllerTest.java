package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.updateServiceUrlPaths;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.cms.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.cms.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatusCode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CmsOverlayControllerTest {
  @Mock FacilityRepository mockFacilityRepository;

  @Mock CmsOverlayRepository mockCmsOverlayRepository;

  CmsOverlayController controller() {
    return CmsOverlayController.builder()
        .facilityRepository(mockFacilityRepository)
        .cmsOverlayRepository(mockCmsOverlayRepository)
        .build();
  }

  @Test
  @SneakyThrows
  void getExistingOverlay() {
    CmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                FacilitiesJacksonConfigV0.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                FacilitiesJacksonConfigV0.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DetailedService d : overlay.detailedServices()) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().overlay()).isEqualTo(overlay);
  }

  @Test
  void getNonExistingOverlay() {
    assertThatThrownBy(() -> controller().getOverlay("vha_041"))
        .isInstanceOf(ExceptionsUtils.NotFound.class)
        .hasMessage("The record identified by vha_041 could not be found");
  }

  private CmsOverlay overlay() {
    return CmsOverlay.builder()
        .operatingStatus(
            OperatingStatus.builder()
                .code(OperatingStatusCode.NOTICE)
                .additionalInfo("i need attention")
                .build())
        .detailedServices(
            List.of(
                DetailedService.builder()
                    .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                    .active(true)
                    .changed(null)
                    .descriptionFacility(null)
                    .appointmentLeadIn(
                        "Your VA health care team will contact you if you...more text")
                    .onlineSchedulingAvailable("True")
                    .path("replaceable path here")
                    .phoneNumbers(
                        List.of(
                            DetailedService.AppointmentPhoneNumber.builder()
                                .extension("123")
                                .label("Main phone")
                                .number("555-555-1212")
                                .type("tel")
                                .build()))
                    .referralRequired("True")
                    .walkInsAccepted("False")
                    .serviceLocations(
                        List.of(
                            DetailedService.DetailedServiceLocation.builder()
                                .serviceLocationAddress(
                                    DetailedService.DetailedServiceAddress.builder()
                                        .buildingNameNumber("Baxter Building")
                                        .clinicName("Baxter Clinic")
                                        .wingFloorOrRoomNumber("Wing East")
                                        .address1("122 Main St.")
                                        .address2(null)
                                        .city("Rochester")
                                        .state("NY")
                                        .zipCode("14623-1345")
                                        .countryCode("US")
                                        .build())
                                .appointmentPhoneNumbers(
                                    List.of(
                                        DetailedService.AppointmentPhoneNumber.builder()
                                            .extension("567")
                                            .label("Alt phone")
                                            .number("556-565-1119")
                                            .type("tel")
                                            .build()))
                                .emailContacts(
                                    List.of(
                                        DetailedService.DetailedServiceEmailContact.builder()
                                            .emailAddress("georgea@va.gov")
                                            .emailLabel("George Anderson")
                                            .build()))
                                .facilityServiceHours(
                                    DetailedService.DetailedServiceHours.builder()
                                        .monday("8:30AM-7:00PM")
                                        .tuesday("8:30AM-7:00PM")
                                        .wednesday("8:30AM-7:00PM")
                                        .thursday("8:30AM-7:00PM")
                                        .friday("8:30AM-7:00PM")
                                        .saturday("8:30AM-7:00PM")
                                        .sunday("CLOSED")
                                        .build())
                                .additionalHoursInfo("Please call for an appointment outside...")
                                .build()))
                    .build()))
        .build();
  }

  @Test
  @SneakyThrows
  void updateIsAcceptedForKnownStation() {
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    FacilityEntity entity =
        FacilityEntity.builder()
            .id(pk)
            .facility(FacilitiesJacksonConfigV0.createMapper().writeValueAsString(f))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(entity));
    CmsOverlay overlay = overlay();
    ResponseEntity<Void> response = controller().saveOverlay("vha_402", overlay);
    Set<String> detailedServices = new HashSet<>();
    for (DetailedService service : overlay.detailedServices()) {
      if (service.active()) {
        detailedServices.add(service.name());
      }
    }
    entity.cmsOperatingStatus(
        FacilitiesJacksonConfigV0.createMapper().writeValueAsString(overlay.operatingStatus()));
    entity.overlayServices(detailedServices);
    verify(mockFacilityRepository).save(entity);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void updateIsSkippedForUnknownStation() {
    var pk = FacilityEntity.Pk.fromIdString("vha_666");
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.empty());
    ResponseEntity<Void> response = controller().saveOverlay("vha_666", overlay());
    verifyNoMoreInteractions(mockFacilityRepository);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  @SneakyThrows
  void updateWithExistingOverlay() {
    CmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                FacilitiesJacksonConfigV0.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                FacilitiesJacksonConfigV0.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    List<DetailedService> additionalServices =
        List.of(
            DetailedService.builder().name("additional service1").active(true).build(),
            DetailedService.builder().name("additional service2").active(true).build());
    overlay.detailedServices(additionalServices);
    controller().saveOverlay("vha_402", overlay);
    CmsOverlay updatedCovidPathOverlay = overlay();
    updateServiceUrlPaths("vha_402", updatedCovidPathOverlay.detailedServices());
    List<DetailedService> combinedServices =
        Streams.stream(
                Iterables.concat(updatedCovidPathOverlay.detailedServices(), additionalServices))
            .toList();
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DetailedService d : combinedServices) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().overlay().detailedServices()).containsAll(combinedServices);
  }

  @Test
  @SneakyThrows
  void verifyServicePathUpdated() {
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    FacilityEntity entity =
        FacilityEntity.builder()
            .id(pk)
            .facility(FacilitiesJacksonConfigV0.createMapper().writeValueAsString(f))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(entity));
    CmsOverlay overlay = overlay();
    for (DetailedService d : overlay.detailedServices()) {
      if (d.name().equals(CMS_OVERLAY_SERVICE_NAME_COVID_19)) {
        assertThat(d.path()).isEqualTo("replaceable path here");
      }
    }
    controller().saveOverlay("vha_402", overlay);
    for (DetailedService d : overlay.detailedServices()) {
      if (d.name().equals(CMS_OVERLAY_SERVICE_NAME_COVID_19)) {
        assertThat(d.path()).isEqualTo("https://www.maine.va.gov/services/covid-19-vaccines.asp");
      }
    }
  }
}
