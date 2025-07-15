package me.bombom.api.v1.member.repository;

import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
