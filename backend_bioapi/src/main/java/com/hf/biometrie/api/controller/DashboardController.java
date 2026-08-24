package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "dashboard";
    }
}
