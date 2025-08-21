package me.bombom.api.v1.highlight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HighlightLocation {
    @Column(name = "start_offset")
    private int startOffset;
    @Column(name = "start_x_path")
    private String startXPath;
    @Column(name = "end_offset")
    private int endOffset;
    @Column(name = "end_x_path")
    private String endXPath;
}
