package me.bombom.api.v1.member.repository;

import java.util.Optional;
import me.bombom.api.v1.member.domain.MemberDiscordAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDiscordAccountRepository extends JpaRepository<MemberDiscordAccount, Long> {

    Optional<MemberDiscordAccount> findByMemberId(Long memberId);
}
