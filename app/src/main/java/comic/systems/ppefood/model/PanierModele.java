package comic.systems.ppefood.model;

public class PanierModele {
    private int ligne;
    private String nomProduit;
    private String categorie;
    private String quantite;
    private String prix;
    private String urlPhoto;
    private int color = -1;
    private boolean isRead;
    /*
    private boolean isImportant;
    */

    public PanierModele() {
    }

    public int getLigne() {
        return ligne;
    }

    public String getNomProduit() {
        return nomProduit;
    }
    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getCategorie() {
        return categorie;
    }
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getQuantite() {
        return quantite;
    }
    public void setQuantite(String quantite) {
        this.quantite = quantite;
    }

    public String getUrlPhoto() { return urlPhoto; }
    public void setUrlPhoto(String photoProduit) { this.urlPhoto = urlPhoto; }
    
    public String getPrix() { return prix; }
    public void setPrix(String prixP) { this.prix = prix; }

    /*
    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
    */

    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }

    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
}
