package com.seniorhomemanager.backend.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Component
public class DocumentFiller {

    public void fillTemplate (File templateDocument, OutputStream outputStream, Map<String, String> placeholderValues) throws IOException {
        try (
            FileInputStream fileInputStream = new FileInputStream(templateDocument);
            XWPFDocument modifiableDocument = new XWPFDocument(fileInputStream);
        ) {
            for (XWPFParagraph paragraph: modifiableDocument.getParagraphs()) {
                fillInParagraph(paragraph, placeholderValues);
            }

            for (XWPFTable table : modifiableDocument.getTables()) {
                fillInTable(table, placeholderValues);
            }

            modifiableDocument.write(outputStream);
        }
    }

    private void fillInParagraph(XWPFParagraph paragraph, Map<String, String> placeholderValues) {
        List<XWPFRun> runs = paragraph.getRuns();

        if (runs == null || runs.isEmpty()) return;

        String fontFamily = runs.getFirst().getFontFamily();
        Double fontSize = runs.getFirst().getFontSizeAsDouble();
        if (fontSize == null) fontSize = 12.0;

        StringBuilder paragraphText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                paragraphText.append(text);
            }
        }

        String replacedText = paragraphText.toString();
        for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
            String value = entry.getValue().isEmpty() ? ".".repeat(30) : entry.getValue().toUpperCase();
            replacedText = replacedText.replace(entry.getKey(), value);
        }

        if (!replacedText.contentEquals(paragraphText)) {

            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(replacedText);
            newRun.setFontFamily(fontFamily);
            newRun.setFontSize(fontSize);
        }
    }

    private void fillInTable(XWPFTable table, Map<String, String> placeholderValues) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    fillInParagraph(paragraph, placeholderValues);
                }
            }
        }
    }
}
