package com.epam.ekc.search.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Author implements Serializable, Identifiable {

  private String id;

  private String firstName;

  private String lastName;

  @JsonIgnoreProperties("authors")
  private Book book;
}
