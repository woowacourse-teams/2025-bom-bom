CREATE INDEX idx_badge_member_category_period
    ON badge (member_id, badge_category, period_year, period_month);

CREATE INDEX idx_badge_member_category_created_at
    ON badge (member_id, badge_category, created_at DESC);

CREATE INDEX idx_badge_member_category_streak
    ON badge (member_id, badge_category, streak_day_count DESC, created_at DESC);
