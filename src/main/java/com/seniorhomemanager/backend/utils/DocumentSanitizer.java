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
            "nr", "str", "bl", "sc" ,"ap", "et", "C\\.N\\.P"
    };

    private static final String dotsSection = "(\\.{4,}|(‥|…){2,}|(\\.|‥|…){3,})";

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

        String fontFamily = runs.getFirst().getFontName();
        Double fontSize = runs.getFirst().getFontSizeAsDouble();
        if (fontSize == null) fontSize = 12.0;


        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }

            if (!run.getCTR().getTabList().isEmpty()) {
                fullText.append("\t");
            }

            if (!run.getCTR().getBrList().isEmpty()) {
                fullText.append("\n");
            }
        }

        String text = fullText.toString();

        // --- Pattern 1: Abbreviation followed by dot section without space ---
        String abbrevPattern = "(?i)\\b(" + String.join("|", ABBREVIATIONS) + ")" + dotsSection;
        text = text.replaceAll(abbrevPattern, "$1. $2");

        // --- Pattern 2: Placeholder dots followed by a capitalized word ---
        String pattern = dotsSection + "(?=\\s+[A-Z])";
        text = text.replaceAll(pattern, "$1 .");

        // --- Pattern 3: Dot section after a word without space ---
        String dotSectionAfterWord = "(\\w)" + dotsSection;
        text = text.replaceAll(dotSectionAfterWord, "$1 $2");

        // --- Pattern 4: Dot section before a word without space ---
        String dotSectionBeforeWord = dotsSection + "(?=\\w)";
        text = text.replaceAll(dotSectionBeforeWord, "$1 ");

//        // --- Pattern 5: Dot section after a word without space ---
//        String dotSectionAfterCharacter = "(\\))" + dotsSection;
//        text = text.replaceAll(dotSectionAfterWord, "$1 $2");

        // Pattern 6: Merge two placeholder-like dot sections separated by any whitespace
        String combineDotSections = dotsSection + "(\\s+)" + dotsSection;
        text = text.replaceAll(combineDotSections, "$1$1$1");

//        // Pattern 6: Dot section followed by new line
//        String dotSectionAtEnd = dotsSection + "(?=\\s*$)";
//        text = text.replaceAll(dotSectionAtEnd, "$1 .");

        if (text.matches(".*" + dotsSection + ".*")) {
            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            XWPFRun newRun = paragraph.createRun();
            newRun.setText(text, 0);
            newRun.setFontFamily(fontFamily);
            newRun.setFontSize(fontSize);
        }
    }
}
