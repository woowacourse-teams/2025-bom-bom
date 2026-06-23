INSERT IGNORE INTO article_read_history (
    member_id,
    article_id,
    newsletter_id,
    category_id,
    read_at
)
SELECT
    a.member_id,
    a.id,
    a.newsletter_id,
    n.category_id,
    a.updated_at
FROM article a
JOIN newsletter n ON n.id = a.newsletter_id
WHERE a.is_read = TRUE
  AND a.updated_at IS NOT NULL;
