package com.seniorhomemanager.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nume;
    private String prenume;

    private String cnp;
    private String serieCi;
    private String numarCi;

//    private String locNastere;
//
//    private String domiciliu;
//
//    private String sex;


}
