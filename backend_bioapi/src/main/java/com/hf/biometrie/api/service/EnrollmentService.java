package com.hf.biometrie.api.service;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Iris;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Utilisateur saveEnrollment(EnrollmentRequest request) {
        Utilisateur u = new Utilisateur();
        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setSexe(request.getSexe());
        u.setAge(request.getAge());
        u.setVille(request.getVille());
        u.setFaceToken(request.getFaceToken());
        u.setFacePhotoBase64(request.getFacePhotoBase64());

        if (request.getEmpreintes() != null) {
            for (EnrollmentRequest.BiometricTemplate et : request.getEmpreintes()) {
                Empreinte emp = new Empreinte();
                emp.setPositionDoigt(et.getPosition());
                emp.setTemplateBase64(et.getTemplateBase64());
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
