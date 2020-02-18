package com.epam.ekc.storage.controller;

import com.epam.ekc.storage.model.Author;
import com.epam.ekc.storage.services.AuthorDynamoService;
import com.epam.ekc.storage.services.SQSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {

  private final AuthorDynamoService authorDynamoService;
  private final SQSService sqsService;

  @PostMapping(consumes = APPLICATION_JSON_VALUE)
  public Author save(@RequestBody Author author) {
    author.setId(randomUUID().toString());
    Author savedAuthor = authorDynamoService.save(author);
    sqsService.sendMessage(savedAuthor.getBook());
    return savedAuthor;
  }

  @PostMapping(value = "/batch", consumes = APPLICATION_JSON_VALUE)
  public List<Author> saveAll(@RequestBody List<Author> author) {
    author.forEach(a -> a.setId(randomUUID().toString()));
    return authorDynamoService.saveAll(author);
  }

  @GetMapping("/{id}")
  public Author findOne(@PathVariable String id) {
    log.debug("Request to get Authors : {}", id);
    return authorDynamoService.findById(id);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    log.debug("Request to delete Authors : {}", id);
    authorDynamoService.deleteById(id);
  }
}
