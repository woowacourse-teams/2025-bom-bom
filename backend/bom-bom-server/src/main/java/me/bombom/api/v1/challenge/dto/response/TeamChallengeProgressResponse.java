package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TeamChallengeProgressResponse(

        @NotNull
        ChallengeInfoResponse challengeInfoResponse,

        @Schema(required = true)
        int achievementAverage,

        @NotNull
        List<MemberDailyResultResponse> memberDailyResultResponses
) {
}

//{
//	"challenge": {
//		"startDate": 2026-01-05,
//		"endDate": 2026-02-04,
//		"totalDays": 24
//	}
//  "teamSummary": {
//    "achievementAverage": 63
//  },
//  "members": [
//    {
//      "memberId": 3,
//      "nickname": "동준",
//      "is_survived": false,
//      "dailyProgress": [
//        { "date": "2025-01-05", "status": "COMPLETE" },
//        { "date": "2025-01-05", "status": "SHIELD" }
//      ]
//    },
//    {
//      "memberId": 7,
//      "nickname": "철원",
//      "is_survived": true,
//      "dailyProgress": [
//        { "date": "2025-01-05", "status": "COMPLETE" },
//        { "date": "2025-01-06", "status": "COMPLETE" }
//      ]
//    }
//  ]
//}
