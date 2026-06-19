package me.bombom.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public final class MutableClock extends Clock implements ResettableTestDouble {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private volatile Clock delegate = Clock.system(DEFAULT_ZONE);

    public void setDate(LocalDate date) {
        setInstant(date.atStartOfDay(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);
    }

    public void setInstant(Instant instant) {
        setInstant(instant, DEFAULT_ZONE);
    }

    public void setInstant(Instant instant, ZoneId zone) {
        delegate = Clock.fixed(Objects.requireNonNull(instant), Objects.requireNonNull(zone));
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return delegate.withZone(zone);
    }

    @Override
    public Instant instant() {
        return delegate.instant();
    }

    @Override
    public void reset() {
        delegate = Clock.system(DEFAULT_ZONE);
    }
}
