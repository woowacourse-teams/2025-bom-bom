package me.bombom.api.v1.member.dto.response;

public record RankHistoryResponse(
        String month,
        String label,
        long rank
) {

    public static RankHistoryResponse of(
            String month,
            String label,
            long rank
    ) {
        return new RankHistoryResponse(
                month,
                label,
                rank
        );
    }
}
