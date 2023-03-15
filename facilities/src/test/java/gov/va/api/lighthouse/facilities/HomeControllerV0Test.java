package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ByteArrayResource;

public class HomeControllerV0Test {
  @Test
  void metadata() {
    Properties properties = new Properties();
    properties.put("version", "3.14");
    assertThat(
            HomeControllerV0.builder()
                .buildProperties(new BuildProperties(properties))
                .basePath("/bp")
                .build()
                .metadata())
        .isEqualTo(
            HomeControllerV0.Metadata.builder()
                .meta(
                    HomeControllerV0.Versions.builder()
                        .versions(
                            List.of(
                                HomeControllerV0.Version.builder()
                                    .version("3.14")
                                    .internalOnly(false)
                                    .status("Current Version")
                                    .path("/bp/docs/v0/api")
                                    .healthcheck("/bp/actuator/health")
                                    .build()))
                        .build())
                .build());
  }

  @Test
  @SneakyThrows
  void openapiJson() {
    assertThat(
            HomeControllerV0.builder()
                .openapi(new ByteArrayResource("{}".getBytes()))
                .basePath("")
                .build()
                .openapiJson())
        .isEqualTo("{}");
  }
}
