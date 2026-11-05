package com.hf.biometrie.api.service;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.CasAdjudication;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Iris;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EnrollmentService {

    private final UtilisateurRepository utilisateurRepository;
    private final AbisDeduplicationService abisDeduplicationService;

    public EnrollmentService(
            UtilisateurRepository utilisateurRepository,
            AbisDeduplicationService abisDeduplicationService
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.abisDeduplicationService = abisDeduplicationService;
    }

    public static final class EnrollmentOutcome {
        private final Utilisateur utilisateur;
        private final List<CasAdjudication> casAdjudications;

        public EnrollmentOutcome(Utilisateur utilisateur, List<CasAdjudication> casAdjudications) {
            this.utilisateur = utilisateur;
            this.casAdjudications = casAdjudications;
        }

        public Utilisateur utilisateur() {
            return utilisateur;
        }

        public List<CasAdjudication> casAdjudications() {
            return casAdjudications;
        }
    }

    @Transactional
    public Utilisateur saveEnrollment(EnrollmentRequest request) {
        return persistAndEvaluate(request).utilisateur();
    }

    @Transactional
    public EnrollmentOutcome saveEnrollmentWithAbis(EnrollmentRequest request) {
        return persistAndEvaluate(request);
    }

    private EnrollmentOutcome persistAndEvaluate(EnrollmentRequest request) {
        Utilisateur u = new Utilisateur();
        if (request.getUin() == null || request.getUin().trim().isEmpty()) {
            long random12Digit = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
            u.setUin(String.valueOf(random12Digit));
        } else {
            u.setUin(request.getUin());
        }
        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setSexe(request.getSexe());
        u.setDateNaissance(request.getDateNaissance());
        u.setLocalite(request.getLocalite());
        u.setFaceToken(request.getFaceToken());
        u.setFacePhotoBase64(request.getFacePhotoBase64());
        u.setNomEquipement(request.getNomEquipement());
        u.setTempsEnrolementTotalMs(request.getTempsEnrolementTotalMs());
        u.setStatut(request.getStatut() != null ? request.getStatut() : "COMPLET");

        if (request.getEmpreintes() != null) {
            for (EnrollmentRequest.BiometricTemplate et : request.getEmpreintes()) {
                Empreinte emp = new Empreinte();
                emp.setPositionDoigt(et.getPosition());
                emp.setCategorieDoigt(et.getCategorieDoigt());
                emp.setTypeCapture(et.getTypeCapture());
                emp.setTemplateBase64(et.getTemplateBase64());
                emp.setImageBase64(et.getImageBase64());
                emp.setDeviceModel(et.getDeviceModel());
                emp.setFirmwareVersion(et.getFirmwareVersion());
                emp.setResolution(et.getResolution());
                emp.setQualite(et.getQualite());
                emp.setScoreNfiq(et.getScoreNfiq());
                emp.setScoreQualiteSdk(et.getScoreQualiteSdk());
                emp.setSurfaceContact(et.getSurfaceContact());
                emp.setTempsCaptureMs(et.getTempsCaptureMs());
                emp.setNombreTentatives(et.getNombreTentatives());
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

        Utilisateur saved = utilisateurRepository.save(u);
        List<CasAdjudication> cases = abisDeduplicationService.evaluateNewEnrollment(saved);

        return new EnrollmentOutcome(saved, cases);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> findAllEnrollments() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> findEnrollmentById(Long id) {
        return utilisateurRepository.findById(id);
    }
}
