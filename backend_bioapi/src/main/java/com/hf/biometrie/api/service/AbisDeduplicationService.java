package com.hf.biometrie.api.service;

import com.hf.biometrie.api.entity.CasAdjudication;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.CasAdjudicationRepository;
import com.hf.biometrie.api.repository.EmpreinteRepository;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbisDeduplicationService {

    public static final double ABIS_THRESHOLD = 40.0; // Seuil officiel SourceAFIS (FAR ~0.01%)
    public static final double DEMOGRAPHIC_THRESHOLD = 95.0;

    private final UtilisateurRepository utilisateurRepository;
    private final EmpreinteRepository empreinteRepository;
    private final CasAdjudicationRepository casAdjudicationRepository;

    // Cache mémoire des gabarits SourceAFIS extraits pour accélérer le 1:N
    private final Map<Long, Optional<FingerprintTemplate>> templateCache = new ConcurrentHashMap<>();

    public record MatchResult(
            Long targetUserId,
            String targetNom,
            String targetPrenom,
            String targetUin,
            double scoreAbis,
            double scoreIdentite,
            String doigtCandidat,
            String doigtExistant,
            String modalite,
            String raison,
            Map<String, Double> fingerScores
    ) {
        public boolean isSuspiciousDuplicate() {
            return scoreAbis >= ABIS_THRESHOLD || scoreIdentite >= DEMOGRAPHIC_THRESHOLD || "TOKEN_FACIAL".equals(modalite);
        }
    }

    public Optional<FingerprintTemplate> getOrExtractTemplate(Empreinte emp) {
        if (emp == null) return Optional.empty();
        if (emp.getId() != null && templateCache.containsKey(emp.getId())) {
            return templateCache.get(emp.getId());
        }
        Optional<FingerprintTemplate> extracted = extractFromEmpreinte(emp);
        if (emp.getId() != null) {
            templateCache.put(emp.getId(), extracted);
        }
        return extracted;
    }

    private Optional<FingerprintTemplate> extractFromEmpreinte(Empreinte emp) {
        try {
            if (emp.getImageBase64() != null && emp.getImageBase64().length() > 40) {
                String clean = emp.getImageBase64().trim();
                if (clean.contains(",")) {
                    clean = clean.substring(clean.indexOf(',') + 1).trim();
                }
                byte[] imgBytes = Base64.getDecoder().decode(clean);
                double dpi = (emp.getResolution() != null && emp.getResolution() > 100) ? emp.getResolution() : 500.0;
                FingerprintImage img = new FingerprintImage(imgBytes, new FingerprintImageOptions().dpi(dpi));
                return Optional.of(new FingerprintTemplate(img));
            }
        } catch (Exception ex) {
            log.debug("Extraction SourceAFIS ignorée sur empreinte #{}: {}", emp.getId(), ex.getMessage());
        }
        return Optional.empty();
    }

    public MatchResult compareUsers(Utilisateur cand, List<Empreinte> candFingers, Utilisateur exist, List<Empreinte> existFingers) {
        double bestAbisScore = 0.0;
        String bestCandPos = null;
        String bestExistPos = null;
        Map<String, Double> fingerBreakdown = new LinkedHashMap<>();

        if (candFingers != null && existFingers != null && !candFingers.isEmpty() && !existFingers.isEmpty()) {
            for (Empreinte cf : candFingers) {
                Optional<FingerprintTemplate> cTpl = getOrExtractTemplate(cf);
                if (cTpl.isEmpty()) continue;
                FingerprintMatcher matcher = new FingerprintMatcher(cTpl.get());

                double bestForThisFinger = 0.0;
                String matchedPos = null;

                for (Empreinte ef : existFingers) {
                    // Priorité au même doigt si les deux positions sont connues
                    boolean samePos = cf.getPositionDoigt() != null && cf.getPositionDoigt().equals(ef.getPositionDoigt());
                    if (cf.getPositionDoigt() != null && ef.getPositionDoigt() != null && !samePos) {
                        continue;
                    }
                    Optional<FingerprintTemplate> eTpl = getOrExtractTemplate(ef);
                    if (eTpl.isEmpty()) continue;

                    double s = matcher.match(eTpl.get());
                    if (s > bestForThisFinger) {
                        bestForThisFinger = s;
                        matchedPos = ef.getPositionDoigt();
                    }
                }

                String key = cf.getPositionDoigt() != null ? cf.getPositionDoigt() : ("emp_" + cf.getId());
                fingerBreakdown.put(key, Math.round(bestForThisFinger * 10.0) / 10.0);

                if (bestForThisFinger > bestAbisScore) {
                    bestAbisScore = bestForThisFinger;
                    bestCandPos = cf.getPositionDoigt();
                    bestExistPos = matchedPos;
                }
            }
        }

        bestAbisScore = Math.round(bestAbisScore * 10.0) / 10.0;

        // Score démographique & Token facial
        boolean faceTokenMatch = notBlank(cand.getFaceToken())
                && cand.getFaceToken().trim().equals(exist.getFaceToken() != null ? exist.getFaceToken().trim() : "");

        double demoScore = computeDemographicScore(cand, exist);

        String modalite = "AUCUNE";
        List<String> raisons = new ArrayList<>();

        if (bestAbisScore >= ABIS_THRESHOLD) {
            raisons.add("Correspondance biométrique SourceAFIS (" + bestAbisScore + " pts sur " + formatPos(bestCandPos) + ")");
        }
        if (faceTokenMatch) {
            raisons.add("Token facial identique");
        }
        if (demoScore >= DEMOGRAPHIC_THRESHOLD) {
            raisons.add("Identité civile identique (" + Math.round(demoScore) + "%)");
        }

        if (bestAbisScore >= ABIS_THRESHOLD && (faceTokenMatch || demoScore >= DEMOGRAPHIC_THRESHOLD)) {
            modalite = "MULTIMODAL";
        } else if (bestAbisScore >= ABIS_THRESHOLD) {
            modalite = "EMPREINTE_ABIS";
        } else if (faceTokenMatch) {
            modalite = "TOKEN_FACIAL";
        } else if (demoScore >= DEMOGRAPHIC_THRESHOLD) {
            modalite = "IDENTITE_CIVILE";
        } else if (bestAbisScore > 0) {
            modalite = "SOUS_SEUIL";
        }

        String raisonFinale = raisons.isEmpty() ? "Score sous le seuil d'alerte" : String.join(" + ", raisons);

        return new MatchResult(
                exist.getId(),
                exist.getNom(),
                exist.getPrenom(),
                exist.getUin(),
                bestAbisScore,
                demoScore,
                bestCandPos,
                bestExistPos,
                modalite,
                raisonFinale,
                fingerBreakdown
        );
    }

    @Transactional
    public List<CasAdjudication> evaluateNewEnrollment(Utilisateur savedCandidate) {
        List<Utilisateur> others = utilisateurRepository.findAll().stream()
                .filter(u -> !u.getId().equals(savedCandidate.getId()))
                .filter(u -> !"REJETE_DOUBLON".equalsIgnoreCase(u.getStatut()))
                .toList();

        List<Empreinte> allFingers = empreinteRepository.findAll();
        Map<Long, List<Empreinte>> fingersByUser = allFingers.stream()
                .filter(e -> e.getUtilisateur() != null)
                .collect(Collectors.groupingBy(e -> e.getUtilisateur().getId()));

        List<Empreinte> candFingers = savedCandidate.getEmpreintes() != null && !savedCandidate.getEmpreintes().isEmpty()
                ? savedCandidate.getEmpreintes()
                : fingersByUser.getOrDefault(savedCandidate.getId(), List.of());

        List<CasAdjudication> createdCases = new ArrayList<>();
        for (Utilisateur exist : others) {
            MatchResult mr = compareUsers(savedCandidate, candFingers, exist, fingersByUser.getOrDefault(exist.getId(), List.of()));
            if (mr.isSuspiciousDuplicate()) {
                CasAdjudication cas = createOrGetCase(savedCandidate, exist, mr);
                createdCases.add(cas);
            }
        }

        if (!createdCases.isEmpty()) {
            savedCandidate.setStatut("EN_ADJUDICATION");
            utilisateurRepository.save(savedCandidate);
        }

        return createdCases;
    }

    @Transactional
    public Map<String, Object> scanEntireRegistry() {
        long t0 = System.currentTimeMillis();
        List<Utilisateur> users = utilisateurRepository.findAll();
        List<Empreinte> allFingers = empreinteRepository.findAll();
        Map<Long, List<Empreinte>> fingersByUser = allFingers.stream()
                .filter(e -> e.getUtilisateur() != null)
                .collect(Collectors.groupingBy(e -> e.getUtilisateur().getId()));

        // Pré-extraction des gabarits
        for (Empreinte emp : allFingers) {
            getOrExtractTemplate(emp);
        }

        int pairsCompared = 0;
        int newCases = 0;

        for (int i = 0; i < users.size(); i++) {
            Utilisateur uA = users.get(i);
            if ("REJETE_DOUBLON".equalsIgnoreCase(uA.getStatut())) continue;
            List<Empreinte> fA = fingersByUser.getOrDefault(uA.getId(), List.of());

            for (int j = i + 1; j < users.size(); j++) {
                Utilisateur uB = users.get(j);
                if ("REJETE_DOUBLON".equalsIgnoreCase(uB.getStatut())) continue;
                pairsCompared++;

                MatchResult mr = compareUsers(uB, fingersByUser.getOrDefault(uB.getId(), List.of()), uA, fA);
                if (mr.isSuspiciousDuplicate()) {
                    if (casAdjudicationRepository.findExistingPair(uB.getId(), uA.getId()).isEmpty()) {
                        createOrGetCase(uB, uA, mr);
                        if (!"EN_ADJUDICATION".equalsIgnoreCase(uB.getStatut())) {
                            uB.setStatut("EN_ADJUDICATION");
                            utilisateurRepository.save(uB);
                        }
                        newCases++;
                    }
                }
            }
        }

        long durationMs = System.currentTimeMillis() - t0;
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("scannedUsers", users.size());
        res.put("pairsCompared", pairsCompared);
        res.put("newCasesDetected", newCases);
        res.put("totalPendingCases", casAdjudicationRepository.countByStatut("EN_ATTENTE"));
        res.put("durationMs", durationMs);
        return res;
    }

    @Transactional(readOnly = true)
    public List<MatchResult> identifyOneToMany(Long userId) {
        Utilisateur probe = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + userId));
        List<Utilisateur> others = utilisateurRepository.findAll().stream()
                .filter(u -> !u.getId().equals(userId))
                .toList();

        List<Empreinte> allFingers = empreinteRepository.findAll();
        Map<Long, List<Empreinte>> fingersByUser = allFingers.stream()
                .filter(e -> e.getUtilisateur() != null)
                .collect(Collectors.groupingBy(e -> e.getUtilisateur().getId()));

        List<Empreinte> probeFingers = fingersByUser.getOrDefault(userId, List.of());

        return others.stream()
                .map(target -> compareUsers(probe, probeFingers, target, fingersByUser.getOrDefault(target.getId(), List.of())))
                .sorted(Comparator.comparingDouble((MatchResult m) -> Math.max(m.scoreAbis(), m.scoreIdentite() * 0.5)).reversed())
                .limit(10)
                .toList();
    }

    @Transactional
    public CasAdjudication decideCase(Long casId, String decision, String operateur, String commentaire) {
        CasAdjudication cas = casAdjudicationRepository.findById(casId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier d'adjudication introuvable: " + casId));

        Utilisateur candidat = cas.getCandidat();
        if ("DOUBLON_CONFIRME".equalsIgnoreCase(decision)) {
            cas.setStatut("DOUBLON_CONFIRME");
            candidat.setStatut("REJETE_DOUBLON");
        } else if ("DIFFERENTS_VALIDES".equalsIgnoreCase(decision)) {
            cas.setStatut("DIFFERENTS_VALIDES");
            candidat.setStatut("COMPLET");
            if (!notBlank(candidat.getUin())) {
                long random12Digit = java.util.concurrent.ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
                candidat.setUin(String.valueOf(random12Digit));
            }
        } else {
            throw new IllegalArgumentException("Décision invalide: " + decision);
        }

        cas.setDecisionPar(notBlank(operateur) ? operateur : "Superviseur ABIS");
        cas.setCommentaireDecision(commentaire);
        cas.setDateDecision(LocalDateTime.now());

        utilisateurRepository.save(candidat);
        return casAdjudicationRepository.save(cas);
    }

    private CasAdjudication createOrGetCase(Utilisateur candidat, Utilisateur existant, MatchResult mr) {
        Optional<CasAdjudication> existing = casAdjudicationRepository.findExistingPair(candidat.getId(), existant.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        CasAdjudication cas = new CasAdjudication();
        cas.setCandidat(candidat);
        cas.setExistant(existant);
        cas.setScoreAbis(mr.scoreAbis());
        cas.setScoreIdentite(mr.scoreIdentite());
        cas.setModalite(mr.modalite());
        cas.setDoigtCandidat(mr.doigtCandidat());
        cas.setDoigtExistant(mr.doigtExistant());
        cas.setRaisonAlerte(mr.raison());
        cas.setStatut("EN_ATTENTE");
        cas.setDateDetection(LocalDateTime.now());
        String json = mr.fingerScores().entrySet().stream()
                .map(e -> "\"" + e.getKey().replace("\"", "") + "\":" + e.getValue())
                .collect(Collectors.joining(",", "{", "}"));
        cas.setDetailsComparaisonJson(json);
        return casAdjudicationRepository.save(cas);
    }

    private double computeDemographicScore(Utilisateur a, Utilisateur b) {
        if (notBlank(a.getUin()) && notBlank(b.getUin()) && a.getUin().trim().equalsIgnoreCase(b.getUin().trim())) {
            return 100.0;
        }
        boolean sameNom = notBlank(a.getNom()) && notBlank(b.getNom())
                && normalize(a.getNom()).equals(normalize(b.getNom()));
        boolean samePrenom = notBlank(a.getPrenom()) && notBlank(b.getPrenom())
                && normalize(a.getPrenom()).equals(normalize(b.getPrenom()));
        boolean sameDob = notBlank(a.getDateNaissance()) && notBlank(b.getDateNaissance())
                && normalize(a.getDateNaissance()).equals(normalize(b.getDateNaissance()));

        if (sameNom && samePrenom && sameDob) return 100.0;
        if (sameNom && samePrenom && (!notBlank(a.getDateNaissance()) || !notBlank(b.getDateNaissance()))) return 85.0;
        if (sameNom && samePrenom) return 75.0;
        if (sameNom && sameDob) return 60.0;
        return 0.0;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String formatPos(String pos) {
        if (pos == null) return "empreinte";
        return switch (pos) {
            case "thumb_1" -> "Pouce Droit";
            case "thumb_0" -> "Pouce Gauche";
            case "right_0" -> "Index Droit";
            case "right_1" -> "Majeur Droit";
            case "right_2" -> "Annulaire Droit";
            case "right_3" -> "Auriculaire Droit";
            case "left_3" -> "Index Gauche";
            case "left_2" -> "Majeur Gauche";
            case "left_1" -> "Annulaire Gauche";
            case "left_0" -> "Auriculaire Gauche";
            default -> pos;
        };
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}

