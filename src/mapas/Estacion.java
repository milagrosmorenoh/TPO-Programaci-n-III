package mapas;

public class Estacion {

    private final int aula;
    private final double x;
    private final double y;
    private final boolean obligatorio;
    private final boolean prohibido;

    public Estacion(int aula, double x, double y, boolean obligatorio, boolean prohibido) {
        this.aula = aula;
        this.x = x;
        this.y = y;
        this.obligatorio = obligatorio;
        this.prohibido = prohibido;
    }

    public int getAula() {
        return aula;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean esObligatoria() {
        return obligatorio;
    }

    public boolean esProhibida() {
        return prohibido;
    }
}
