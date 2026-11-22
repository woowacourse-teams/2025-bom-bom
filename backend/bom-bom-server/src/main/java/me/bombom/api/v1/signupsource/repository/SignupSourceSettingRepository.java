package me.bombom.api.v1.signupsource.repository;

import java.util.Optional;
import me.bombom.api.v1.signupsource.domain.SignupSourceSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupSourceSettingRepository extends JpaRepository<SignupSourceSetting, Long> {

    Optional<SignupSourceSetting> findTopByOrderByIdAsc();
}
