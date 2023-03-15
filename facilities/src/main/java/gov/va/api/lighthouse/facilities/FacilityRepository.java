package gov.va.api.lighthouse.facilities;

import static java.util.Collections.emptySet;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

@Loggable
public interface FacilityRepository
    extends CrudRepository<FacilityEntity, FacilityEntity.Pk>,
        JpaSpecificationExecutor<FacilityEntity> {

  @Query("select e.id from #{#entityName} e")
  List<FacilityEntity.Pk> findAllIds();

  List<HasFacilityPayload> findAllProjectedBy();

  List<FacilityEntity> findByIdIn(Collection<FacilityEntity.Pk> ids);

  List<FacilityEntity> findByVisn(String visn);

  @Query("select max(e.lastUpdated) from #{#entityName} e")
  Instant findLastUpdated();

  abstract class ServicesSpecificationHelper implements Specification<FacilityEntity> {

    @SneakyThrows
    protected Predicate buildServicesPredicate(
        Root<FacilityEntity> root, CriteriaBuilder criteriaBuilder, Set<String> services) {
      Predicate[] servicePredicates =
          services.stream()
              .map(s -> criteriaBuilder.isMember(s, root.get("services")))
              .toArray(Predicate[]::new);
      return criteriaBuilder.or(servicePredicates);
    }
  }

  @Value
  @Builder
  @EqualsAndHashCode(callSuper = false)
  final class BoundingBoxSpecification extends ServicesSpecificationHelper {
    @NonNull BigDecimal minLongitude;

    @NonNull BigDecimal maxLongitude;

    @NonNull BigDecimal minLatitude;

    @NonNull BigDecimal maxLatitude;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<String> services = emptySet();

    Boolean mobile;

    @Override
    @SneakyThrows
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(5);
      basePredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("longitude"), minLongitude));
      basePredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("longitude"), maxLongitude));
      basePredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("latitude"), minLatitude));
      basePredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("latitude"), maxLatitude));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }

      if (mobile != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("mobile"), mobile));
      }

      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      return criteriaBuilder.and(
          combinedBase, buildServicesPredicate(root, criteriaBuilder, services));
    }
  }

  @Value
  @Builder
  @EqualsAndHashCode(callSuper = false)
  final class StateSpecification extends ServicesSpecificationHelper {
    @NonNull String state;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<String> services = emptySet();

    Boolean mobile;

    @Override
    @SneakyThrows
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(2);
      basePredicates.add(criteriaBuilder.equal(root.get("state"), state));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }

      if (mobile != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("mobile"), mobile));
      }

      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      return criteriaBuilder.and(
          combinedBase, buildServicesPredicate(root, criteriaBuilder, services));
    }
  }

  @Value
  @Builder
  @EqualsAndHashCode(callSuper = false)
  final class StationNumbersSpecification extends ServicesSpecificationHelper {
    @Builder.Default Set<String> stationNumbers = emptySet();

    FacilityEntity.Type facilityType;

    @Builder.Default Set<String> services = emptySet();

    @Override
    @SneakyThrows
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      if (isEmpty(stationNumbers)) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
      }

      List<Predicate> basePredicates = new ArrayList<>(2);

      CriteriaBuilder.In<String> stationsInClause =
          criteriaBuilder.in(root.get("id").get("stationNumber"));
      stationNumbers.forEach(stationsInClause::value);
      basePredicates.add(stationsInClause);

      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }

      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      return criteriaBuilder.and(
          combinedBase, buildServicesPredicate(root, criteriaBuilder, services));
    }
  }

  @Value
  @Builder
  @EqualsAndHashCode(callSuper = false)
  final class TypeServicesIdsSpecification extends ServicesSpecificationHelper {
    @Builder.Default Collection<FacilityEntity.Pk> ids = emptySet();

    FacilityEntity.Type facilityType;

    @Builder.Default Set<String> services = emptySet();

    Boolean mobile;

    @Override
    @SneakyThrows
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(2);
      if (!isEmpty(ids)) {
        CriteriaBuilder.In<FacilityEntity.Pk> idsInClause = criteriaBuilder.in(root.get("id"));
        ids.forEach(idsInClause::value);
        basePredicates.add(idsInClause);
      }
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }

      if (mobile != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("mobile"), mobile));
      }

      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      return criteriaBuilder.and(
          combinedBase, buildServicesPredicate(root, criteriaBuilder, services));
    }
  }

  @Value
  @Builder
  @EqualsAndHashCode(callSuper = false)
  final class ZipSpecification extends ServicesSpecificationHelper {
    @NonNull String zip;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<String> services = emptySet();

    Boolean mobile;

    @Override
    @SneakyThrows
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(2);
      basePredicates.add(criteriaBuilder.equal(root.get("zip"), zip));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }

      if (mobile != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("mobile"), mobile));
      }

      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      return criteriaBuilder.and(
          combinedBase, buildServicesPredicate(root, criteriaBuilder, services));
    }
  }
}
