package gov.va.api.lighthouse.facilities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static gov.va.api.lighthouse.facilities.collector.Transformers.allBlank;
import static gov.va.api.lighthouse.facilities.collector.Transformers.isBlank;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.facilities.api.FacilityPair;
import gov.va.api.lighthouse.facilities.api.ServiceType;
import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.cms.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.ReloadResponse;
import gov.va.api.lighthouse.facilities.collector.FacilitiesCollector;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/internal/management", produces = "application/json")
public class InternalFacilitiesController {
  static final String SPECIAL_INSTRUCTION_OLD_1 =
      "Expanded or Nontraditional hours are available for some services on a routine and "
          + "or requested basis. Please call our main phone number for details.";

  static final String SPECIAL_INSTRUCTION_UPDATED_1 =
      "More hours are available for some services. To learn more, call our main phone number.";

  static final String SPECIAL_INSTRUCTION_OLD_2 =
      "Vet Center after hours assistance is "
          + "available by calling 1-877-WAR-VETS (1-877-927-8387).";

  static final String SPECIAL_INSTRUCTION_UPDATED_2 =
      "If you need to talk to someone "
          + "or get advice right away, call the Vet Center anytime at 1-877-WAR-VETS "
          + "(1-877-927-8387).";

  static final String SPECIAL_INSTRUCTION_OLD_3 =
      "Administrative hours are Monday-Friday 8:00 a.m. to 4:30 p.m.";

  static final String SPECIAL_INSTRUCTION_UPDATED_3 =
      "Normal business hours are Monday through Friday, 8:00 a.m. to 4:30 p.m.";

  private static final String ZIP_REGEX = "^[0-9]{5}(-[0-9]{4})?$";

  private static final Pattern ZIP_PATTERN = Pattern.compile(ZIP_REGEX);

  private static final ObjectMapper MAPPER_V0 = FacilitiesJacksonConfigV0.createMapper();

  //  private static final ObjectMapper MAPPER_V1 = FacilitiesJacksonConfigV1.createMapper();

  private final FacilitiesCollector collector;

  private final CmsOverlayRepository cmsOverlayRepository;

  private final FacilityRepository facilityRepository;

  private final FacilityGraveyardRepository graveyardRepository;

  private static Optional<gov.va.api.lighthouse.facilities.api.v0.Facility.Address> addressMailing(
      gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return addresses(facility).map(a -> a.mailing());
  }

  private static Optional<gov.va.api.lighthouse.facilities.api.v0.Facility.Address> addressPhysical(
      gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return addresses(facility).map(a -> a.physical());
  }

  private static Optional<gov.va.api.lighthouse.facilities.api.v0.Facility.Addresses> addresses(
      gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return attributes(facility).map(a -> a.address());
  }

  private static Optional<gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityAttributes>
      attributes(gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return Optional.ofNullable(facility.attributes());
  }

  private static boolean isHoursNull(gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return facility.attributes().hours() == null;
  }

  /** Populate the given record with facility data _EXCEPT_ of the PK. */
  @SneakyThrows
  static FacilityEntity populate(FacilityEntity record, FacilityPair facilityPair) {

    gov.va.api.lighthouse.facilities.api.v0.Facility facilityV0 = facilityPair.v0();

    checkArgument(record.id() != null);
    record.latitude(facilityV0.attributes().latitude().doubleValue());
    record.longitude(facilityV0.attributes().longitude().doubleValue());
    record.state(stateOf(facilityV0));
    record.zip(zipOf(facilityV0));
    record.servicesFromServiceTypes(serviceTypesOf(facilityV0));
    record.facility(MAPPER_V0.writeValueAsString(facilityV0));
    record.visn(facilityV0.attributes().visn());
    record.mobile(facilityV0.attributes().mobile());

    //    gov.va.api.lighthouse.facilities.api.v1.Facility facilityV1 = facilityPair.v1();
    //    record.facilityV1(MAPPER_V1.writeValueAsString(facilityV1));
    return record;
  }

