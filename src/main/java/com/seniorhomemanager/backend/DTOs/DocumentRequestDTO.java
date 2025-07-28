package com.seniorhomemanager.backend.DTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class DocumentRequestDTO {
    private String templateName;
    private UUID beneficiaryId;
}
