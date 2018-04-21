package databases;

/**
 * Created by eduardo.benitez on 23/11/2017.
 */

public class Serie extends Diecast{

    private Brand brand;

    private Serie parent;

    private String createdAt;

    private boolean isDefault;

    public Serie() {

    }

    public Serie(final String name) {
        setName(name);
    }

    public Serie(final int id, final String name) {
        setId(id);
        setName(name);
    }

    public Serie(final String name, final Brand brand) {
        this.brand = brand;
        setName(name);
    }

    public Serie(final String name, final Brand brand, final Serie parent) {
        this.brand = brand;
        this.parent = parent;
        setName(name);
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(final Brand brand) {
        this.brand = brand;
    }

    public Serie getParent() {
        return parent;
    }

    public void setParent(final Serie parent) {
        this.parent = parent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createAt) {
        this.createdAt = createAt;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public void setIsDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }
}
