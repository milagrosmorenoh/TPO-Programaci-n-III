package mapas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Conexion {

    private final String desde;
    private final String hasta;
    private final double tiempoCaminando;
    private final String movimientosPermitidos;
    private final Map<Character, Double> duraciones = new HashMap<>();

    public Conexion(String desde, String hasta, double tiempoCaminando, String movimientosPermitidos) {
        this.desde = desde;
        this.hasta = hasta;
        this.tiempoCaminando = tiempoCaminando;
        this.movimientosPermitidos = movimientosPermitidos;
    }

    public void calcularDuraciones() {
        for (char movimiento : movimientosPermitidos.toCharArray()) {
            switch (movimiento) {
                case 'C':
                    duraciones.put('C', tiempoCaminando);
                    break;

                case 'S':
                    duraciones.put('S', tiempoCaminando / 2.0);
                    break;

                case 'P':
                    duraciones.put('P', tiempoCaminando * 2.0);
                    break;

                default:
                    throw new IllegalArgumentException("Movimiento desconocido: " + movimiento);
            }
        }
    }

    public double getMenorDuracion() {
        return Collections.min(duraciones.values());
    }

    public double getDuracion(char movimiento) {
        return duraciones.get(movimiento);
    }

    public String getDesde() {
        return desde;
    }

    public String getHasta() {
        return hasta;
    }

    public double getTiempoCaminando() {
        return tiempoCaminando;
    }

    public String getMovimientosPermitidos() {
        return movimientosPermitidos;
    }
}