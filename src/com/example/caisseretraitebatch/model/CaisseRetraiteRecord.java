package com.example.caisseretraitebatch.model;

import javax.validation.constraints.*;
import java.util.Date;

public class CaisseRetraiteRecord {

    @NotBlank
    private String numeroSecuriteSociale;  // 1) Obligatoire

    @NotBlank
    private String nom;                   // 2)

    @NotBlank
    private String prenom;               // 3)

    @Past
    private Date dateNaissance;          // 4) Doit être dans le passé

    private String adresse;              // 5)
    private String codePostal;           // 6)
    private String ville;                // 7)
    private String pays;                 // 8)

    private String nomConjoint;          // 9) Nouveau champ (ex. "Marie")

    @PositiveOrZero
    private Integer nombreEnfants;       // 10) >= 0

    @Min(0)
    @NotNull
    private Double montantCotisation;    // 11) >= 0 et non null

    // --- Getters / Setters ---
    public String getNumeroSecuriteSociale() {
        return numeroSecuriteSociale;
    }

    public void setNumeroSecuriteSociale(String numeroSecuriteSociale) {
        this.numeroSecuriteSociale = numeroSecuriteSociale;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getNomConjoint() {
        return nomConjoint;
    }

    public void setNomConjoint(String nomConjoint) {
        this.nomConjoint = nomConjoint;
    }

    public Integer getNombreEnfants() {
        return nombreEnfants;
    }

    public void setNombreEnfants(Integer nombreEnfants) {
        this.nombreEnfants = nombreEnfants;
    }

    public Double getMontantCotisation() {
        return montantCotisation;
    }

    public void setMontantCotisation(Double montantCotisation) {
        this.montantCotisation = montantCotisation;
    }
}
