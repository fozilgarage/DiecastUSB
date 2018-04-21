package databases;

/**
 * Created by eduardo.benitez on 17/10/2017.
 */

public class Brand extends Diecast{

    private String extra;

    private String createdAt;

    public Brand() {
    }

    public Brand(final String name) {
        setName(name);
    }

    public Brand(final int idBrand, final String name, final String extra) {
        setId(idBrand);
        setName(name);
        this.extra = extra;
    }

    public Brand(final String name, final String extra) {
        setName(name);
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
