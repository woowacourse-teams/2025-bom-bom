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
 * PetResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-12T22:06:52.246236+09:00[Asia/Seoul]", comments = "Generator version: 7.10.0")
public class PetResponse {

  private Integer level;

  private Integer currentStageScore;

  private Integer requiredStageScore;

  private Boolean isAttended;

  public PetResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PetResponse(Integer level, Integer currentStageScore, Integer requiredStageScore, Boolean isAttended) {
    this.level = level;
    this.currentStageScore = currentStageScore;
    this.requiredStageScore = requiredStageScore;
    this.isAttended = isAttended;
  }

  public PetResponse level(Integer level) {
    this.level = level;
    return this;
  }

  /**
   * 펫 레벨
   * @return level
   */
  @NotNull 
  @Schema(name = "level", description = "펫 레벨", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("level")
  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public PetResponse currentStageScore(Integer currentStageScore) {
    this.currentStageScore = currentStageScore;
    return this;
  }

  /**
   * 현재 스테이지 점수
   * @return currentStageScore
   */
  @NotNull 
  @Schema(name = "currentStageScore", description = "현재 스테이지 점수", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currentStageScore")
  public Integer getCurrentStageScore() {
    return currentStageScore;
  }

  public void setCurrentStageScore(Integer currentStageScore) {
    this.currentStageScore = currentStageScore;
  }

  public PetResponse requiredStageScore(Integer requiredStageScore) {
    this.requiredStageScore = requiredStageScore;
    return this;
  }

  /**
   * 필요한 스테이지 점수
   * @return requiredStageScore
   */
  @NotNull 
  @Schema(name = "requiredStageScore", description = "필요한 스테이지 점수", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("requiredStageScore")
  public Integer getRequiredStageScore() {
    return requiredStageScore;
  }

  public void setRequiredStageScore(Integer requiredStageScore) {
    this.requiredStageScore = requiredStageScore;
  }

  public PetResponse isAttended(Boolean isAttended) {
    this.isAttended = isAttended;
    return this;
  }

  /**
   * 출석 여부
   * @return isAttended
   */
  @NotNull 
  @Schema(name = "isAttended", description = "출석 여부", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isAttended")
  public Boolean getIsAttended() {
    return isAttended;
  }

  public void setIsAttended(Boolean isAttended) {
    this.isAttended = isAttended;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PetResponse petResponse = (PetResponse) o;
    return Objects.equals(this.level, petResponse.level) &&
        Objects.equals(this.currentStageScore, petResponse.currentStageScore) &&
        Objects.equals(this.requiredStageScore, petResponse.requiredStageScore) &&
        Objects.equals(this.isAttended, petResponse.isAttended);
  }

  @Override
  public int hashCode() {
    return Objects.hash(level, currentStageScore, requiredStageScore, isAttended);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PetResponse {\n");
    sb.append("    level: ").append(toIndentedString(level)).append("\n");
    sb.append("    currentStageScore: ").append(toIndentedString(currentStageScore)).append("\n");
    sb.append("    requiredStageScore: ").append(toIndentedString(requiredStageScore)).append("\n");
    sb.append("    isAttended: ").append(toIndentedString(isAttended)).append("\n");
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

