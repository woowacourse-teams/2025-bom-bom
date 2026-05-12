package me.bombom.openapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import me.bombom.openapi.model.NewsletterSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ArticleResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-12T21:53:07.877682+09:00[Asia/Seoul]", comments = "Generator version: 7.10.0")
public class ArticleResponse {

  private Long articleId;

  private String title;

  private String contentsSummary;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime arrivedDateTime;

  private String thumbnailUrl = null;

  private Integer expectedReadTime;

  private Boolean isRead;

  private Boolean isBookmarked;

  private NewsletterSummaryResponse newsletter;

  public ArticleResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ArticleResponse(Long articleId, String title, String contentsSummary, OffsetDateTime arrivedDateTime, Integer expectedReadTime, Boolean isRead, Boolean isBookmarked, NewsletterSummaryResponse newsletter) {
    this.articleId = articleId;
    this.title = title;
    this.contentsSummary = contentsSummary;
    this.arrivedDateTime = arrivedDateTime;
    this.expectedReadTime = expectedReadTime;
    this.isRead = isRead;
    this.isBookmarked = isBookmarked;
    this.newsletter = newsletter;
  }

  public ArticleResponse articleId(Long articleId) {
    this.articleId = articleId;
    return this;
  }

  /**
   * 아티클 ID
   * @return articleId
   */
  @NotNull 
  @Schema(name = "articleId", description = "아티클 ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("articleId")
  public Long getArticleId() {
    return articleId;
  }

  public void setArticleId(Long articleId) {
    this.articleId = articleId;
  }

  public ArticleResponse title(String title) {
    this.title = title;
    return this;
  }

  /**
   * 아티클 제목
   * @return title
   */
  @NotNull 
  @Schema(name = "title", description = "아티클 제목", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public ArticleResponse contentsSummary(String contentsSummary) {
    this.contentsSummary = contentsSummary;
    return this;
  }

  /**
   * 본문 요약
   * @return contentsSummary
   */
  @NotNull 
  @Schema(name = "contentsSummary", description = "본문 요약", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("contentsSummary")
  public String getContentsSummary() {
    return contentsSummary;
  }

  public void setContentsSummary(String contentsSummary) {
    this.contentsSummary = contentsSummary;
  }

  public ArticleResponse arrivedDateTime(OffsetDateTime arrivedDateTime) {
    this.arrivedDateTime = arrivedDateTime;
    return this;
  }

  /**
   * 도착 일시
   * @return arrivedDateTime
   */
  @NotNull @Valid 
  @Schema(name = "arrivedDateTime", description = "도착 일시", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("arrivedDateTime")
  public OffsetDateTime getArrivedDateTime() {
    return arrivedDateTime;
  }

  public void setArrivedDateTime(OffsetDateTime arrivedDateTime) {
    this.arrivedDateTime = arrivedDateTime;
  }

  public ArticleResponse thumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
    return this;
  }

  /**
   * 썸네일 이미지 URL
   * @return thumbnailUrl
   */
  
  @Schema(name = "thumbnailUrl", description = "썸네일 이미지 URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("thumbnailUrl")
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public ArticleResponse expectedReadTime(Integer expectedReadTime) {
    this.expectedReadTime = expectedReadTime;
    return this;
  }

  /**
   * 예상 읽기 시간 (분)
   * @return expectedReadTime
   */
  @NotNull 
  @Schema(name = "expectedReadTime", description = "예상 읽기 시간 (분)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("expectedReadTime")
  public Integer getExpectedReadTime() {
    return expectedReadTime;
  }

  public void setExpectedReadTime(Integer expectedReadTime) {
    this.expectedReadTime = expectedReadTime;
  }

  public ArticleResponse isRead(Boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  /**
   * 읽음 여부
   * @return isRead
   */
  @NotNull 
  @Schema(name = "isRead", description = "읽음 여부", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isRead")
  public Boolean getIsRead() {
    return isRead;
  }

  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }

  public ArticleResponse isBookmarked(Boolean isBookmarked) {
    this.isBookmarked = isBookmarked;
    return this;
  }

  /**
   * 북마크 여부
   * @return isBookmarked
   */
  @NotNull 
  @Schema(name = "isBookmarked", description = "북마크 여부", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isBookmarked")
  public Boolean getIsBookmarked() {
    return isBookmarked;
  }

  public void setIsBookmarked(Boolean isBookmarked) {
    this.isBookmarked = isBookmarked;
  }

  public ArticleResponse newsletter(NewsletterSummaryResponse newsletter) {
    this.newsletter = newsletter;
    return this;
  }

  /**
   * Get newsletter
   * @return newsletter
   */
  @NotNull @Valid 
  @Schema(name = "newsletter", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("newsletter")
  public NewsletterSummaryResponse getNewsletter() {
    return newsletter;
  }

  public void setNewsletter(NewsletterSummaryResponse newsletter) {
    this.newsletter = newsletter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArticleResponse articleResponse = (ArticleResponse) o;
    return Objects.equals(this.articleId, articleResponse.articleId) &&
        Objects.equals(this.title, articleResponse.title) &&
        Objects.equals(this.contentsSummary, articleResponse.contentsSummary) &&
        Objects.equals(this.arrivedDateTime, articleResponse.arrivedDateTime) &&
        Objects.equals(this.thumbnailUrl, articleResponse.thumbnailUrl) &&
        Objects.equals(this.expectedReadTime, articleResponse.expectedReadTime) &&
        Objects.equals(this.isRead, articleResponse.isRead) &&
        Objects.equals(this.isBookmarked, articleResponse.isBookmarked) &&
        Objects.equals(this.newsletter, articleResponse.newsletter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(articleId, title, contentsSummary, arrivedDateTime, thumbnailUrl, expectedReadTime, isRead, isBookmarked, newsletter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArticleResponse {\n");
    sb.append("    articleId: ").append(toIndentedString(articleId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    contentsSummary: ").append(toIndentedString(contentsSummary)).append("\n");
    sb.append("    arrivedDateTime: ").append(toIndentedString(arrivedDateTime)).append("\n");
    sb.append("    thumbnailUrl: ").append(toIndentedString(thumbnailUrl)).append("\n");
    sb.append("    expectedReadTime: ").append(toIndentedString(expectedReadTime)).append("\n");
    sb.append("    isRead: ").append(toIndentedString(isRead)).append("\n");
    sb.append("    isBookmarked: ").append(toIndentedString(isBookmarked)).append("\n");
    sb.append("    newsletter: ").append(toIndentedString(newsletter)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

