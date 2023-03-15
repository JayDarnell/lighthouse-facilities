package gov.va.api.lighthouse.facilities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ActuatorPathMappingConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/v0/healthcheck").setViewName("/actuator/health");
    registry.addViewController("/v1/healthcheck").setViewName("/actuator/health");
  }
}
