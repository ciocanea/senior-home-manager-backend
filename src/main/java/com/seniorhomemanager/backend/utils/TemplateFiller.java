package com.seniorhomemanager.backend.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Component
public class TemplateFiller {

    public void fillTemplate (File templateDocument, OutputStream outputStream, Map<String, String> placeholderValues) throws IOException {
        try (
            FileInputStream fileInputStream = new FileInputStream(templateDocument);
            XWPFDocument modifiableDocument = new XWPFDocument(fileInputStream);
        ) {
            for (XWPFParagraph paragraph: modifiableDocument.getParagraphs()) {
                fillInParagraph(paragraph, placeholderValues);
            }

            modifiableDocument.write(outputStream);
        }
    }

    private void fillInParagraph(XWPFParagraph paragraph, Map<String, String> placeholderValues) {
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
                run.setText(text, 0);
            }
        }
    }
}
