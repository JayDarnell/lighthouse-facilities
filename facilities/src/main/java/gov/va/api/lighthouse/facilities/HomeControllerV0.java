package gov.va.api.lighthouse.facilities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeControllerV0 {
  private final Resource openapi;

  private final String linkBase;

  private final BuildProperties buildProperties;

  @Builder
  HomeControllerV0(
      @Value("classpath:/v0/openapi.json") Resource openapi,
      @Value("${facilities.base-path}") String basePath,
      @Autowired BuildProperties buildProperties) {
    this.openapi = openapi;
    String strip = basePath.replaceAll("^/", "").replaceAll("/$", "");
    linkBase = strip.isEmpty() ? "/" : "/" + strip + "/";
    this.buildProperties = buildProperties;
  }

  @GetMapping(value = "/metadata", produces = "application/json")
  Metadata metadata() {
    return Metadata.builder()
        .meta(
            Versions.builder()
                .versions(
                    List.of(
                        Version.builder()
                            .version(buildProperties.getVersion())
                            .internalOnly(false)
                            .status("Current Version")
                            .path(linkBase + "docs/v0/api")
                            .healthcheck(linkBase + "actuator/health")
                            .build()))
                .build())
        .build();
  }

  @SneakyThrows
  private String openapiContent() {
    try (InputStream is = openapi.getInputStream()) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }

  @SneakyThrows
  @GetMapping(
      value = {"/", "/docs/v0/api", "/v0/facilities/openapi.json"},
      produces = "application/json")
  Object openapiJson() {
    return openapiContent();
  }

  @Builder
  @lombok.Value
  static final class Metadata {
    Versions meta;
  }

  @Builder
  @lombok.Value
  static final class Versions {
    List<Version> versions;
  }

  @Builder
  @lombok.Value
  static final class Version {
    String version;

    @JsonProperty("internal_only")
    Boolean internalOnly;

    String status;

    String path;

    String healthcheck;
  }
}
