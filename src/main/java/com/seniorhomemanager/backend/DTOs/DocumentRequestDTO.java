package com.seniorhomemanager.backend.DTOs;

import com.seniorhomemanager.backend.models.Beneficiary;
import lombok.Data;

@Data
public class DocumentRequestDTO {
    private String templateName;
    private Beneficiary beneficiary;
}
