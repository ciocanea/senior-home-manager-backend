package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.models.Person;
import com.seniorhomemanager.backend.utils.DocumentEditor;
import com.seniorhomemanager.backend.utils.DocumentFiller;
import com.seniorhomemanager.backend.utils.DocumentSanitizer;
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

    private final DocumentFiller documentFiller;
    private final DocumentEditor documentEditor;
    private final DocumentSanitizer documentSanitizer;

    @Value("${document.folder.path:data/documents}")
    private String documentFolderPath;


    public DocumentService(DocumentFiller documentFiller, DocumentEditor documentEditor, DocumentSanitizer documentSanitizer) {
        this.documentFiller = documentFiller;
        this.documentEditor = documentEditor;
        this.documentSanitizer = documentSanitizer;
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
        if (beneficiary.getGuardian().getDataNasterii() != null) {
            dataNasteriiApartinator = beneficiary.getGuardian().getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataEliberareCiApartinator = "";
        if (beneficiary.getGuardian().getDataNasterii() != null) {
            dataEliberareCiApartinator = beneficiary.getGuardian().getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        Map<String, String> placeholderValues = Map.ofEntries(
                Map.entry("${data}", currentDate),

                Map.entry("${nume_BEN}", beneficiary.getNume()),
                Map.entry("${prenume_BEN}", beneficiary.getPrenume()),
                Map.entry("${nume_complet_BEN}", beneficiary.getNume() + " " + beneficiary.getPrenume()),
                Map.entry("${data_nasterii_BEN}", dataNasterii),
                Map.entry("${cnp_BEN}", beneficiary.getCnp()),
                Map.entry("${serie_ci_BEN}", beneficiary.getSerieCi()),
                Map.entry("${numar_ci_BEN}", beneficiary.getNumarCi()),
                Map.entry("${data_eliberare_ci_BEN}", dataEliberareCi),
                Map.entry("${sectie_BEN}", beneficiary.getSectie()),
                Map.entry("${adresa_BEN}", buildAddress(beneficiary)),
                Map.entry("${oras_BEN}", beneficiary.getOras()),
                Map.entry("${judet_BEN}", beneficiary.getJudet()),
                Map.entry("${strada_BEN}", beneficiary.getStrada()),
                Map.entry("${numar_adresa_BEN}", beneficiary.getNumarAdresa()),
                Map.entry("${bloc_BEN}", beneficiary.getBloc()),
                Map.entry("${scara_BEN}", beneficiary.getScara()),
                Map.entry("${etaj_BEN}", beneficiary.getEtaj()),
                Map.entry("${apartament_BEN}", beneficiary.getApartament()),

                Map.entry("${nume_APA}", beneficiary.getGuardian().getNume()),
                Map.entry("${prenume_APA}", beneficiary.getGuardian().getPrenume()),
                Map.entry("${nume_complet_APA}", beneficiary.getGuardian().getNume() + " " + beneficiary.getGuardian().getPrenume()),
                Map.entry("${data_nasterii_APA}", dataNasteriiApartinator),
                Map.entry("${cnp_APA}", beneficiary.getGuardian().getCnp()),
                Map.entry("${serie_ci_APA}", beneficiary.getGuardian().getSerieCi()),
                Map.entry("${numar_ci_APA}", beneficiary.getGuardian().getNumarCi()),
                Map.entry("${data_eliberare_ci_APA}", dataEliberareCiApartinator),
                Map.entry("${sectie_APA}", beneficiary.getGuardian().getSectie()),
                Map.entry("${adresa_APA}", buildAddress(beneficiary.getGuardian())),
                Map.entry("${oras_APA}", beneficiary.getGuardian().getOras()),
                Map.entry("${judet_APA}", beneficiary.getGuardian().getJudet()),
                Map.entry("${strada_APA}", beneficiary.getGuardian().getStrada()),
                Map.entry("${numar_adresa_APA}", beneficiary.getGuardian().getNumarAdresa()),
                Map.entry("${bloc_APA}", beneficiary.getGuardian().getBloc()),
                Map.entry("${scara_APA}", beneficiary.getGuardian().getScara()),
                Map.entry("${etaj_APA}", beneficiary.getGuardian().getEtaj()),
                Map.entry("${apartament_APA}", beneficiary.getGuardian().getApartament())
        );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentFiller.fillTemplate(document, outputStream, placeholderValues);
            return outputStream.toByteArray();
        }
    }

    public String buildAddress(Person person) {
        StringBuilder sb = new StringBuilder();

        sb.append(person.getOras().isEmpty() ? ".".repeat(30) : person.getOras());

        sb.append(", STR. ");
        sb.append(person.getStrada().isEmpty() ? ".".repeat(30) : person.getStrada());

        sb.append(", NR. ");
        sb.append(person.getNumarAdresa().isEmpty() ? ".".repeat(10) : person.getNumarAdresa());

        sb.append(person.getBloc().isEmpty() ? "" : ", BL. " + person.getBloc());
        sb.append(person.getScara().isEmpty() ? "" : ", SC. " + person.getScara());
        sb.append(person.getEtaj().isEmpty() ? "" : ", ET. " + person.getEtaj());
        sb.append(person.getApartament().isEmpty() ? "" : ", AP. " + person.getApartament());

        sb.append(", SECTOR/JUDEȚ ");
        sb.append(person.getJudet().isEmpty() ? ".".repeat(30) : person.getJudet());

        return sb.toString();
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

    public void upload (byte[] document, String documentName) {
        File folder = new File(documentFolderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Folder not found: " + documentFolderPath);
        }

        File newLocation = new File(folder, documentName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(newLocation)) {
            fileOutputStream.write(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
    }

    public byte[] sanitize (byte[] document, String documentName) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentSanitizer.sanitize(document, outputStream);
            return outputStream.toByteArray();
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
