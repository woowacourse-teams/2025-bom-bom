package me.bombom.api.v1.highlight.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Color {

    private static final String COLOR_HEX_PATTERN = "^#[0-9a-fA-F]{6}$";

    private String value;

    @JsonCreator(mode = Mode.DELEGATING)
    public static Color from(String value) {
        validateFormat(value);
        return new Color(value);
    }

    private static void validateFormat(String value) {
        if (!Pattern.matches(COLOR_HEX_PATTERN, value)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE);
        }
    }
}
