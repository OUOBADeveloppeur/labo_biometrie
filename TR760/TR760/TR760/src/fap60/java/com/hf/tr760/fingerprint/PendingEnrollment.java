package com.hf.tr760.fingerprint;

public class PendingEnrollment {
    private long id;
    private String uin;
    private String jsonPayload;
    private String nom;
    private String prenom;
    private long timestamp;
    private boolean isSynchronized;

    public PendingEnrollment() {}

    public PendingEnrollment(long id, String uin, String jsonPayload, String nom, String prenom, long timestamp, boolean isSynchronized) {
        this.id = id;
        this.uin = uin;
        this.jsonPayload = jsonPayload;
        this.nom = nom;
        this.prenom = prenom;
        this.timestamp = timestamp;
        this.isSynchronized = isSynchronized;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUin() { return uin; }
    public void setUin(String uin) { this.uin = uin; }

    public String getJsonPayload() { return jsonPayload; }
    public void setJsonPayload(String jsonPayload) { this.jsonPayload = jsonPayload; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSynchronized() { return isSynchronized; }
    public void setSynchronized(boolean isSynchronized) { this.isSynchronized = isSynchronized; }
}
