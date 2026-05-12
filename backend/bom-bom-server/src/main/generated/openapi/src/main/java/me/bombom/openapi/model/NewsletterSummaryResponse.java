package me.bombom.openapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * NewsletterSummaryResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-12T21:53:07.877682+09:00[Asia/Seoul]", comments = "Generator version: 7.10.0")
public class NewsletterSummaryResponse {

  private String name;

  private String imageUrl = null;

  private String category;

  public NewsletterSummaryResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public NewsletterSummaryResponse(String name, String category) {
    this.name = name;
    this.category = category;
  }

  public NewsletterSummaryResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * 뉴스레터명
   * @return name
   */
  @NotNull 
  @Schema(name = "name", description = "뉴스레터명", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NewsletterSummaryResponse imageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  /**
   * 이미지 URL
   * @return imageUrl
   */
  
  @Schema(name = "imageUrl", description = "이미지 URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imageUrl")
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public NewsletterSummaryResponse category(String category) {
    this.category = category;
    return this;
  }

  /**
   * 카테고리
   * @return category
   */
  @NotNull 
  @Schema(name = "category", description = "카테고리", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NewsletterSummaryResponse newsletterSummaryResponse = (NewsletterSummaryResponse) o;
    return Objects.equals(this.name, newsletterSummaryResponse.name) &&
        Objects.equals(this.imageUrl, newsletterSummaryResponse.imageUrl) &&
        Objects.equals(this.category, newsletterSummaryResponse.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, imageUrl, category);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewsletterSummaryResponse {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
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