  /**
   * Determine the total collection of service types by combining health, benefits, and other
   * services types. This is guaranteed to return a non-null, but potentially empty collection.
   */
  static Set<ServiceType> serviceTypesOf(
      gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    var services = facility.attributes().services();
    if (services == null) {
      return Set.of();
    }
    var allServices = new HashSet<ServiceType>();
    if (services.health() != null) {
      allServices.addAll(services.health());
    }
    if (services.benefits() != null) {
      allServices.addAll(services.benefits());
    }
    if (services.other() != null) {
      allServices.addAll(services.other());
    }
    return allServices;
  }

  private static Optional<gov.va.api.lighthouse.facilities.api.v0.Facility.Services> services(
      gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    return attributes(facility).map(a -> a.services());
  }

  /** Determine the state if available in a physical address, otherwise return null. */
  static String stateOf(gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    if (facility.attributes().address() != null
        && facility.attributes().address().physical() != null
        && isNotBlank(facility.attributes().address().physical().state())) {
      return facility.attributes().address().physical().state();
    }
    return null;
  }

  /** Determine the 5 digit zip if available in a physical address, otherwise return null. */
  static String zipOf(gov.va.api.lighthouse.facilities.api.v0.Facility facility) {
    if (facility.attributes().address() != null
        && facility.attributes().address().physical() != null
        && isNotBlank(facility.attributes().address().physical().zip())) {
      /* We only store the destination portion of the zip code, we do not store the route. */
      return facility
          .attributes()
          .address()
          .physical()
          .zip()
          .substring(0, Math.min(5, facility.attributes().address().physical().zip().length()));
    }
    return null;
  }

