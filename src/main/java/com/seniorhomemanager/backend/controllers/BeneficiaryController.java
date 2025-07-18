package com.seniorhomemanager.backend.controllers;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.services.BeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @Autowired
    public BeneficiaryController (BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public List<Beneficiary> getAll () {
        return beneficiaryService.getAll();
    }

    @PostMapping
    public Beneficiary add (@RequestBody Beneficiary beneficiary) {
        return beneficiaryService.add(beneficiary);
    }
}
