package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_newsletter", uniqueConstraints = {
        @UniqueConstraint(name = "uk_challenge_newsletter", columnNames = { "challenge_id", "newsletter_id" })
})
public class ChallengeNewsletter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long newsletterId;

    @Builder
    public ChallengeNewsletter(
            Long id,
            @NonNull Long challengeId,
            @NonNull Long newsletterId
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.newsletterId = newsletterId;
    }
}
