package com.hf.passport.model;

public class BacKeyParts {

    private String docNum;
    private String birthDate;
    private String expiryDate;
    private String mrzCode;

    public BacKeyParts(String docNum, String birthDate, String expiryDate, String mrzCode) {
        this.docNum = docNum;
        this.birthDate = birthDate;
        this.expiryDate = expiryDate;
        this.mrzCode = mrzCode;
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

    public String getMrzCode() {
        return mrzCode;
    }

    public void setMrzCode(String mrzCode) {
        this.mrzCode = mrzCode;
    }

    @Override
    public String toString() {
        return "BacKeyParts{" +
                "docNum='" + docNum + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", mrzCode='" + mrzCode + '\'' +
                '}';
    }
}
