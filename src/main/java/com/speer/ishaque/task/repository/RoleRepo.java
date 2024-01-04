package com.speer.ishaque.task.repository;


import java.util.Optional;

import com.speer.ishaque.task.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface RoleRepo extends MongoRepository<Role, String> {
    Optional<Role> findByName(String name);
}