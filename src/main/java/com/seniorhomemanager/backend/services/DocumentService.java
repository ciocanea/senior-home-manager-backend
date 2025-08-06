package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.utils.DocumentEditor;
import com.seniorhomemanager.backend.utils.TemplateFiller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final TemplateFiller documentFiller;
    private final DocumentEditor documentEditor;

    @Value("${document.folder.path:data/documents}")
    private String documentFolderPath;


    public DocumentService(TemplateFiller documentFiller, DocumentEditor documentEditor) {
        this.documentFiller = documentFiller;
        this.documentEditor = documentEditor;
    }

    public byte[] generate (String documentName, Beneficiary beneficiary) throws IOException {
        File document = new File(documentFolderPath, documentName);

        if (!document.exists() || !document.isFile()) {
            throw new IllegalArgumentException("Document not found: " + documentName);
        }

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String dataNasterii = "";
        if (beneficiary.getDataNasterii() != null) {
            dataNasterii = beneficiary.getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataEliberareCi = "";
        if (beneficiary.getDataNasterii() != null) {
            dataEliberareCi = beneficiary.getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataNasteriiApartinator = "";
        if (beneficiary.getDataNasterii() != null) {
            dataNasteriiApartinator = beneficiary.getGuardian().getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataEliberareCiApartinator = "";
        if (beneficiary.getDataNasterii() != null) {
            dataEliberareCiApartinator = beneficiary.getGuardian().getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        Map<String, String> placeholderValues = Map.ofEntries(
                Map.entry("${data}", currentDate),

                Map.entry("${nume}", beneficiary.getNume()),
                Map.entry("${prenume}", beneficiary.getPrenume()),
                Map.entry("${data_nasterii}", dataNasterii),
                Map.entry("${cnp}", beneficiary.getCnp()),
                Map.entry("${serie_ci}", beneficiary.getSerieCi()),
                Map.entry("${numar_ci}", beneficiary.getNumarCi()),
                Map.entry("${oras}", beneficiary.getOras()),
                Map.entry("${judet}", beneficiary.getJudet()),
                Map.entry("${strada}", beneficiary.getStrada()),
                Map.entry("${numar_adresa}", beneficiary.getNumarAdresa()),
                Map.entry("${bloc}", beneficiary.getBloc()),
                Map.entry("${scara}", beneficiary.getScara()),
                Map.entry("${etaj}", beneficiary.getEtaj()),
                Map.entry("${apartament}", beneficiary.getApartament()),
                Map.entry("${data_eliberare_ci}", dataEliberareCi),
                Map.entry("${sectie}", beneficiary.getSectie()),

                Map.entry("${nume_apartinator}", beneficiary.getGuardian().getNume()),
                Map.entry("${prenume_apartinator}", beneficiary.getGuardian().getPrenume()),
                Map.entry("${data_nasterii_apartinator}", dataNasteriiApartinator),
                Map.entry("${cnp_apartinator}", beneficiary.getGuardian().getCnp()),
                Map.entry("${serie_ci_apartinator}", beneficiary.getGuardian().getSerieCi()),
                Map.entry("${numar_ci_apartinator}", beneficiary.getGuardian().getNumarCi()),
                Map.entry("${oras_apartinator}", beneficiary.getGuardian().getOras()),
                Map.entry("${judet_apartinator}", beneficiary.getGuardian().getJudet()),
                Map.entry("${strada_apartinator}", beneficiary.getGuardian().getStrada()),
                Map.entry("${bloc_apartinator}", beneficiary.getGuardian().getBloc()),
                Map.entry("${scara_apartinator}", beneficiary.getGuardian().getScara()),
                Map.entry("${etaj_apartinator}", beneficiary.getGuardian().getEtaj()),
                Map.entry("${apartament_apartinator}", beneficiary.getGuardian().getApartament()),
                Map.entry("${numar_adresa_apartinator}", beneficiary.getGuardian().getNumarAdresa()),
                Map.entry("${data_eliberare_ci_apartinator}", dataEliberareCiApartinator),
                Map.entry("${sectie_apartinator}", beneficiary.getGuardian().getSectie())
        );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentFiller.fillTemplate(document, outputStream, placeholderValues);
            return outputStream.toByteArray();
        }
    }

    public void edit (String documentName, List<String> placeholders) throws IOException {
        File document = new File(documentFolderPath, documentName);

        if (!document.exists() || !document.isFile()) {
            throw new IllegalArgumentException("Document not found: " + documentName);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentEditor.replacePlaceholders(document, outputStream, placeholders);
            upload(outputStream.toByteArray(), documentName);
        }
    }

    public void upload (MultipartFile newDocument) {
        File folder = new File(documentFolderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Folder not found: " + documentFolderPath);
        }

        File newLocation = new File(folder, newDocument.getOriginalFilename());

        try {
            newDocument.transferTo(newLocation.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
    }

    public void upload (byte[] fileData, String documentName) {
        File folder = new File(documentFolderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Folder not found: " + documentFolderPath);
        }

        File newLocation = new File(folder, documentName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(newLocation)) {
            fileOutputStream.write(fileData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
    }


    public void delete (String name) throws IOException{
        File file = new File(documentFolderPath + File.separator + name);


        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
    }

    public byte[] get (String documentName) throws IOException {
        File document = new File(documentFolderPath, documentName);

        if (!document.exists() || !document.isFile()) {
            throw new IllegalArgumentException("Document not found: " + documentName);
        }

        return Files.readAllBytes(document.toPath());
    }

    public List<String> getNames() throws IOException {
        File folder = new File(documentFolderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new FileNotFoundException("Folder not found: " + documentFolderPath);
        }

        return Arrays.stream(folder.listFiles())
                .filter(file -> file.isFile() && file.getName().endsWith(".docx"))
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
