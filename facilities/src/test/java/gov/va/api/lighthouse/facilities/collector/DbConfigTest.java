package gov.va.api.lighthouse.facilities.collector;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
public class DbConfigTest {
  @Test
  @Bean
  void jdbcTemplate() {
    DbConfig dbConfig = new DbConfig();
    JdbcTemplate jdbcTemplate =
        dbConfig.jdbcTemplate(
            "org.h2.Driver",
            "password",
            "jdbc:h2:mem:dbconfig;DB_CLOSE_DELAY=-1",
            "username",
            1,
            2,
            500);
    assertThat(jdbcTemplate.getDataSource()).isInstanceOf(HikariDataSource.class);
  }
}
