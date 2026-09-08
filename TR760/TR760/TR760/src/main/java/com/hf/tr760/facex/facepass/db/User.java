package com.hf.tr760.facex.facepass.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity
public class User {
    @Id
    @Property
    public Long id = 0l;
    @Property
    public String name = "";
    @Property
    public String prenom = "";
    @Property
    public String sex = "";
    @Property
    public String dateNaissance = "";
    @Property
    public String localite = "";
    @Property
    public String faceToken = "";
    @Generated(hash = 1079076218)
    public User(Long id, String name, String prenom, String sex,
            String dateNaissance, String localite, String faceToken) {
        this.id = id;
        this.name = name;
        this.prenom = prenom;
        this.sex = sex;
        this.dateNaissance = dateNaissance;
        this.localite = localite;
        this.faceToken = faceToken;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPrenom() {
        return this.prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getSex() {
        return this.sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getDateNaissance() {
        return this.dateNaissance;
    }
    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    public String getLocalite() {
        return this.localite;
    }
    public void setLocalite(String localite) {
        this.localite = localite;
    }
    public String getFaceToken() {
        return this.faceToken;
    }
    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

}
