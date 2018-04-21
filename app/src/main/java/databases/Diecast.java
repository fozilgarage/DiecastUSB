package databases;

/**
 * Created by eduardo.benitez on 29/11/2017.
 */

public class Diecast {

    private int id;

    private String name;

    public Diecast() {

    }

    public int getId() {
        return id;
    }

    public Diecast(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Diecast(final String name) {
        this.name = name;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
