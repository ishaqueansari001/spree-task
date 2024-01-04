package com.speer.ishaque.task.repository;


import java.util.List;
import java.util.Optional;

import com.speer.ishaque.task.entity.User;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    List<User> findAllBy(TextCriteria criteria);
}