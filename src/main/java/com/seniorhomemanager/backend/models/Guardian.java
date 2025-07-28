package com.seniorhomemanager.backend.models;

import jakarta.persistence.Entity;
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
public class Guardian extends Person {

//    public Guardian(UUID id, String nume, String prenume, String cnp, String serieCi, String numarCi, String oras, String judet, String strada, String numarAdresa, LocalDate dataEliberareCi, String sectie) {
//        super(id, nume, prenume, cnp, serieCi, numarCi, oras, judet, strada, numarAdresa, dataEliberareCi, sectie);
//    }

}
