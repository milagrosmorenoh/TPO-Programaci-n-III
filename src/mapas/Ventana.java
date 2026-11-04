package mapas;

public class Ventana {

    private final String lugar;
    private final int inicioCierre;
    private final int finCierre;

    public Ventana(String lugar, int inicioCierre, int finCierre) {
        this.lugar = lugar;
        this.inicioCierre = inicioCierre;
        this.finCierre = finCierre;
    }

    public String getLugar() {
        return lugar;
    }

    public int getInicioCierre() {
        return inicioCierre;
    }

    public int getFinCierre() {
        return finCierre;
    }
}