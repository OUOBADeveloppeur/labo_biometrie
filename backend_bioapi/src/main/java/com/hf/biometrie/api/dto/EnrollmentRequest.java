package com.hf.biometrie.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnrollmentRequest {
    private String nom;
    private String prenom;
    private String sexe;
    private String dateNaissance;
    private String localite;
    private String faceToken;
    private String facePhotoBase64; // Image de la face en base64

    private List<BiometricTemplate> empreintes;
    private List<BiometricTemplate> iris;

    @Data
    public static class BiometricTemplate {
        private String position; // "left_0", "right_3", "left_iris", etc.
        private String templateBase64;
        private String imageBase64; // Image de l'empreinte
    }
}
