package me.bombom.api.v1.member.dto;

public record CategoryReadCount(
        Long id,
        String name,
        long count
) {
}
