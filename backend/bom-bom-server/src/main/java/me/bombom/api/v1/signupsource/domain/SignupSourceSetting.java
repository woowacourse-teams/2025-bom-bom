package me.bombom.api.v1.signupsource.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

/**
 * 가입 경로를 물을 회원의 기준 시각. 마이그레이션이 적용된 시각으로 한 행을 넣는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupSourceSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime askSince;

    @Builder
    public SignupSourceSetting(Long id, @NonNull LocalDateTime askSince) {
        this.id = id;
        this.askSince = askSince;
    }
}
