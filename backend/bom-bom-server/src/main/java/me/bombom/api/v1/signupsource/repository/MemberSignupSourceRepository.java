package me.bombom.api.v1.signupsource.repository;

import me.bombom.api.v1.signupsource.domain.MemberSignupSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSignupSourceRepository extends JpaRepository<MemberSignupSource, Long> {

    boolean existsByMemberId(Long memberId);
}
