package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;

public class HomeControllerV1Test {
  @Test
  void metadata() {
    Properties properties = new Properties();
    properties.put("version", "3.14");
    assertThat(
            HomeControllerV1.builder()
                .buildProperties(new BuildProperties(properties))
                .basePath("/bp")
                .build()
                .metadata())
        .isEqualTo(
            HomeControllerV1.Metadata.builder()
                .meta(
                    HomeControllerV1.Versions.builder()
                        .versions(
                            List.of(
                                HomeControllerV1.Version.builder()
                                    .version("3.14")
                                    .internalOnly(false)
                                    .status("Current Version")
                                    .path("/bp/docs/v1/api")
                                    .healthcheck("/bp/actuator/health")
                                    .build()))
                        .build())
                .build());
  }

  @Test
  @SneakyThrows
  void openapiJson() {
    assertThat(
            HomeControllerV1.builder()
                .openapi(new ByteArrayResource("{}".getBytes()))
                .basePath("")
                .build()
                .openapiJson())
        .isEqualTo("{}");
  }
}
