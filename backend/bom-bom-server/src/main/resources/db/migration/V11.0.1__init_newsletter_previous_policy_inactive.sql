INSERT INTO newsletter_previous_policy (
    newsletter_id,
    strategy,
    latest_count,
    fixed_count,
    exposure_ratio,
    created_at,
    updated_at
)
SELECT
    n.id,
    'INACTIVE',
    5,
    0,
    100,
    NOW(6),
    NOW(6)
FROM newsletter n
WHERE NOT EXISTS (
    SELECT 1
    FROM newsletter_previous_policy p
    WHERE p.newsletter_id = n.id
);


