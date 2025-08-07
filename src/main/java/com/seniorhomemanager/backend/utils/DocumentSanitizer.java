package com.seniorhomemanager.backend.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component
public class DocumentSanitizer {

    private static final String[] ABBREVIATIONS = {
            "nr", "bl", "str", "et", "ap", "C\\.N\\.P"
    };

    public void sanitize (byte[] document, OutputStream outputStream) throws IOException {
        try (
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(document);
            XWPFDocument modifiableDocument = new XWPFDocument(byteArrayInputStream);
        ) {
            for (XWPFParagraph paragraph : modifiableDocument.getParagraphs()) {
                sanitizeParagraph(paragraph);
            }

            modifiableDocument.write(outputStream);
        }
    }

    private void sanitizeParagraph(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            fullText.append(run.getText(0));
        }

        String text = fullText.toString();

        // --- Pattern 1: Abbreviation followed by dot section without space ---
        String abbrevPattern = "\\b(" + String.join("|", ABBREVIATIONS) + ")\\.(?=([.‥…]{3,}))";
        text = text.replaceAll(abbrevPattern, "$1. ");

        // --- Pattern 2: Placeholder dots followed by a punctuation dot and capitalized word ---
        String pattern = "([.‥…]{3,})(\\.)(?=\\s+[A-Z])";
        text = text.replaceAll(pattern, "$1 $2");

        // --- Pattern 3: Dot section after a word without space ---
        String dotSectionAfterWord = "(\\w)([.‥…]{3,})";
        text = text.replaceAll(dotSectionAfterWord, "$1 $2");

        // --- Pattern 4: Dot section before a word without space ---
        String dotSectionBeforeWord = "([.‥…]{3,})(?=\\w)";
        text = text.replaceAll(dotSectionBeforeWord, "$1 ");

        // Pattern 5: Merge two placeholder-like dot sections separated by any whitespace
        String combineDotSections = "([.‥…]{3,})\\s+([.‥…]{3,})";
        text = text.replaceAll(combineDotSections, "$1$2");

        // Pattern 6: Dot section followed by new line
        String dotSectionAtEnd = "([.‥…]{3,})(\\.)(?=\\s*$)";
        text = text.replaceAll(dotSectionAtEnd, "$1 $2");

        if (text.matches(".*[.‥…]{3,}.*")) {
            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(text, 0);
        }
    }
}
