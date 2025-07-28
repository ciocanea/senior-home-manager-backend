package com.seniorhomemanager.backend.models;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nume;
    private String prenume;

    private String cnp;
    private String serieCi;
    private String numarCi;

    private String oras;
    private String judet;

    private String strada;
    private String numarAdresa;

    private LocalDate dataEliberareCi;
    private String sectie;
}
