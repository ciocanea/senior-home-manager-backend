package com.seniorhomemanager.backend.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentEditor {
    public void replacePlaceholders (File document, OutputStream outputStream, List<String> placeholders) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(document);
                XWPFDocument modifiableDocument = new XWPFDocument(fileInputStream);
        ) {
            int placeholderIndex = 0;
            for (XWPFParagraph paragraph: modifiableDocument.getParagraphs()) {
                List<XWPFRun> runs = paragraph.getRuns();
                if (runs == null || runs.isEmpty()) continue;

                String fontFamily = runs.getFirst().getFontName();
                Double fontSize = runs.getFirst().getFontSizeAsDouble();
                if (fontSize == null) fontSize = 12.0;


                boolean edited = false;
                String paragraphText = paragraph.getText();

                Pattern pattern = Pattern.compile("(\\$\\{[^}]+}|\\.{4,}|(\\.|‥|…){3,}|(‥|…){2,})");
                Matcher matcher = pattern.matcher(paragraphText);

                StringBuilder replacedText = new StringBuilder();

                while (matcher.find() && placeholderIndex < placeholders.size()) {
                    String replacement = placeholders.get(placeholderIndex++);
                    if (!replacement.isEmpty()) {
                        edited = true;
                        matcher.appendReplacement(replacedText, Matcher.quoteReplacement(replacement));
                    }
                }

                matcher.appendTail(replacedText);

                if (edited) {
                    for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                        paragraph.removeRun(i);
                    }

                    XWPFRun newRun = paragraph.createRun();
                    newRun.setText(replacedText.toString());
                    newRun.setFontFamily(fontFamily);
                    newRun.setFontSize(fontSize);
                }

            }

            modifiableDocument.write(outputStream);
        }
    }
}
