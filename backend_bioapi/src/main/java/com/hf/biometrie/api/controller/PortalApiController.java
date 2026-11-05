package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.CasAdjudication;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.*;
import com.hf.biometrie.api.service.AbisDeduplicationService;
import com.hf.biometrie.api.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/portal")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PortalApiController {

    private final UtilisateurRepository utilisateurRepository;
    private final EmpreinteRepository empreinteRepository;
    private final IrisRepository irisRepository;
    private final AgentRepository agentRepository;
    private final PaysRepository paysRepository;
    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;
    private final CasAdjudicationRepository casAdjudicationRepository;
    private final AbisDeduplicationService abisDeduplicationService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<UtilisateurRepository.UtilisateurRow> rows = utilisateurRepository.findAllSummaries();
        long terminalCount = rows.stream().filter(r -> r.getSourceIdentificationId() == null).count();
        long sqlCount = rows.stream().filter(r -> r.getSourceIdentificationId() != null).count();
        long withBio = rows.stream().filter(r -> r.getNbEmpreintes() > 0 || r.getNbIris() > 0 || (r.getFacePhotoBase64() != null && !r.getFacePhotoBase64().isEmpty())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEnrollments", rows.size());
        stats.put("terminalCount", terminalCount);
        stats.put("sqlImportCount", sqlCount);
        stats.put("withBiometricsCount", withBio);
        stats.put("totalFingerprints", empreinteRepository.count());
        stats.put("totalIris", irisRepository.count());
        stats.put("agentsCount", agentRepository.count());
        stats.put("paysCount", paysRepository.count());
        stats.put("provincesCount", provinceRepository.count());
        stats.put("communesCount", communeRepository.count());
        stats.put("pendingAdjudications", casAdjudicationRepository.countByStatut("EN_ATTENTE"));
        stats.put("confirmedDuplicates", casAdjudicationRepository.countByStatut("DOUBLON_CONFIRME"));
        stats.put("clearedCases", casAdjudicationRepository.countByStatut("DIFFERENTS_VALIDES"));
        return stats;
    }

    @GetMapping("/enrollments")
    public List<UtilisateurRepository.UtilisateurRow> getEnrollments() {
        return utilisateurRepository.findAllSummaries();
    }

    @GetMapping("/adjudications")
    public List<Map<String, Object>> getAdjudications() {
        return casAdjudicationRepository.findAllByOrderByDateDetectionDesc().stream()
                .map(this::toCaseSummary)
                .toList();
    }

    @GetMapping("/adjudications/{id}")
    public ResponseEntity<Map<String, Object>> getAdjudicationDetail(@PathVariable Long id) {
        return casAdjudicationRepository.findById(id).map(cas -> {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("caseInfo", toCaseSummary(cas));
            res.put("candidat", cas.getCandidat());
            res.put("existant", cas.getExistant());
            return ResponseEntity.ok(res);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/adjudications/{id}/decide")
    public ResponseEntity<?> decideAdjudication(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String decision = body.get("decision");
            String operateur = body.get("operateur");
            String commentaire = body.get("commentaire");
            CasAdjudication updated = abisDeduplicationService.decideCase(id, decision, operateur, commentaire);
            return ResponseEntity.ok(toCaseSummary(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/abis/scan")
    public Map<String, Object> scanRegistry() {
        return abisDeduplicationService.scanEntireRegistry();
    }

    @PostMapping("/abis/match/{userId}")
    public List<AbisDeduplicationService.MatchResult> matchOneToMany(@PathVariable Long userId) {
        return abisDeduplicationService.identifyOneToMany(userId);
    }

    @PostMapping("/abis/simulate-duplicate/{userId}")
    public ResponseEntity<?> simulateDuplicate(@PathVariable Long userId) {
        Optional<Utilisateur> opt = utilisateurRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Utilisateur src = opt.get();
        EnrollmentRequest req = new EnrollmentRequest();
        req.setNom((src.getNom() != null ? src.getNom() : "TEST") + "");
        req.setPrenom((src.getPrenom() != null ? src.getPrenom() : "Candidat") + " (Test ABIS)");
        req.setSexe(src.getSexe());
        req.setDateNaissance(src.getDateNaissance());
        req.setLocalite(src.getLocalite());
        req.setFacePhotoBase64(src.getFacePhotoBase64());
        req.setFaceToken(src.getFaceToken());
        req.setNomEquipement("Simulateur ABIS");

        if (src.getEmpreintes() != null && !src.getEmpreintes().isEmpty()) {
            List<EnrollmentRequest.BiometricTemplate> fps = new ArrayList<>();
            for (Empreinte e : src.getEmpreintes()) {
                EnrollmentRequest.BiometricTemplate bt = new EnrollmentRequest.BiometricTemplate();
                bt.setPosition(e.getPositionDoigt());
                bt.setCategorieDoigt(e.getCategorieDoigt());
                bt.setTypeCapture(e.getTypeCapture());
                bt.setImageBase64(e.getImageBase64());
                bt.setTemplateBase64(e.getTemplateBase64());
                bt.setDeviceModel("Simulateur ABIS");
                bt.setResolution(e.getResolution());
                fps.add(bt);
            }
            req.setEmpreintes(fps);
        }

        EnrollmentService.EnrollmentOutcome outcome = enrollmentService.saveEnrollmentWithAbis(req);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("candidatId", outcome.utilisateur().getId());
        resp.put("casesCreated", outcome.casAdjudications().size());
        if (!outcome.casAdjudications().isEmpty()) {
            resp.put("firstCaseId", outcome.casAdjudications().get(0).getId());
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/agents")
    public List<AgentRepository.AgentRow> getAgents() {
        return agentRepository.findAllSummaries();
    }

    @GetMapping("/geo")
    public Map<String, Object> getGeo() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("pays", paysRepository.findAll());
        res.put("provinces", provinceRepository.findAll());
        res.put("communes", communeRepository.findAll());
        return res;
    }

    private Map<String, Object> toCaseSummary(CasAdjudication c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("scoreAbis", c.getScoreAbis());
        m.put("scoreIdentite", c.getScoreIdentite());
        m.put("modalite", c.getModalite());
        m.put("doigtCandidat", c.getDoigtCandidat());
        m.put("doigtExistant", c.getDoigtExistant());
        m.put("raisonAlerte", c.getRaisonAlerte());
        m.put("detailsComparaisonJson", c.getDetailsComparaisonJson());
        m.put("statut", c.getStatut());
        m.put("decisionPar", c.getDecisionPar());
        m.put("commentaireDecision", c.getCommentaireDecision());
        m.put("dateDetection", c.getDateDetection());
        m.put("dateDecision", c.getDateDecision());
        m.put("candidat", toMiniUser(c.getCandidat()));
        m.put("existant", toMiniUser(c.getExistant()));
        return m;
    }

    private Map<String, Object> toMiniUser(Utilisateur u) {
        if (u == null) return Map.of();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("uin", u.getUin());
        m.put("nom", u.getNom());
        m.put("prenom", u.getPrenom());
        m.put("sexe", u.getSexe());
        m.put("dateNaissance", u.getDateNaissance());
        m.put("localite", u.getLocalite());
        m.put("statut", u.getStatut());
        m.put("nomEquipement", u.getNomEquipement());
        m.put("facePhotoBase64", u.getFacePhotoBase64());
        m.put("nbEmpreintes", u.getEmpreintes() != null ? u.getEmpreintes().size() : 0);
        return m;
    }
}

