package com.seniorhomemanager.backend.repositories;

import com.seniorhomemanager.backend.models.HelloWorldModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HelloWorldRepository extends JpaRepository<HelloWorldModel, UUID> {
}