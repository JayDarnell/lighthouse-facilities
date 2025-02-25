package gov.va.api.lighthouse.facilities.collector;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TransformersTest {
  @Test
  void allBlank() {
    assertThat(Transformers.allBlank()).isTrue();
    assertThat(Transformers.allBlank(null, null, null, null)).isTrue();
    assertThat(Transformers.allBlank(null, "", " ")).isTrue();
    assertThat(Transformers.allBlank(emptyList(), emptyMap(), Optional.of(" "))).isTrue();
    assertThat(Transformers.allBlank(null, 1, null, null)).isFalse();
    assertThat(Transformers.allBlank(1, "x", "z", 2.0)).isFalse();
  }

  @Test
  void emptyToNull() {
    List<Object> list = new ArrayList<>();
    assertThat(Transformers.emptyToNull(list)).isNull();
    list.add(null);
    assertThat(Transformers.emptyToNull(list)).isNull();
    list.add("x");
    assertThat(Transformers.emptyToNull(list)).isEqualTo(List.of("x"));
  }

  @Test
  void hoursToClosed() {
    assertThat(Transformers.hoursToClosed(null)).isNull();
    assertThat(Transformers.hoursToClosed("")).isNull();
    assertThat(Transformers.hoursToClosed(" - ")).isEqualTo("Closed");
    assertThat(Transformers.hoursToClosed("24/7")).isEqualTo("24/7");
  }

  @Test
  void nullAngleBracket() {
    assertThat(Transformers.checkAngleBracketNull("<Null>")).isNull();
    assertThat(Transformers.checkAngleBracketNull(null)).isNull();
    assertThat(Transformers.checkAngleBracketNull("COOL COOL COOL")).isEqualTo("COOL COOL COOL");
  }

  @Test
  void phoneTrim() {
    assertThat(Transformers.phoneTrim(null)).isNull();
    assertThat(Transformers.phoneTrim("")).isEqualTo(null);
    assertThat(Transformers.phoneTrim("1 x")).isEqualTo("1");
    assertThat(Transformers.phoneTrim(" x ")).isNull();
    assertThat(Transformers.phoneTrim("000-000-0000")).isNull();
    assertThat(Transformers.phoneTrim("000-000-0000 x")).isNull();
  }
}
