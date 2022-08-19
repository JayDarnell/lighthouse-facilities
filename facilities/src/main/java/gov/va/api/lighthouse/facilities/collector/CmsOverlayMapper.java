package gov.va.api.lighthouse.facilities.collector;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.CmsOverlayHelper;
import gov.va.api.lighthouse.facilities.CmsOverlayRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CmsOverlayMapper extends BaseCmsOverlayHandler implements ServiceDataMapper {

  private Map<String, String> serviceIdToServiceNameMapping;

  /**
   * Loader of CMS overlay service data for purposes of forming mapping between service id and
   * service name.
   */
  public CmsOverlayMapper(@Autowired CmsOverlayRepository cmsOverlayRepository) {
    super(cmsOverlayRepository);
    refreshServiceIdToServiceNameMapping();
  }

  /**
   * Form service id to service name mapping based on all CMS service overlays uploaded for all
   * facilities.
   */
  private Map<String, String> buildServiceIdToServiceNameMapping() {
    final Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    try {
      Streams.stream(cmsOverlayRepository.findAll())
          .filter(Objects::nonNull)
          .filter(cmsOverlayEntity -> isNotEmpty(cmsOverlayEntity.cmsServices()))
          .forEach(
              cmsOverlayEntity ->
                  CmsOverlayHelper.getDetailedServices(cmsOverlayEntity.cmsServices()).stream()
                      .forEach(
                          dds -> map.put(dds.serviceInfo().serviceId(), dds.serviceInfo().name())));
    } catch (final Exception ex) {
      log.error("Unable to build service id to service name mapping.", ex);
    }
    return ImmutableMap.copyOf(map);
  }

  /** Method for refreshing service id to service name mapping. Intended for use by daily reload. */
  public void refreshServiceIdToServiceNameMapping() {
    serviceIdToServiceNameMapping = buildServiceIdToServiceNameMapping();
  }

  @Override
  public Set<String> serviceIds() {
    return serviceIdToServiceNameMapping.keySet();
  }

  @Override
  public Optional<String> serviceNameForServiceId(@NonNull String serviceId) {
    return Optional.ofNullable(serviceIdToServiceNameMapping.get(serviceId));
  }
}
