package com.speer.ishaque.task.repository;


import com.speer.ishaque.task.entity.Note;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface NoteRepo extends MongoRepository<Note, String> {

}