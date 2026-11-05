package com.hf.biometrie.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pays")
@Data
@NoArgsConstructor
public class Pays {
    @Id
    private Long idpays;
    private LocalDate createdat;
    private Long createdby;
    private LocalDate deletedat;
    private Long deletedby;
    private Boolean isdeleted;
    private LocalDate lastmodifiedat;
    private Long lastmodifiedby;
    private String libellepays;
}

