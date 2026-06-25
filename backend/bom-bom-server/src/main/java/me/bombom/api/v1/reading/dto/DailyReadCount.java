package me.bombom.api.v1.reading.dto;

public record DailyReadCount(

        int dayOfMonth,
        long readCount
) {
}
