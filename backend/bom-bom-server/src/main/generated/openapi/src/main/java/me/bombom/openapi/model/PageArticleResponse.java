package me.bombom.openapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.bombom.openapi.model.ArticleResponse;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * 페이징된 아티클 목록 응답
 */

@Schema(name = "PageArticleResponse", description = "페이징된 아티클 목록 응답")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-12T22:06:52.246236+09:00[Asia/Seoul]", comments = "Generator version: 7.10.0")
public class PageArticleResponse {

  @Valid
  private List<@Valid ArticleResponse> content = new ArrayList<>();

  private Long totalElements;

  private Integer totalPages;

  private Integer number;

  private Integer size;

  private Integer numberOfElements;

  private Boolean first;

  private Boolean last;

  public PageArticleResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PageArticleResponse(List<@Valid ArticleResponse> content, Long totalElements, Integer totalPages, Integer number, Integer size, Integer numberOfElements, Boolean first, Boolean last) {
    this.content = content;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.number = number;
    this.size = size;
    this.numberOfElements = numberOfElements;
    this.first = first;
    this.last = last;
  }

  public PageArticleResponse content(List<@Valid ArticleResponse> content) {
    this.content = content;
    return this;
  }

  public PageArticleResponse addContentItem(ArticleResponse contentItem) {
    if (this.content == null) {
      this.content = new ArrayList<>();
    }
    this.content.add(contentItem);
    return this;
  }

  /**
   * Get content
   * @return content
   */
  @NotNull @Valid 
  @Schema(name = "content", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("content")
  public List<@Valid ArticleResponse> getContent() {
    return content;
  }

  public void setContent(List<@Valid ArticleResponse> content) {
    this.content = content;
  }

  public PageArticleResponse totalElements(Long totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  /**
   * 전체 데이터 개수
   * @return totalElements
   */
  @NotNull 
  @Schema(name = "totalElements", description = "전체 데이터 개수", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalElements")
  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

  public PageArticleResponse totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * 전체 페이지 수
   * @return totalPages
   */
  @NotNull 
  @Schema(name = "totalPages", description = "전체 페이지 수", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalPages")
  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public PageArticleResponse number(Integer number) {
    this.number = number;
    return this;
  }

  /**
   * 현재 페이지 번호 (0-base)
   * @return number
   */
  @NotNull 
  @Schema(name = "number", description = "현재 페이지 번호 (0-base)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("number")
  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public PageArticleResponse size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * 페이지 크기
   * @return size
   */
  @NotNull 
  @Schema(name = "size", description = "페이지 크기", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public PageArticleResponse numberOfElements(Integer numberOfElements) {
    this.numberOfElements = numberOfElements;
    return this;
  }

  /**
   * 현재 페이지 데이터 개수
   * @return numberOfElements
   */
  @NotNull 
  @Schema(name = "numberOfElements", description = "현재 페이지 데이터 개수", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("numberOfElements")
  public Integer getNumberOfElements() {
    return numberOfElements;
  }

  public void setNumberOfElements(Integer numberOfElements) {
    this.numberOfElements = numberOfElements;
  }

  public PageArticleResponse first(Boolean first) {
    this.first = first;
    return this;
  }

  /**
   * 첫 페이지 여부
   * @return first
   */
  @NotNull 
  @Schema(name = "first", description = "첫 페이지 여부", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("first")
  public Boolean getFirst() {
    return first;
  }

  public void setFirst(Boolean first) {
    this.first = first;
  }

  public PageArticleResponse last(Boolean last) {
    this.last = last;
    return this;
  }

  /**
   * 마지막 페이지 여부
   * @return last
   */
  @NotNull 
  @Schema(name = "last", description = "마지막 페이지 여부", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("last")
  public Boolean getLast() {
    return last;
  }

  public void setLast(Boolean last) {
    this.last = last;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageArticleResponse pageArticleResponse = (PageArticleResponse) o;
    return Objects.equals(this.content, pageArticleResponse.content) &&
        Objects.equals(this.totalElements, pageArticleResponse.totalElements) &&
        Objects.equals(this.totalPages, pageArticleResponse.totalPages) &&
        Objects.equals(this.number, pageArticleResponse.number) &&
        Objects.equals(this.size, pageArticleResponse.size) &&
        Objects.equals(this.numberOfElements, pageArticleResponse.numberOfElements) &&
        Objects.equals(this.first, pageArticleResponse.first) &&
        Objects.equals(this.last, pageArticleResponse.last);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, totalElements, totalPages, number, size, numberOfElements, first, last);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PageArticleResponse {\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    numberOfElements: ").append(toIndentedString(numberOfElements)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
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

