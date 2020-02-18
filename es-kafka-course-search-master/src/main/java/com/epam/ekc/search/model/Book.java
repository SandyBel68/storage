package com.epam.ekc.search.model;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book implements Serializable, Identifiable {

  private static final long serialVersionUID = 1L;

  private String id;
  @NotNull
  @NotEmpty
  private String customerId;
  private String type;
  private String genre;
  private String title;
  private String language;
  private Instant publicationDate;
  private List<Author> authors = new ArrayList<>();

}