  private Optional<CmsOverlayEntity> cmsOverlayEntityById(String id) {
    FacilityEntity.Pk pk = null;
    try {
      pk = FacilityEntity.Pk.fromIdString(id);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
    return cmsOverlayRepository.findById(pk);
  }

  /**
   * Delete an overlay if thisNodeOnly is not specified or partial overlay identified by
   * thisNodeOnly.
   */
  @DeleteMapping(value = {"/facilities/{id}/cms-overlay", "/facilities/{id}/cms-overlay/{node}"})
  ResponseEntity<Void> deleteCmsOverlayById(
      @PathVariable("id") String id,
      @PathVariable(value = "node", required = false) String thisNodeOnly) {
    CmsOverlayEntity overlayEntity = cmsOverlayEntityById(id).orElse(null);
    if (overlayEntity == null) {
      log.info("CmsOverlay {} does not exist, ignoring request.", sanitize(id));
      return ResponseEntity.accepted().build();
    }

    if (thisNodeOnly == null) {
      log.info("Deleting cms overlay for id: {}", sanitize(id));
      overlayEntity.cmsOperatingStatus(null);
      overlayEntity.cmsServices(null);
    } else if (thisNodeOnly.equalsIgnoreCase("operating_status")) {
      if (overlayEntity.cmsOperatingStatus() == null) {
        log.info("CmsOverlay {} does not have an operating_status, ignoring request", sanitize(id));
        return ResponseEntity.accepted().build();
      }
      log.info("Deleting operating_status node from overlay for id: {}", sanitize(id));
      overlayEntity.cmsOperatingStatus(null);
    } else if (thisNodeOnly.equalsIgnoreCase("detailed_services")) {
      if (overlayEntity.cmsServices() == null) {
        log.info("CmsOverlay {} does not have detailed_services, ignoring request", sanitize(id));
        return ResponseEntity.accepted().build();
      }
      log.info("Deleting detailed_services node from overlay for id: {}", sanitize(id));
      overlayEntity.cmsServices(null);
    } else {
      log.info("CmsOverlay field {} does not exist.", sanitize(thisNodeOnly));
      throw new ExceptionsUtils.NotFound(thisNodeOnly);
    }

    if (overlayEntity.cmsOperatingStatus() == null && overlayEntity.cmsServices() == null) {
      cmsOverlayRepository.delete(overlayEntity);
    } else {
      cmsOverlayRepository.save(overlayEntity);
    }

    FacilityEntity facilityEntity = facilityEntityById(id).orElse(null);
    if (facilityEntity != null) {
      facilityEntity
          .cmsOperatingStatus(overlayEntity.cmsOperatingStatus())
          .cmsServices(overlayEntity.cmsServices());
      if (overlayEntity.cmsServices() == null) {
        facilityEntity.overlayServices(new HashSet<>());
      }
      facilityRepository.save(facilityEntity);
    }

    return ResponseEntity.ok().build();
  }

  @DeleteMapping(value = "/facilities/{id}")
  ResponseEntity<String> deleteFacilityById(@PathVariable("id") String id) {
    Optional<FacilityEntity> entity = facilityEntityById(id);
    if (entity.isEmpty()) {
      log.info("Facility {} does not exist, ignoring request.", sanitize(id));
      return ResponseEntity.accepted().build();
    }
    log.info("Deleting facility {}", sanitize(id));
    facilityRepository.delete(entity.get());
    return ResponseEntity.ok().build();
  }

  void deleteFromGraveyard(ReloadResponse response, FacilityGraveyardEntity entity) {
    try {
      graveyardRepository.delete(entity);
    } catch (Exception e) {
      log.error(
          "Failed to delete facility {} from graveyard: {}",
          entity.id().toIdString(),
          e.getMessage());
      response
          .problems()
          .add(
              ReloadResponse.Problem.of(
                  entity.id().toIdString(),
                  "Failed to delete facility from graveyard: " + e.getMessage()));
      throw e;
    }
  }

  private Optional<FacilityEntity> facilityEntityById(String id) {
    FacilityEntity.Pk pk = null;
    try {
      pk = FacilityEntity.Pk.fromIdString(id);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
    return facilityRepository.findById(pk);
  }

  private String findAndReplaceOperationalHoursSpecialInstructions(String instructions) {
    if (instructions == null) {
      return null;
    } else {
      // Look through the instructions for specific substrings and replace them if necessary
      if (instructions.contains(SPECIAL_INSTRUCTION_OLD_1)) {
        instructions =
            instructions.replace(SPECIAL_INSTRUCTION_OLD_1, SPECIAL_INSTRUCTION_UPDATED_1);
      }
      if (instructions.contains(SPECIAL_INSTRUCTION_OLD_2)) {
        instructions =
            instructions.replace(SPECIAL_INSTRUCTION_OLD_2, SPECIAL_INSTRUCTION_UPDATED_2);
      }
      if (instructions.contains(SPECIAL_INSTRUCTION_OLD_3)) {
        instructions =
            instructions.replace(SPECIAL_INSTRUCTION_OLD_3, SPECIAL_INSTRUCTION_UPDATED_3);
      }
    }
    return instructions;
  }

  @GetMapping("/graveyard")
  GraveyardResponse graveyardAll() {
    return GraveyardResponse.builder()
        .facilities(
            Streams.stream(graveyardRepository.findAll())
                .map(
                    z ->
                        GraveyardResponse.Item.builder()
                            .facility(
                                FacilitiesJacksonConfigV0.quietlyMap(
                                    MAPPER_V0,
                                    z.facility(),
                                    gov.va.api.lighthouse.facilities.api.v0.Facility.class))
                            //                            .facilityV1(
                            //                                FacilitiesJacksonConfigV1.quietlyMap(
                            //                                    MAPPER_V1,
                            //                                    z.facilityV1(),
                            //
                            // gov.va.api.lighthouse.facilities.api.v1.Facility.class))
                            .cmsOverlay(
                                CmsOverlay.builder()
                                    .operatingStatus(
                                        z.cmsOperatingStatus() == null
                                            ? null
                                            : FacilitiesJacksonConfigV0.quietlyMap(
                                                MAPPER_V0,
                                                z.cmsOperatingStatus(),
                                                gov.va.api.lighthouse.facilities.api.v0.Facility
                                                    .OperatingStatus.class))
                                    .detailedServices(
                                        z.cmsServices() == null
                                            ? null
                                            : List.of(
                                                FacilitiesJacksonConfigV0.quietlyMap(
                                                    MAPPER_V0,
                                                    z.cmsServices(),
                                                    DetailedService[].class)))
                                    .build())
                            .overlayServices(z.graveyardOverlayServices())
                            .missing(
                                z.missingTimestamp() == null
                                    ? null
                                    : Instant.ofEpochMilli(z.missingTimestamp()))
                            .lastUpdated(z.lastUpdated())
                            .build())
                .collect(toList()))
        .build();
  }

  private Set<FacilityEntity.Pk> missingIds(List<FacilityPair> collectedFacilities) {
    Set<FacilityEntity.Pk> newIds =
        collectedFacilities.stream()
            .map(f -> FacilityEntity.Pk.optionalFromIdString(f.v0().id()).orElse(null))
            .filter(Objects::nonNull)
            .collect(toCollection(LinkedHashSet::new));
    Set<FacilityEntity.Pk> oldIds = new LinkedHashSet<>(facilityRepository.findAllIds());
    return ImmutableSet.copyOf(Sets.difference(oldIds, newIds));
  }

  private void moveToGraveyard(ReloadResponse response, FacilityEntity entity) {
    FacilityEntity.Pk id = FacilityEntity.Pk.of(entity.id().type(), entity.id().stationNumber());
    try {
      Instant now = response.timing().completeCollection();
      response.facilitiesRemoved().add(id.toIdString());
      log.warn("Moving facility {} to graveyard.", id.toIdString());
      graveyardRepository.save(
          FacilityGraveyardEntity.builder()
              .id(id)
              .facility(entity.facility())
              //              .facilityV1(entity.facilityV1())
              .cmsOperatingStatus(entity.cmsOperatingStatus())
              .cmsServices(entity.cmsServices())
              .graveyardOverlayServices(
                  entity.overlayServices() == null ? null : new HashSet<>(entity.overlayServices()))
              .missingTimestamp(entity.missingTimestamp())
              .lastUpdated(now)
              .build());
      facilityRepository.delete(entity);
    } catch (Exception e) {
      log.error("Failed to move facility {} to graveyard: {}", id.toIdString(), e.getMessage());
      response
          .problems()
          .add(
              ReloadResponse.Problem.of(
                  id.toIdString(), "Failed to move facility to graveyard: " + e.getMessage()));
      throw e;
    }
  }

  @GetMapping(value = "/populate-cms-overlay-table")
  void populateCmsOverlayTable() {
    // parallel stream all facilities response
    // build entity for cms_overlay table
    // operating status AND/OR detailed services exist add to entity otherwise it will just be null
    // save entity
    // done after all processing completes
    boolean noErrors = true;
    try {
      log.warn("Attempting to save all facility overlay info to cms_overlay table.");
      Streams.stream(facilityRepository.findAll())
          .parallel()
          .filter(f -> f.cmsOperatingStatus() != null || f.cmsServices() != null)
          .forEach(
              f ->
                  cmsOverlayRepository.save(
                      CmsOverlayEntity.builder()
                          .id(f.id())
                          .cmsOperatingStatus(f.cmsOperatingStatus())
                          .cmsServices(f.cmsServices())
                          .build()));
    } catch (Exception e) {
      noErrors = false;
      log.error(
          "Failed to save all facility overlay info to cms_overlay table. {}", e.getMessage());
    }

    if (noErrors) {
      log.warn("Completed saving all facility overlay info to cms_overlay table!");
    }
  }

  private ResponseEntity<ReloadResponse> process(
      ReloadResponse response, List<FacilityPair> collectedFacilities) {
    response.timing().markCompleteCollection();
    log.info("Facilities collected: {}", collectedFacilities.size());
    try {
      collectedFacilities.parallelStream().forEach(f -> updateFacility(response, f));
      for (FacilityEntity.Pk missingId : missingIds(collectedFacilities)) {
        processMissingFacility(response, missingId);
      }
    } catch (Exception e) {
      log.error("Failed to process facilities: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    } finally {
      response.timing().markComplete();
    }
    return ResponseEntity.ok(response);
  }

  private void processMissingFacility(ReloadResponse response, FacilityEntity.Pk id) {
    Optional<FacilityEntity> optEntity = facilityRepository.findById(id);
    checkState(optEntity.isPresent());
    FacilityEntity entity = optEntity.get();
    Instant now = response.timing().completeCollection();
    if (entity.missingTimestamp() == null) {
      entity.missingTimestamp(now.toEpochMilli());
    }
    if (now.toEpochMilli() - entity.missingTimestamp() <= TimeUnit.HOURS.toMillis(24)) {
      saveAsMissing(response, entity);
      return;
    }
    moveToGraveyard(response, entity);
  }

  @GetMapping(value = "/reload")
  ResponseEntity<ReloadResponse> reload() {
    var response = ReloadResponse.start();
    var collectedFacilities = collector.collectFacilities();
    response.totalFacilities(collectedFacilities.size());
    return process(response, collectedFacilities);
  }

  private void saveAsMissing(ReloadResponse response, FacilityEntity entity) {
    FacilityEntity.Pk id = entity.id();
    try {
      response.facilitiesMissing().add(id.toIdString());
      log.warn("Marking facility {} as missing.", id.toIdString());
      facilityRepository.save(entity);
      return;
    } catch (Exception e) {
      log.error("Failed to mark facility {} as missing: {}", id.toIdString(), e.getMessage());
      response
          .problems()
          .add(
              ReloadResponse.Problem.of(
                  id.toIdString(), "Failed to mark facility as missing: " + e.getMessage()));
      throw e;
    }
  }

  @SneakyThrows
  void updateAndSave(ReloadResponse response, FacilityEntity record, FacilityPair facilityPair) {

    gov.va.api.lighthouse.facilities.api.v0.Facility facility = facilityPair.v0();

    facility
        .attributes()
        .operationalHoursSpecialInstructions(
            findAndReplaceOperationalHoursSpecialInstructions(
                facility.attributes().operationalHoursSpecialInstructions()));

    populate(record, facilityPair);
    record.missingTimestamp(null);
    record.lastUpdated(response.timing().completeCollection());
    /*
     * Determine if there is something wrong with the record, but it is still usable.
     */
    if (isBlank(record.zip()) || !ZIP_PATTERN.matcher(record.zip()).matches()) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing or invalid physical address zip"));
    }
    if (isBlank(record.state())) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing physical address state"));
    }
    if (isBlank(addressPhysical(facility).map(a -> a.city()))) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing physical address city"));
    }
    if (allBlank(
        addressPhysical(facility).map(a -> a.address1()),
        addressPhysical(facility).map(a -> a.address2()),
        addressPhysical(facility).map(a -> a.address3()))) {
      response
          .problems()
          .add(
              ReloadResponse.Problem.of(
                  facility.id(), "Missing physical address street information"));
    }
    // Mailing addresses only exist for cemeteries
    if (facility.attributes().facilityType()
        == gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.va_cemetery) {
      if (isBlank(addressMailing(facility).map(a -> a.zip()))
          || !ZIP_PATTERN.matcher(facility.attributes().address().mailing().zip()).matches()) {
        response
            .problems()
            .add(
                ReloadResponse.Problem.of(facility.id(), "Missing or invalid mailing address zip"));
      }
      if (isBlank(addressMailing(facility).map(a -> a.state()))) {
        response
            .problems()
            .add(ReloadResponse.Problem.of(facility.id(), "Missing mailing address state"));
      }
      if (isBlank(addressMailing(facility).map(a -> a.city()))) {
        response
            .problems()
            .add(ReloadResponse.Problem.of(facility.id(), "Missing mailing address city"));
      }
      if (allBlank(
          addressMailing(facility).map(a -> a.address1()),
          addressMailing(facility).map(a -> a.address2()),
          addressMailing(facility).map(a -> a.address3()))) {
        response
            .problems()
            .add(
                ReloadResponse.Problem.of(
                    facility.id(), "Missing mailing address street information"));
      }
    }
    if (facility.attributes().phone() == null || isBlank(facility.attributes().phone().main())) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing main phone number"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().monday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Monday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().tuesday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Tuesday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().wednesday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Wednesday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().thursday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Thursday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().friday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Friday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().saturday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Saturday"));
    }
    if (isHoursNull(facility) || isBlank(facility.attributes().hours().sunday())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing hours Sunday"));
    }
    // Currently classification is not populated for vet centers
    if (facility.attributes().facilityType()
            != gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.vet_center
        && isBlank(facility.attributes().classification())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing classification"));
    }
    if (record.latitude() > 90 || record.latitude() < -90) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing or invalid location latitude"));
    }
    if (record.longitude() > 180 || record.longitude() < -180) {
      response
          .problems()
          .add(ReloadResponse.Problem.of(facility.id(), "Missing or invalid location longitude"));
    }
    if ((facility.attributes().facilityType()
            == gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.va_benefits_facility)
        && isBlank(services(facility).map(s -> s.benefits()))) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing services"));
    }
    if ((facility.attributes().facilityType()
            == gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.va_health_facility)
        && isBlank(services(facility).map(s -> s.health()))) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing services"));
    }
    if ((facility.attributes().facilityType()
                == gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.va_health_facility
            || facility.attributes().facilityType()
                == gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityType.vet_center)
        && isBlank(record.visn())) {
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing VISN"));
    }
    try {
      facilityRepository.save(record);
    } catch (Exception e) {
      log.error("Failed to save facility record {}: {}", record.id(), e.getMessage());
      log.error("{}", record);
      response
          .problems()
          .add(
              ReloadResponse.Problem.of(facility.id(), "Failed to save record: " + e.getMessage()));
      throw e;
    }
  }

  private void updateFacility(ReloadResponse response, FacilityPair facilityPair) {
    FacilityEntity.Pk pk;

    gov.va.api.lighthouse.facilities.api.v0.Facility facility = facilityPair.v0();

    try {
      pk = FacilityEntity.Pk.fromIdString(facility.id());
    } catch (IllegalArgumentException e) {
      log.error("Cannot process facility {}, ID not understood", facility.id(), e);
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Cannot parse ID"));
      return;
    }
    if (facility.attributes().latitude() == null || facility.attributes().longitude() == null) {
      log.error("Cannot process facility {}, latitude and/or longitude is null", facility.id());
      response.problems().add(ReloadResponse.Problem.of(facility.id(), "Missing coordinates"));
      return;
    }
    var existing = facilityRepository.findById(pk);
    if (existing.isPresent()) {
      response.facilitiesUpdated().add(facility.id());
      log.warn("Updating facility {}", facility.id());
      updateAndSave(response, existing.get(), facilityPair);
      return;
    }
    var zombie = graveyardRepository.findById(pk);
    if (zombie.isPresent()) {
      response.facilitiesRevived().add(facility.id());
      log.warn("Reviving facility {}", facility.id());
      FacilityGraveyardEntity zombieEntity = zombie.get();
      // only thing to retain from graveyard is CMS overlay
      // all other fields will be populated in updateAndSave()
      FacilityEntity facilityEntity =
          FacilityEntity.builder()
              .id(pk)
              .cmsOperatingStatus(zombieEntity.cmsOperatingStatus())
              .cmsServices(zombieEntity.cmsServices())
              .overlayServices(
                  zombieEntity.graveyardOverlayServices() == null
                      ? null
                      : new HashSet<>(zombieEntity.graveyardOverlayServices()))
              .build();
      updateAndSave(response, facilityEntity, facilityPair);
      deleteFromGraveyard(response, zombieEntity);
      return;
    }
    response.facilitiesCreated().add(facility.id());
    log.warn("Creating new facility {}", facility.id());
    updateAndSave(response, FacilityEntity.builder().id(pk).build(), facilityPair);
  }

  @PostMapping(value = "/reload")
  @Loggable(arguments = false)
  ResponseEntity<ReloadResponse> upload(@RequestBody List<FacilityPair> collectedFacilities) {
    var response = ReloadResponse.start();
    return process(response, collectedFacilities);
  }
}
