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

    public Car() {
    }

    public Car(final String name, final Brand brand, final Serie serie, final String image) {
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

}
