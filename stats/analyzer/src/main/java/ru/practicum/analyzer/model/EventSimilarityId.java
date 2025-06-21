package ru.practicum.analyzer.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarityId implements Serializable {

  private Long eventAId;
  private Long eventBId;

}
