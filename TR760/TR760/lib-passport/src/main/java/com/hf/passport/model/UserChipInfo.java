package com.hf.passport.model;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.Serializable;

public class UserChipInfo implements Serializable {
    /**
     * 3 Passport
     */
    private int documentType;
    private String documentCode;
    private String primaryId;
    private String secondId;
    private String gender;
    private String issuingState;
    private String nationality;
    private String docNum;
    private String birthDate;
    private String expiryDate;
    private Boolean passiveAuth;
    private Boolean ChipAuth;
    private String optionalData1; /* NOTE: For TD1 holds personal number for some issuing states (e.g. NL), but is used to hold (part of) document number for others. */
    private String optionalData2;
    private Bitmap photo;

    public UserChipInfo() {
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getSecondId() {
        return secondId;
    }

    public void setSecondId(String secondId) {
        this.secondId = secondId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIssuingState() {
        return issuingState;
    }

    public void setIssuingState(String issuingState) {
        this.issuingState = issuingState;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDocNum() {
        return docNum;
    }

    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getPassiveAuth() {
        return passiveAuth;
    }

    public void setPassiveAuth(Boolean passiveAuth) {
        this.passiveAuth = passiveAuth;
    }

    public Boolean getChipAuth() {
        return ChipAuth;
    }

    public void setChipAuth(Boolean chipAuth) {
        ChipAuth = chipAuth;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getOptionalData1() {
        if (TextUtils.isEmpty(optionalData1)) {
            this.optionalData1 = "";
        }
        return optionalData1;
    }

    public void setOptionalData1(String optionalData1) {
        this.optionalData1 = optionalData1;
    }

    public String getOptionalData2() {
        if (TextUtils.isEmpty(optionalData2)) {
            this.optionalData2 = "";
        }
        return optionalData2;
    }

    public void setOptionalData2(String optionalData2) {
        this.optionalData2 = optionalData2;
    }

    @Override
    public String toString() {
        return "UserChipInfo{" +
                "documentType=" + documentType +
                ", documentCode='" + documentCode + '\'' +
                ", primaryId='" + primaryId + '\'' +
                ", secondId='" + secondId + '\'' +
                ", gender='" + gender + '\'' +
                ", issuingState='" + issuingState + '\'' +
                ", nationality='" + nationality + '\'' +
                ", docNum='" + docNum + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", passiveAuth=" + passiveAuth +
                ", ChipAuth=" + ChipAuth +
                ", optionalData1='" + optionalData1 + '\'' +
                ", optionalData2='" + optionalData2 + '\'' +
                ", photo=" + photo +
                '}';
    }
}
