package me.bombom.api.v1.withdraw.event;

import java.time.LocalDate;

public record WithdrawEvent (
        Long memberId,
        LocalDate birthDate
){
}
