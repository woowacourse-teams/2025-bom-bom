package me.bombom.api.v1.common.exception;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 서비스_예외는_에러상세에_맞는_응답으로_변환한다() {
        // given
        CIllegalArgumentException exception = new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext(ErrorContextKeys.MEMBER_ID, 1L)
                .addContext("entityType", "member");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(exception);

        // then
        assertError(response, HttpStatus.NOT_FOUND, "M003");
    }

    @Test
    void ConversionFailedException의_cause가_서비스_예외이면_해당_예외_응답을_사용한다() {
        // given
        ConversionFailedException exception = new ConversionFailedException(
                TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(Object.class),
                "UNKNOWN",
                new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
        );

        // when
        ResponseEntity<ErrorResponse> response = handler.handleConversionFailedException(exception);

        // then
        assertError(response, HttpStatus.BAD_REQUEST, "M001");
    }

    @Test
    void ConversionFailedException의_cause가_서비스_예외가_아니면_파라미터_검증_오류로_응답한다() {
        // given
        ConversionFailedException exception = new ConversionFailedException(
                TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(Object.class),
                "UNKNOWN",
                new IllegalArgumentException("failed")
        );

        // when
        ResponseEntity<ErrorResponse> response = handler.handleConversionFailedException(exception);

        // then
        assertError(response, HttpStatus.BAD_REQUEST, "M008");
    }

    @Test
    void MethodArgumentTypeMismatchException의_cause가_ConversionFailedException이면_변환_실패_응답을_사용한다()
            throws Exception {
        // given
        ConversionFailedException cause = new ConversionFailedException(
                TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(Object.class),
                "UNKNOWN",
                new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
        );
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "UNKNOWN",
                Object.class,
                "filter",
                methodParameter(),
                cause
        );

        // when
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        // then
        assertError(response, HttpStatus.BAD_REQUEST, "M001");
    }

    @Test
    void MethodArgumentTypeMismatchException의_cause가_서비스_예외이면_해당_예외_응답을_사용한다()
            throws Exception {
        // given
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "UNKNOWN",
                Object.class,
                "filter",
                methodParameter(),
                new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
        );

        // when
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        // then
        assertError(response, HttpStatus.BAD_REQUEST, "M001");
    }

    @Test
    void MethodArgumentTypeMismatchException의_cause가_알수없는_예외이면_파라미터_검증_오류로_응답한다()
            throws Exception {
        // given
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "UNKNOWN",
                Object.class,
                "filter",
                methodParameter(),
                new IllegalArgumentException("failed")
        );

        // when
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        // then
        assertError(response, HttpStatus.BAD_REQUEST, "M008");
    }

    @Test
    void 인증_예외와_서버_예외를_각각의_상태로_변환한다() {
        // given
        UnauthorizedException unauthorizedException = new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                .addContext(ErrorContextKeys.OPERATION, "login");
        CServerErrorException serverErrorException = new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                .addContext(ErrorContextKeys.OPERATION, "batch");

        // when
        ResponseEntity<ErrorResponse> unauthorizedResponse = handler.handleUnauthorizedException(unauthorizedException);
        ResponseEntity<ErrorResponse> serverErrorResponse = handler.handleCServerErrorException(serverErrorException);

        // then
        assertSoftly(softly -> {
            softly.assertThat(unauthorizedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            softly.assertThat(unauthorizedResponse.getBody().code()).isEqualTo("J002");
            softly.assertThat(serverErrorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            softly.assertThat(serverErrorResponse.getBody().code()).isEqualTo("M004");
        });
    }

    @Test
    void 검증_예외와_리소스_없음과_읽을수없는_body를_에러응답으로_변환한다() throws Exception {
        // given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "blank"));
        bindingResult.addError(new ObjectError("request", "invalid"));

        // when
        ResponseEntity<ErrorResponse> methodArgumentNotValid =
                handler.handleMethodArgumentNotValidException(
                        new MethodArgumentNotValidException(methodParameter(), bindingResult)
                );
        ResponseEntity<ErrorResponse> constraintViolation =
                handler.handleConstraintViolationException(new ConstraintViolationException(Set.of()));
        ResponseEntity<ErrorResponse> noResource =
                handler.handleNoResourceFoundException(new NoResourceFoundException(HttpMethod.GET, "/not-found"));
        ResponseEntity<ErrorResponse> notReadable =
                handler.handleNotReadable(new HttpMessageNotReadableException("invalid body"));

        // then
        assertSoftly(softly -> {
            softly.assertThat(methodArgumentNotValid.getBody().code()).isEqualTo("M011");
            softly.assertThat(constraintViolation.getBody().code()).isEqualTo("M008");
            softly.assertThat(noResource.getBody().code()).isEqualTo("M003");
            softly.assertThat(notReadable.getBody().code()).isEqualTo("M008");
        });
    }

    @Test
    void 처리하지_않은_예외는_서버_오류로_응답한다() {
        // when
        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("failed"));

        // then
        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "M004");
    }

    @Test
    void 컨텍스트가_없는_서비스_예외들도_에러상세에_맞게_응답한다() {
        // when
        ResponseEntity<ErrorResponse> illegalArgument =
                handler.handleIllegalArgumentException(new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE));
        ResponseEntity<ErrorResponse> unauthorized =
                handler.handleUnauthorizedException(new UnauthorizedException(ErrorDetail.UNAUTHORIZED));
        ResponseEntity<ErrorResponse> serverError =
                handler.handleCServerErrorException(new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR));

        // then
        assertSoftly(softly -> {
            softly.assertThat(illegalArgument.getBody().code()).isEqualTo("M001");
            softly.assertThat(unauthorized.getBody().code()).isEqualTo("J001");
            softly.assertThat(serverError.getBody().code()).isEqualTo("M004");
        });
    }

    private static void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String code) {
        ErrorResponse body = response.getBody();
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode()).isEqualTo(status);
            softly.assertThat(body).isNotNull();
            softly.assertThat(body.status()).isEqualTo(status);
            softly.assertThat(body.code()).isEqualTo(code);
            softly.assertThat(body.message()).isNotBlank();
        });
    }

    private static MethodParameter methodParameter() throws Exception {
        return new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("stub", String.class),
                0
        );
    }

    @SuppressWarnings("unused")
    private static void stub(String value) {
    }
}
