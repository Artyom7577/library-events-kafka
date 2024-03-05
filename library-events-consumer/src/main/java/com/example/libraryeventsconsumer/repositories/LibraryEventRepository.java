package com.example.libraryeventsconsumer.repositories;

import com.example.libraryeventsconsumer.entities.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Integer> {

}
