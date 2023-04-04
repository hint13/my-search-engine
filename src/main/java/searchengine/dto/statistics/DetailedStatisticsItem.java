package searchengine.dto.statistics;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.Data;

@Data
public class DetailedStatisticsItem {
  private String url;
  private String name;
  private String status;
  private long statusTime;
  private String error;
  private int pages;
  private int lemmas;

  public void setStatusTimeFromLocalDateTime(LocalDateTime value) {
    ZoneOffset offset= ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
    Instant instant = value.toInstant(offset);
    this.setStatusTime(instant.toEpochMilli());
  }
}
