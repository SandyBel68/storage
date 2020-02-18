package com.epam.ekc.storage.controller;

import com.epam.ekc.storage.model.Author;
import com.epam.ekc.storage.model.Book;
import com.epam.ekc.storage.services.AuthorDynamoService;
import com.epam.ekc.storage.services.BookDynamoService;
import com.epam.ekc.storage.services.SQSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/books")
public class BookController {

  private final BookDynamoService bookDynamoService;
  private final AuthorDynamoService authorDynamoService;
  private final SQSService sqsService;

  @PostMapping(consumes = APPLICATION_JSON_VALUE)
  public Book save(@RequestBody @Validated Book book) {
    book.setId(randomUUID().toString());
    savePassedAuthors(book.getAuthors());
    var savedBook = bookDynamoService.save(book);
    sqsService.sendMessage(savedBook);
    return savedBook;
  }

  @PostMapping(value = "/batch", consumes = APPLICATION_JSON_VALUE)
  public List<Book> saveAll(@RequestBody List<Book> books) {
    books.forEach(a -> a.setId(randomUUID().toString()));
    books.forEach(a -> savePassedAuthors(a.getAuthors()));
    List<Book> savedBooks = bookDynamoService.saveAll(books);
    for (Book book : savedBooks
    ) {
      sqsService.sendMessage(book);
    }
    return savedBooks;
  }

  @GetMapping("/{id}")
  public Book findOne(@PathVariable String id) {
    log.debug("Request to get Book : {}", id);
    return bookDynamoService.findById(id);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    log.debug("Request to delete Book : {}", id);
    bookDynamoService.deleteById(id);
  }

  private void savePassedAuthors(List<Author> authors) {
    authors.stream()
        .filter(author -> author.getId() == null)
        .forEach(author -> author.setId(randomUUID().toString()));
    authorDynamoService.saveAll(authors);
  }
}
