package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Guardian;
import com.seniorhomemanager.backend.repositories.GuardianRepository;
import org.springframework.stereotype.Service;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;

    public GuardianService (GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    public Guardian add (Guardian guardian) {
        return guardianRepository.save(guardian);
    }
}
