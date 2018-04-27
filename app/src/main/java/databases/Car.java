package databases;


/**
 * Created by eduardo.benitez on 17/10/2017.
 */

public class Car extends Diecast{

    private Brand brand;

    private Serie serie;

    private String image;

    private String createdAt;

    private boolean isFavorite;

    private int count;

    private String hashtags;

    private String purchaseDate;

    private double price;

    private String extra;

    public Car() {
    }

    public Car(final String name, final Brand brand, final Serie serie, final String image,
               final String hashtags) {
        setName(name);
        this.image = image;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public void setSerie(final Serie serie) {
        this.serie = serie;
    }

    public Serie getSerie() {
        return this.serie;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(final String hashtags) {
        this.hashtags = hashtags;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(final String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(final String extra) {
        this.extra = extra;
    }

}
