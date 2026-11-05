package com.hf.biometrie.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "commune")
@Data
@NoArgsConstructor
public class Commune {
    @Id
    private Long idcommune;
    private LocalDate createdat;
    private Long createdby;
    private LocalDate deletedat;
    private Long deletedby;
    private Boolean isdeleted;
    private LocalDate lastmodifiedat;
    private Long lastmodifiedby;
    private String libellecommune;
    private Long idprovince;
}

