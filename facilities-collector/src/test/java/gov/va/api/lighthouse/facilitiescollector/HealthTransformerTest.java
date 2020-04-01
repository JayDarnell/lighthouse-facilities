package gov.va.api.lighthouse.facilitiescollector;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ArrayListMultimap;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import org.junit.Test;

public class HealthTransformerTest {
  private void assertClassification(String classificationId, String featureCode, String expected) {
    assertThat(
            HealthTransformer.builder()
                .gis(
                    ArcGisHealths.Feature.builder()
                        .attributes(
                            ArcGisHealths.Attributes.builder()
                                .cocClassificationId(classificationId)
                                .featureCode(featureCode)
                                .build())
                        .build())
                .accessToCare(ArrayListMultimap.create())
                .accessToPwt(ArrayListMultimap.create())
                .dentalServiceFacilityIds(emptySet())
                .mentalHealthPhoneNumbers(emptyMap())
                .stopCodesMap(ArrayListMultimap.create())
                .websites(emptyMap())
                .build()
                .classification())
        .isEqualTo(expected);
  }

  @Test
  public void classification() {
    assertThat(
            HealthTransformer.builder()
                .gis(ArcGisHealths.Feature.builder().build())
                .accessToCare(ArrayListMultimap.create())
                .accessToPwt(ArrayListMultimap.create())
                .dentalServiceFacilityIds(emptySet())
                .mentalHealthPhoneNumbers(emptyMap())
                .stopCodesMap(ArrayListMultimap.create())
                .websites(emptyMap())
                .build()
                .classification())
        .isNull();

    assertClassification(null, null, null);
    assertClassification("1", null, "VA Medical Center (VAMC)");
    assertClassification("2", null, "Health Care Center (HCC)");
    assertClassification("3", null, "Multi-Specialty CBOC");
    assertClassification("4", null, "Primary Care CBOC");
    assertClassification("5", null, "Other Outpatient Services (OOS)");
    assertClassification("7", null, "Residential Care Site (MH RRTP/DRRTP) (Stand-Alone)");
    assertClassification("8", null, "Extended Care Site (Community Living Center) (Stand-Alone)");
    assertClassification("x", null, "x");
    assertClassification(null, "f", "f");
  }

  @Test
  public void empty() {
    assertThat(
            HealthTransformer.builder()
                .gis(ArcGisHealths.Feature.builder().build())
                .accessToCare(ArrayListMultimap.create())
                .accessToPwt(ArrayListMultimap.create())
                .dentalServiceFacilityIds(emptySet())
                .mentalHealthPhoneNumbers(emptyMap())
                .stopCodesMap(ArrayListMultimap.create())
                .websites(emptyMap())
                .build()
                .toFacility())
        .isNull();

    ArrayListMultimap<String, AccessToCareEntry> atc = ArrayListMultimap.create();
    atc.put("VHA_X", AccessToCareEntry.builder().build());
    ArrayListMultimap<String, AccessToPwtEntry> atp = ArrayListMultimap.create();
    atp.put("VHA_X", AccessToPwtEntry.builder().build());
    ArrayListMultimap<String, StopCode> sc = ArrayListMultimap.create();
    sc.put("VHA_X", StopCode.builder().build());

    assertThat(
            HealthTransformer.builder()
                .gis(
                    ArcGisHealths.Feature.builder()
                        .attributes(ArcGisHealths.Attributes.builder().stationNum("x").build())
                        .build())
                .accessToCare(atc)
                .accessToPwt(atp)
                .dentalServiceFacilityIds(emptySet())
                .mentalHealthPhoneNumbers(emptyMap())
                .stopCodesMap(sc)
                .websites(emptyMap())
                .build()
                .toFacility())
        .isEqualTo(Facility.builder().id("vha_x").type(Facility.Type.va_facilities).build());
  }
}
