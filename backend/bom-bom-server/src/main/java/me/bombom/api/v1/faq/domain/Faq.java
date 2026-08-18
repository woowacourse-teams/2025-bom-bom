package me.bombom.api.v1.faq.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String answer;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private FaqCategory faqCategory;

    @Builder
    public Faq(
            Long id,
            @NonNull String question,
            @NonNull String answer,
            @NonNull FaqCategory faqCategory
    ) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.faqCategory = faqCategory;
    }
}
