package news.bombomemail.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsletterSubscriptionCount {

    private static final int DECADE_UNIT = 10;
    private static final int MAX_DECADE = 6;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    Long newsletterId;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int age0s;

    @Column(nullable = false)
    private int age10s;

    @Column(nullable = false)
    private int age20s;

    @Column(nullable = false)
    private int age30s;

    @Column(nullable = false)
    private int age40s;

    @Column(nullable = false)
    private int age50s;

    @Column(nullable = false)
    private int age60plus;

    @Builder
    public NewsletterSubscriptionCount(
            Long id,
            @NonNull Long newsletterId,
            int total,
            int age0s,
            int age10s,
            int age20s,
            int age30s,
            int age40s,
            int age50s,
            int age60plus
    ) {
        this.id = id;
        this.newsletterId = newsletterId;
        this.total = total;
        this.age0s = age0s;
        this.age10s = age10s;
        this.age20s = age20s;
        this.age30s = age30s;
        this.age40s = age40s;
        this.age50s = age50s;
        this.age60plus = age60plus;
    }

    public static NewsletterSubscriptionCount from(Long newsletterId) {
        return NewsletterSubscriptionCount.builder()
                .newsletterId(newsletterId)
                .build();
    }

    public static int toDecadeBucket(int baseYear, int birthYear) {
        int age = baseYear - birthYear;
        if (age < 0) {
            throw new IllegalArgumentException("출생연도가 미래입니다: " + birthYear);
        }
        int decade = age / DECADE_UNIT;
        return Math.min(decade, MAX_DECADE);
    }
}
