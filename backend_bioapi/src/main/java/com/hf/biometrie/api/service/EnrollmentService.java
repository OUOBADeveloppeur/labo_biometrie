package com.hf.biometrie.api.service;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Iris;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.exception.DoublonException;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Utilisateur saveEnrollment(EnrollmentRequest request) {

        // ── 1. Vérification par faceToken (identifiant biométrique facial unique) ──
        if (request.getFaceToken() != null && !request.getFaceToken().isBlank()) {
            Optional<Utilisateur> parToken = utilisateurRepository.findByFaceToken(request.getFaceToken());
            if (parToken.isPresent()) {
                throw new DoublonException(
                    "Token facial déjà enregistré",
                    parToken.get()
                );
            }
        }

        // ── 2. Vérification par identité civile (nom + prénom + date de naissance) ──
        if (request.getNom() != null && request.getPrenom() != null && request.getDateNaissance() != null) {
            Optional<Utilisateur> parIdentite = utilisateurRepository
                .findByNomIgnoreCaseAndPrenomIgnoreCaseAndDateNaissance(
                    request.getNom().trim(),
                    request.getPrenom().trim(),
                    request.getDateNaissance().trim()
                );
            if (parIdentite.isPresent()) {
                throw new DoublonException(
                    "Identité déjà enregistrée (Nom + Prénom + Date de naissance)",
                    parIdentite.get()
                );
            }
        }

        // ── 3. Aucun doublon détecté → enregistrement normal ──
        Utilisateur u = new Utilisateur();
        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setSexe(request.getSexe());
        u.setDateNaissance(request.getDateNaissance());
        u.setLocalite(request.getLocalite());
        u.setFaceToken(request.getFaceToken());
        u.setFacePhotoBase64(request.getFacePhotoBase64());

        if (request.getEmpreintes() != null) {
            for (EnrollmentRequest.BiometricTemplate et : request.getEmpreintes()) {
                Empreinte emp = new Empreinte();
                emp.setPositionDoigt(et.getPosition());
                emp.setTemplateBase64(et.getTemplateBase64());
                emp.setImageBase64(et.getImageBase64());
                emp.setDeviceModel(et.getDeviceModel());
                emp.setQualite(et.getQualite());
                emp.setTempsCaptureMs(et.getTempsCaptureMs());
                emp.setUtilisateur(u);
                u.getEmpreintes().add(emp);
            }
        }

        if (request.getIris() != null) {
            for (EnrollmentRequest.BiometricTemplate it : request.getIris()) {
                Iris iris = new Iris();
                iris.setPositionOeil(it.getPosition());
                iris.setTemplateBase64(it.getTemplateBase64());
                iris.setUtilisateur(u);
                u.getIris().add(iris);
            }
        }

        return utilisateurRepository.save(u);
    }
}
