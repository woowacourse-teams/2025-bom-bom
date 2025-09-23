package me.bombom.api.v1.withdraw.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {
}
