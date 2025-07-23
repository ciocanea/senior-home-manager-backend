package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.utils.TemplateFiller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
public class DocumentService {

    private final TemplateFiller templateFiller;

    @Value("${template.folder.path:data/templates}")
    private String templateFolderPath;


    public DocumentService(TemplateFiller templateFiller) {
        this.templateFiller = templateFiller;
    }

    public byte[] generate (String templateName, Map<String, String> placeholderValues) throws IOException {
        File templateFile = new File(templateFolderPath, templateName);

        if (!templateFile.exists() || !templateFile.isFile()) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            templateFiller.fillTemplate(templateFile, outputStream, placeholderValues);
            return outputStream.toByteArray();
        }
    }
}
