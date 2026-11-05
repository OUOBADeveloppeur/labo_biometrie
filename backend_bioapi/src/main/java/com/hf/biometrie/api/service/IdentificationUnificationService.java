package com.hf.biometrie.api.service;

import com.hf.biometrie.api.entity.Commune;
import com.hf.biometrie.api.entity.Empreinte;
import com.hf.biometrie.api.entity.Identification;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.CommuneRepository;
import com.hf.biometrie.api.repository.IdentificationRepository;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentificationUnificationService {

    private final IdentificationRepository identificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CommuneRepository communeRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void unifyIdentificationIntoUtilisateurs() {
        List<Identification> allIdents = identificationRepository.findAll();
        if (allIdents.isEmpty()) {
            return;
        }

        Map<Long, String> communeMap = communeRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Commune::getIdcommune,
                        c -> c.getLibellecommune() != null ? c.getLibellecommune() : "",
                        (a, b) -> a
                ));

        int imported = 0;
        for (Identification ident : allIdents) {
            if (utilisateurRepository.existsBySourceIdentificationId(ident.getId())) {
                continue;
            }

            Utilisateur u = new Utilisateur();
            u.setSourceIdentificationId(ident.getId());
            u.setUin(notBlank(ident.getNoNip()) ? ident.getNoNip().trim() : null);
            u.setMatricule(ident.getMatricule());
            u.setNom(ident.getNom());
            u.setNomJeuneFille(ident.getNomJf());
            u.setPrenom(ident.getPrenoms());
            u.setSexe(ident.getSexe());
            u.setDateNaissance(ident.getDtNaissance() != null ? ident.getDtNaissance().toString() : null);
            u.setLieuNaissance(ident.getLieuNaiss());

            String resolvedCommune = notBlank(ident.getCommune())
                    ? ident.getCommune()
                    : (ident.getIdcommune() != null ? communeMap.get(ident.getIdcommune()) : null);
            u.setLocalite(notBlank(resolvedCommune) ? resolvedCommune : ident.getLieuNaiss());
            u.setLocaliteOrigine(ident.getLocaliteOrigine());
            u.setVillage(ident.getVillage());
            u.setIdCommune(ident.getIdcommune());
            u.setIdCommuneNaissance(ident.getIdcommunenaissance());
            u.setIdPays(ident.getIdpays());

            u.setFacePhotoBase64(extractCleanBase64Image(ident.getPhotographie()));

            u.setSituationMatrimoniale(ident.getSituatMatrimo());
            u.setNbEpouses(ident.getNbfemme());
            u.setNbEnfants(ident.getNbEnfant());
            u.setCategorie(ident.getCategorie());
            u.setPromotion(ident.getPromotion());
            u.setEmploiTenu(ident.getEmploiTenu());
            u.setNiveauInstruction(ident.getNiveauInstruct());
            u.setAutreNiveauInstruction(ident.getAutreNiveauInstruct());
            u.setDernierDiplome(ident.getDernierdiplome());
            u.setGroupeSanguin(ident.getGroupesanguin());
            u.setDateCnib(ident.getDtCnib());
            u.setSituationPrime(ident.getSituationprime());
            u.setNumeroArme(ident.getNumeroarme());
            u.setNumeroChassis(ident.getNumerochassis());

            u.setContact1(ident.getContact1());
            u.setContact2(ident.getContact2());
            u.setContact3(ident.getContact3());
            u.setPersonnePrevenir1(ident.getPersonnePrev1());
            u.setContactPersonnePrevenir1(ident.getContactPersPrev1());
            u.setPersonnePrevenir2(ident.getPersonnePrev2());
            u.setContactPersonnePrevenir2(ident.getContactPersPrev2());

            u.setNomEquipement("Base Identification");
            u.setStatut(notBlank(ident.getEtat()) ? ident.getEtat() : "COMPLET");
            u.setDateCreation(ident.getCreatedat());

            String tplThumbs = extractCleanTemplate(ident.getEmpreinte2poucestemplateiso1979422011());
            String tplRight4 = extractCleanTemplate(ident.getEmpreinte4ddroittemplateiso1979422011());
            String tplLeft4 = extractCleanTemplate(ident.getEmpreinte4dgauchetemplateiso1979422011());

            addFingerIfPresent(u, "thumb_1", "POUCE_D", "POUCES", ident.getEmpreintepoucedroit(), tplThumbs, ident);
            addFingerIfPresent(u, "thumb_0", "POUCE_G", "POUCES", ident.getEmpreintepoucegauche(), null, ident);

            addFingerIfPresent(u, "right_0", "INDEX_D", "SLAP_DROIT", ident.getEmpreinteindexdroit(), tplRight4, ident);
            addFingerIfPresent(u, "right_1", "MAJEUR_D", "SLAP_DROIT", ident.getEmpreintemajeurdroit(), null, ident);
            addFingerIfPresent(u, "right_2", "ANNULAIRE_D", "SLAP_DROIT", ident.getEmpreinteannulairedroit(), null, ident);
            addFingerIfPresent(u, "right_3", "AURICULAIRE_D", "SLAP_DROIT", ident.getEmpreinteauriculairedroit(), null, ident);

            addFingerIfPresent(u, "left_3", "INDEX_G", "SLAP_GAUCHE", ident.getEmpreinteindexgauche(), tplLeft4, ident);
            addFingerIfPresent(u, "left_2", "MAJEUR_G", "SLAP_GAUCHE", ident.getEmpreintemajeurgauche(), null, ident);
            addFingerIfPresent(u, "left_1", "ANNULAIRE_G", "SLAP_GAUCHE", ident.getEmpreinteannulairegauche(), null, ident);
            addFingerIfPresent(u, "left_0", "AURICULAIRE_G", "SLAP_GAUCHE", ident.getEmpreinteauriculairegauche(), null, ident);

            utilisateurRepository.save(u);
            imported++;
        }

        if (imported > 0) {
            log.info("Unification terminée : {} fiches Identification fusionnées dans la table utilisateurs.", imported);
        }
    }

    private void addFingerIfPresent(
            Utilisateur u,
            String positionDoigt,
            String categorieDoigt,
            String typeCapture,
            byte[] rawImage,
            String isoTemplateBase64,
            Identification ident
    ) {
        String imgB64 = extractCleanBase64Image(rawImage);
        if (imgB64 == null && isoTemplateBase64 == null) {
            return;
        }
        Empreinte emp = new Empreinte();
        emp.setPositionDoigt(positionDoigt);
        emp.setCategorieDoigt(categorieDoigt);
        emp.setTypeCapture(typeCapture);
        emp.setImageBase64(imgB64);
        emp.setTemplateBase64(isoTemplateBase64);
        emp.setDeviceModel("Import SQL");
        emp.setResolution(500);
        emp.setDateCapture(ident.getCreatedat());
        emp.setUtilisateur(u);
        u.getEmpreintes().add(emp);
    }

    private String extractCleanBase64Image(byte[] raw) {
        if (raw == null || raw.length == 0) return null;
        String asText = new String(raw, StandardCharsets.UTF_8).trim();
        if (asText.equalsIgnoreCase("binary data") || asText.length() < 24) {
            return null;
        }
        if (asText.startsWith("data:image/")) {
            int comma = asText.indexOf(',');
            return comma >= 0 ? asText.substring(comma + 1).trim() : null;
        }
        if (asText.startsWith("iVBOR") || asText.startsWith("/9j/") || asText.startsWith("Qk")) {
            return asText;
        }
        if (((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8)
                || ((raw[0] & 0xFF) == 0x89 && raw[1] == 0x50)
                || (raw[0] == 'B' && raw[1] == 'M')) {
            return Base64.getEncoder().encodeToString(raw);
        }
        return null;
    }

    private String extractCleanTemplate(byte[] raw) {
        if (raw == null || raw.length == 0) return null;
        String asText = new String(raw, StandardCharsets.UTF_8).trim();
        if (asText.equalsIgnoreCase("binary data") || asText.length() < 16) {
            return null;
        }
        if (asText.startsWith("Rk1S")) {
            return asText;
        }
        if (raw.length > 4 && raw[0] == 'F' && raw[1] == 'M' && raw[2] == 'R') {
            return Base64.getEncoder().encodeToString(raw);
        }
        return null;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}

