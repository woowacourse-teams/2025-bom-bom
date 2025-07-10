package me.bombom.api.v1.newsletter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsLetter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 512)
    private String imageUrl;

    private String email;

    private String description;

    @Column(length=512)
    private String mainUrl;

    @Column(length=512)
    private String subscribeUrl;

    private String issueCycle;

    private Long categoryId;
}
