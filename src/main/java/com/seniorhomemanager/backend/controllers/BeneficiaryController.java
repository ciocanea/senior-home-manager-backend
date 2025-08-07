package com.seniorhomemanager.backend.controllers;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.models.Guardian;
import com.seniorhomemanager.backend.services.BeneficiaryService;
import com.seniorhomemanager.backend.services.GuardianService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final GuardianService guardianService;


    @Autowired
    public BeneficiaryController (BeneficiaryService beneficiaryService, GuardianService guardianService) {
        this.beneficiaryService = beneficiaryService;
        this.guardianService = guardianService;
    }

    @GetMapping
    public List<Beneficiary> getAll () {
        return beneficiaryService.getAll();
    }

    @PostMapping
    public Beneficiary add (@RequestBody Beneficiary beneficiary) {
        Guardian guardian = guardianService.add(beneficiary.getGuardian());
        beneficiary.setGuardian(guardian);
        return beneficiaryService.add(beneficiary);
    }

    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> delete (@PathVariable UUID beneficiaryId) {
        try {
            beneficiaryService.delete(beneficiaryId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
