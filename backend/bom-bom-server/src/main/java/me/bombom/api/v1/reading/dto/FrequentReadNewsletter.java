package me.bombom.api.v1.reading.dto;

public record FrequentReadNewsletter(

        Long newsletterId,
        String name,
        long readCount
) {
}
