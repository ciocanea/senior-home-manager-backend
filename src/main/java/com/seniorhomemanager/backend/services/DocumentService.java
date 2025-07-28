package com.seniorhomemanager.backend.services;

import com.seniorhomemanager.backend.models.Beneficiary;
import com.seniorhomemanager.backend.utils.TemplateFiller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DocumentService {

    private final TemplateFiller templateFiller;

    @Value("${template.folder.path:data/templates}")
    private String templateFolderPath;


    public DocumentService(TemplateFiller templateFiller) {
        this.templateFiller = templateFiller;
    }

    public byte[] generate (String templateName, Beneficiary beneficiary) throws IOException {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Map<String, String> placeholderValues = Map.ofEntries(
                Map.entry("${data}", currentDate),

                Map.entry("${nume}", beneficiary.getNume()),
                Map.entry("${prenume}", beneficiary.getPrenume()),
                Map.entry("${data_nasterii}", beneficiary.getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
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
                Map.entry("${data_eliberare_ci}", beneficiary.getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                Map.entry("${sectie}", beneficiary.getSectie()),

                Map.entry("${nume_apartinator}", beneficiary.getGuardian().getNume()),
                Map.entry("${prenume_apartinator}", beneficiary.getGuardian().getPrenume()),
                Map.entry("${data_nasterii_apartinator}", beneficiary.getDataNasterii().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
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
                Map.entry("${data_eliberare_ci_apartinator}", beneficiary.getGuardian().getDataEliberareCi().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                Map.entry("${sectie_apartinator}", beneficiary.getGuardian().getSectie())
        );

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
