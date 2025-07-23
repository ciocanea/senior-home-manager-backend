package com.seniorhomemanager.backend.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Beneficiary extends Person{

    @ManyToOne
    private Guardian guardian;

    public Beneficiary(UUID id, String nume, String prenume, String cnp, String serieCi, String numarCi, Guardian guardian) {
        super(id, nume, prenume, cnp, serieCi, numarCi);
        this.guardian = guardian;
    }

//    private String locNastere;
//
//    private String domiciliu;
//
//    private String sex;
}
