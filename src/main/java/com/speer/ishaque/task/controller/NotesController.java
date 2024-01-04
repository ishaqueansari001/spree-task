package com.speer.ishaque.task.controller;


import com.speer.ishaque.task.entity.Note;
import com.speer.ishaque.task.entity.User;
import com.speer.ishaque.task.repository.NoteRepo;
import com.speer.ishaque.task.repository.UserRepo;
import com.speer.ishaque.task.util.JwtUtils;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    NoteRepo noteRepository;

    @Autowired
    UserRepo userRepository;

    @GetMapping("")
    @PreAuthorize("hasAuthority('USER')")
    @RateLimiter(name="simpleRateLimit" ,fallbackMethod = "tryAfterSomeTime")
    public Set<Note> getNotes(@RequestHeader(name = "Authorization") String authorization) {
        String userName = jwtUtils.getUserNameFromJwtToken(authorization.split(" ")[1]);
        User u = userRepository.findByUsername(userName).get();
        return u.getNotes();
        // return noteRepository.findAll();
    }

    private Set<Note>  tryAfterSomeTime(@RequestHeader(name = "Authorization") String authorization, RequestNotPermitted requestNotPermitted){
        Note sampleNote= new Note();
        sampleNote.setId("12345");
        sampleNote.setText("this is sample note for you as server is busy");
        Set<Note> sampleNotes=new HashSet<Note>();
        sampleNotes.add(sampleNote);
        return sampleNotes ;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Note getNoteById(@PathVariable String id, @RequestHeader(name = "Authorization") String authorization) {
        String userName = jwtUtils.getUserNameFromJwtToken(authorization.split(" ")[1]);
        User u = userRepository.findByUsername(userName).get();
        return u.getNotes().stream().filter(note -> note.getId().equals(id)).findFirst().get();
        //   return noteRepository.findById(id).get();
    }

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public Note createNote(@RequestBody Note note, @RequestHeader(name = "Authorization") String authorization) {
        Note noteCreated = noteRepository.save(note);
        String userName = jwtUtils.getUserNameFromJwtToken(authorization.split(" ")[1]);
        User u = userRepository.findByUsername(userName).get();
        u.addNote(noteCreated);
        userRepository.save(u);
        return noteCreated;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Note updateNote(@PathVariable String id, @RequestBody Note newNote, @RequestHeader(name = "Authorization") String authorization) {
        newNote.setId(id);
        Note updatedNote = noteRepository.save(newNote);
        return updatedNote;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteNoteById(@PathVariable String id, @RequestHeader(name = "Authorization") String authorization) {

        noteRepository.deleteById(id);
        String userName = jwtUtils.getUserNameFromJwtToken(authorization.split(" ")[1]);
        User u = userRepository.findByUsername(userName).get();
        u.setNotes(u.getNotes().stream().filter(note -> !note.getId().equals(id)).collect(Collectors.toSet()));
        userRepository.save(u);
    }

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER')")
    public Collection<? extends Object> searchNotes(@RequestParam String query, @RequestHeader(name = "Authorization") String authorization) {
        String userName = jwtUtils.getUserNameFromJwtToken(authorization.split(" ")[1]);
        User u = userRepository.findByUsername(userName).get();
        Query query1 = TextQuery.queryText(TextCriteria.forDefaultLanguage().matching(query)).sortByScore();

        List<Note> notes = mongoTemplate.find(query1, Note.class);
        return notes;
    }


}