package com.seniorhomemanager.backend.controllers;

import com.seniorhomemanager.backend.DTOs.DocumentRequestDTO;
import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController (DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate (@RequestBody DocumentRequestDTO documentRequestDTO) {
        try {
            String templateName = documentRequestDTO.getTemplateName();
            Beneficiary beneficiary = documentRequestDTO.getBeneficiary();

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            Map<String, String> placeholderValues = Map.of(
                    "${data}", currentDate,
                    "${nume}", beneficiary.getNume(),
                    "${prenume}", beneficiary.getPrenume()
            );

            byte[] document = documentService.generate(templateName, placeholderValues);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateName + "\"")
                    .body(document);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
