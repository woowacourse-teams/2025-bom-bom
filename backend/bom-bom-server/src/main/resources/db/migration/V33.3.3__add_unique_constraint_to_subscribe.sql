DELETE s1
FROM subscribe s1
         JOIN subscribe s2
              ON s1.member_id = s2.member_id
                  AND s1.newsletter_id = s2.newsletter_id
                  AND s1.id > s2.id;

ALTER TABLE subscribe
    ADD CONSTRAINT uk_subscribe_member_newsletter UNIQUE (member_id, newsletter_id);
