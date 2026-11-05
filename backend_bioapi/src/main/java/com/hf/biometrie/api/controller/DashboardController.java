package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.entity.*;
import com.hf.biometrie.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final UtilisateurRepository utilisateurRepository;
    private final IdentificationRepository identificationRepository;
    private final AgentRepository agentRepository;
    private final PaysRepository paysRepository;
    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Pays> paysList = paysRepository.findAll();
        List<Province> provincesList = provinceRepository.findAll();
        List<Commune> communesList = communeRepository.findAll();

        Map<Long, String> paysMap = paysList.stream()
                .collect(Collectors.toMap(Pays::getIdpays, p -> p.getLibellepays() != null ? p.getLibellepays() : "", (a, b) -> a));
        Map<Long, String> provinceMap = provincesList.stream()
                .collect(Collectors.toMap(Province::getIdprovince, p -> p.getLibelleprovince() != null ? p.getLibelleprovince() : "", (a, b) -> a));
        Map<Long, String> communeMap = communesList.stream()
                .collect(Collectors.toMap(Commune::getIdcommune, c -> c.getLibellecommune() != null ? c.getLibellecommune() : "", (a, b) -> a));

        List<UtilisateurRepository.UtilisateurRow> allUsers = utilisateurRepository.findAllSummaries();
        long countApp = allUsers.stream().filter(u -> u.getSourceIdentificationId() == null).count();
        long countImport = allUsers.stream().filter(u -> u.getSourceIdentificationId() != null).count();

        model.addAttribute("utilisateurs", allUsers);
        model.addAttribute("countApp", countApp);
        model.addAttribute("countImport", countImport);
        model.addAttribute("agents", agentRepository.findAllSummaries());
        model.addAttribute("paysList", paysList);
        model.addAttribute("provincesList", provincesList);
        model.addAttribute("communesList", communesList);
        model.addAttribute("paysMap", paysMap);
        model.addAttribute("provinceMap", provinceMap);
        model.addAttribute("communeMap", communeMap);
        return "dashboard";
    }

    @GetMapping("/api/identifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getIdentificationDetail(@PathVariable Long id) {
        return identificationRepository.findById(id).map(ident -> {
            Map<String, Object> res = new LinkedHashMap<>();

            Map<String, String> slaps = new LinkedHashMap<>();
            putIfPresent(slaps, "Slap 4 Doigts Droits", ident.getEmpreinte4ddroit());
            putIfPresent(slaps, "Slap 4 Doigts Gauches", ident.getEmpreinte4dgauche());
            putIfPresent(slaps, "Slap 2 Pouces", ident.getEmpreinte2pouces());
            res.put("slapImages", slaps);

            Map<String, Integer> standards = new LinkedHashMap<>();
            putSize(standards, "2 Pouces - Image ANSI 381-2004", ident.getEmpreinte2poucesimageansiincits3812004());
            putSize(standards, "2 Pouces - Image ISO 19794-4:2011", ident.getEmpreinte2poucesimageiso1979442011());
            putSize(standards, "2 Pouces - Template ANSI 378-2004", ident.getEmpreinte2poucestemplateansiincits3782004());
            putSize(standards, "2 Pouces - Template ISO 19794-2:2011", ident.getEmpreinte2poucestemplateiso1979422011());
            putSize(standards, "4D Droit - Image ANSI 381-2004", ident.getEmpreinte4ddroitimageansiincits3812004());
            putSize(standards, "4D Droit - Image ISO 19794-4:2011", ident.getEmpreinte4ddroitimageiso1979442011());
            putSize(standards, "4D Droit - Template ANSI 378-2004", ident.getEmpreinte4ddroittemplateansiincits3782004());
            putSize(standards, "4D Droit - Template ISO 19794-2:2011", ident.getEmpreinte4ddroittemplateiso1979422011());
            putSize(standards, "4D Gauche - Image ANSI 381-2004", ident.getEmpreinte4dgaucheimageansiincits3812004());
            putSize(standards, "4D Gauche - Image ISO 19794-4:2011", ident.getEmpreinte4dgaucheimageiso1979442011());
            putSize(standards, "4D Gauche - Template ANSI 378-2004", ident.getEmpreinte4dgauchetemplateansiincits3782004());
            putSize(standards, "4D Gauche - Template ISO 19794-2:2011", ident.getEmpreinte4dgauchetemplateiso1979422011());
            res.put("standardPayloads", standards);

            return ResponseEntity.ok(res);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/agents/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAgentDetail(@PathVariable Integer id) {
        return agentRepository.findById(id).map(agent -> {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("entity", agent);
            res.put("photoUri", Identification.byteaToDataUri(agent.getPhotographieutilisateur()));

            Map<String, String> fingers = new LinkedHashMap<>();
            putIfPresent(fingers, "Pouce Droit", agent.getPoucedroitutilisateur());
            putIfPresent(fingers, "Index Droit", agent.getIndexdroitutilisateur());
            putIfPresent(fingers, "Majeur Droit", agent.getMajeurdroitutilisateur());
            putIfPresent(fingers, "Annulaire Droit", agent.getAnnulairedroiutilisateur());
            putIfPresent(fingers, "Auriculaire Droit", agent.getAuriculairedroitutilisateur());
            putIfPresent(fingers, "Pouce Gauche", agent.getPoucegaucheutilisateur());
            putIfPresent(fingers, "Index Gauche", agent.getIndexgaucheutilisateur());
            putIfPresent(fingers, "Majeur Gauche", agent.getMajeurgaucheutilisateur());
            putIfPresent(fingers, "Annulaire Gauche", agent.getAnnulairegaucheutilisateur());
            putIfPresent(fingers, "Auriculaire Gauche", agent.getAuriculairegaucheutilisateur());
            res.put("fingerImages", fingers);

            return ResponseEntity.ok(res);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void putIfPresent(Map<String, String> map, String label, byte[] raw) {
        String uri = Identification.byteaToDataUri(raw);
        if (uri != null) {
            map.put(label, uri);
        } else if (raw != null && raw.length > 0) {
            map.put(label, "BINARY:" + raw.length);
        }
    }

    private void putSize(Map<String, Integer> map, String label, byte[] raw) {
        if (raw != null && raw.length > 0) {
            map.put(label, raw.length);
        }
    }
}
