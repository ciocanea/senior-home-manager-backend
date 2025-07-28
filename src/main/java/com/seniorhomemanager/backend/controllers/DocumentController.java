package com.seniorhomemanager.backend.controllers;

import com.seniorhomemanager.backend.DTOs.DocumentRequestDTO;
import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.services.BeneficiaryService;
import com.seniorhomemanager.backend.services.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final BeneficiaryService beneficiaryService;

    @Autowired
    public DocumentController (DocumentService documentService, BeneficiaryService beneficiaryService) {
        this.documentService = documentService;
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate (@RequestBody DocumentRequestDTO documentRequestDTO) {
        try {
            String templateName = documentRequestDTO.getTemplateName();
            Beneficiary beneficiary = beneficiaryService.get(documentRequestDTO.getBeneficiaryId());

            byte[] document = documentService.generate(templateName, beneficiary);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateName + "\"")
                    .body(document);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
