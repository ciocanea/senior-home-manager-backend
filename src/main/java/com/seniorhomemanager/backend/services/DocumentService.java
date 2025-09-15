package com.seniorhomemanager.backend.services;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.models.Person;
import com.seniorhomemanager.backend.utils.DocumentEditor;
import com.seniorhomemanager.backend.utils.DocumentFiller;
import com.seniorhomemanager.backend.utils.DocumentSanitizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DocumentService {

    private final DocumentFiller documentFiller;
    private final DocumentEditor documentEditor;
    private final DocumentSanitizer documentSanitizer;
    private final BlobContainerClient containerClient;


    public DocumentService(
            DocumentFiller documentFiller,
            DocumentEditor documentEditor,
            DocumentSanitizer documentSanitizer,
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName
    ) {
        this.documentFiller = documentFiller;
        this.documentEditor = documentEditor;
        this.documentSanitizer = documentSanitizer;

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString.substring(1, connectionString.length()-1))
                .buildClient();

        this.containerClient = serviceClient.getBlobContainerClient(containerName);
    }

    public byte[] generate (String userId, String documentName, Beneficiary beneficiary) throws IllegalArgumentException, IOException {
        String blobName = userId + "/" +  documentName;

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException();
        }

        Map<String,String> placeholderValues = buildPlaceholdersValues(beneficiary);

        byte[] document = blobClient.downloadContent().toBytes();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentFiller.fillTemplate(document, outputStream, placeholderValues);
            return outputStream.toByteArray();
        }
    }

    public void edit (String userId, String documentName, List<String> placeholders) throws IllegalArgumentException, IOException {
        String blobName = userId + "/" +  documentName;

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException();
        }

        byte[] document = blobClient.downloadContent().toBytes();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentEditor.replacePlaceholders(document, outputStream, placeholders);
            upload(userId, documentName, outputStream.toByteArray());
        }
    }

    public void upload(String userId, String filename, byte[] content) {
        String blobName = userId + "/" + filename;

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(BinaryData.fromBytes(content), true);
    }

    public byte[] sanitize (byte[] document) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            documentSanitizer.sanitize(document, outputStream);
            return outputStream.toByteArray();
        }
    }

    public void delete (String userId, String documentName) throws IllegalArgumentException {
        String blobName = userId + "/" + documentName;

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.deleteIfExists()) {
            throw new IllegalArgumentException();
        }
    }


    public byte[] get(String userId, String documentName) throws IllegalArgumentException {
        String blobName = userId + "/" + documentName;

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Document not found: " + documentName);
        }

        return blobClient.downloadContent().toBytes();
    }

    public List<String> getNames(String userId) {
        String prefix = userId + "/";

        Iterable<BlobItem> blobItems = containerClient.listBlobs(
                new ListBlobsOptions().setPrefix(prefix), null);

        return StreamSupport.stream(blobItems.spliterator(), false)
                .map(BlobItem::getName)
                .filter(name -> name.endsWith(".docx"))
                .map(name -> name.substring(prefix.length()))
                .collect(Collectors.toList());
    }

    public Map<String,String> buildPlaceholdersValues (Beneficiary beneficiary) {

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String dataNasterii = "";
        if (beneficiary.getDataNasterii() != null) {
            dataNasterii = beneficiary.getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataEliberareCi = "";
        if (beneficiary.getDataEliberareCi() != null) {
            dataEliberareCi = beneficiary.getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataNasteriiApartinator = "";
        if (beneficiary.getGuardian().getDataNasterii() != null) {
            dataNasteriiApartinator = beneficiary.getGuardian().getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        String dataEliberareCiApartinator = "";
        if (beneficiary.getGuardian().getDataEliberareCi() != null) {
            dataEliberareCiApartinator = beneficiary.getGuardian().getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        return Map.ofEntries(
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
}
