package com.hf.biometrie.api.exception;

import com.hf.biometrie.api.entity.Utilisateur;
import lombok.Getter;

/**
 * Exception levée lorsqu'un doublon est détecté lors d'un enrôlement.
 * Contient la référence vers l'utilisateur existant pour afficher ses détails.
 */
@Getter
public class DoublonException extends RuntimeException {

    private final Utilisateur utilisateurExistant;
    private final String raisonDoublon;

    public DoublonException(String raisonDoublon, Utilisateur utilisateurExistant) {
        super("DOUBLON_DETECTE");
        this.raisonDoublon = raisonDoublon;
        this.utilisateurExistant = utilisateurExistant;
    }
}
