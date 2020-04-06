package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.lighthouse.facilities.api.v0.ApiError;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class WebExceptionHandlerV0Test {
  private static HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  public void invalidParameter() {
    assertThat(
            new WebExceptionHandlerV0()
                .handleInvalidParameter(new ExceptionsV0.InvalidParameter("services", "x")))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Invalid field value")
                                    .detail("'x' is not a valid value for 'services'")
                                    .code("103")
                                    .status("400")
                                    .build()))
                        .build()));
  }

  @Test
  public void methodArgumentTypeMismatch() {
    assertThat(
            new WebExceptionHandlerV0()
                .handleMethodArgumentTypeMismatch(
                    new MethodArgumentTypeMismatchException(
                        "hello", Integer.class, "foo", null, null)))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Invalid field value")
                                    .detail("'hello' is not a valid value for 'foo'")
                                    .code("103")
                                    .status("400")
                                    .build()))
                        .build()));
  }

  @Test
  public void notAcceptable() {
    assertThat(
            new WebExceptionHandlerV0()
                .handleNotAcceptable(
                    new HttpMediaTypeNotAcceptableException(List.of(MediaType.ALL))))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Not acceptable")
                                    .detail(
                                        "The resource could not be returned in the requested format")
                                    .code("406")
                                    .status("406")
                                    .build()))
                        .build()));
  }

  @Test
  public void notFound() {
    assertThat(new WebExceptionHandlerV0().handleNotFound(new ExceptionsV0.NotFound("vha_555")))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Record not found")
                                    .detail("The record identified by vha_555 could not be found")
                                    .code("404")
                                    .status("404")
                                    .build()))
                        .build()));
  }

  @Test
  public void unsatisfiedServletRequestParameter() {
    assertThat(
            new WebExceptionHandlerV0()
                .handleUnsatisfiedServletRequestParameter(
                    new UnsatisfiedServletRequestParameterException(
                        new String[] {"hello"}, ImmutableMap.of("foo", new String[] {"bar"}))))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Missing parameter")
                                    .detail(
                                        "Parameter conditions \"hello\" not met for actual request parameters: foo={bar}")
                                    .code("108")
                                    .status("400")
                                    .build()))
                        .build()));
  }

  @Test
  public void validationException() {
    Set<ConstraintViolation<Foo>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(Foo.builder().build());
    assertThat(
            new WebExceptionHandlerV0()
                .handleValidationException(new ConstraintViolationException(violations)))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeaders())
                .body(
                    ApiError.builder()
                        .errors(
                            List.of(
                                ApiError.ErrorMessage.builder()
                                    .title("Invalid field value")
                                    .detail("bar must not be null")
                                    .code("400")
                                    .status("400")
                                    .build()))
                        .build()));
  }

  @Value
  @Builder
  private static final class Foo {
    @NotNull String bar;
  }
}
