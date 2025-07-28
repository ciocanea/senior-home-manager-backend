package com.seniorhomemanager.backend.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Beneficiary extends Person{

    @ManyToOne
    private Guardian guardian;

//    public Beneficiary(UUID id, String nume, String prenume, String cnp, String serieCi, String numarCi, String oras, String judet, String strada, String numarAdresa, LocalDate dataEliberareCi, String sectie, Guardian guardian) {
//        super(id, nume, prenume, cnp, serieCi, numarCi, oras, judet, strada, numarAdresa, dataEliberareCi, sectie);
//        this.guardian = guardian;
//    }
}
