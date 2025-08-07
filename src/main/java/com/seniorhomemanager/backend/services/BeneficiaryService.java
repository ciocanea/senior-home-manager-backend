package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.repositories.BeneficiaryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryService (BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public List<Beneficiary> getAll () {
        return beneficiaryRepository.findAll();
    }

    public Beneficiary get (UUID beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiary not found: " + beneficiaryId));
    }

    public Beneficiary add (Beneficiary beneficiary) {
        return beneficiaryRepository.save(beneficiary);
    }

    public void delete(UUID beneficiaryId) {
        if (!beneficiaryRepository.existsById(beneficiaryId)) {
            throw new EntityNotFoundException("Beneficiary not found: " + beneficiaryId);
        }

        beneficiaryRepository.deleteById(beneficiaryId);
    }
}
