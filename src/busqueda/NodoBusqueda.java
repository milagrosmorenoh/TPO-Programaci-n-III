package busqueda;

import mapas.Mapa;
import java.util.HashSet;
import java.util.Set;

public class NodoBusqueda {

    private final String estacion;
    private final double tiempo;
    private final double bateria;
    private final Set<String> visitados;
    private final Set<String> objetivosPendientes;
    private final NodoBusqueda padre;
    private final Character movimiento;
    private final int nivel;

    // Constructor de la raíz: Uada todavía no hizo ningún movimiento.
    public NodoBusqueda(Mapa mapa) {
        this.estacion = mapa.getOrigen();
        this.tiempo = 0;
        this.bateria = 100;

        this.visitados = new HashSet<>();
        this.visitados.add(this.estacion);

        this.objetivosPendientes = new HashSet<>(mapa.getObjetivosObligatorios());

        this.padre = null;
        this.movimiento = null;
        this.nivel = 0;
    }

    // Sobrecargamos el constructor
    public NodoBusqueda(NodoBusqueda padre, String destino, char movimiento, double tiempoLlegada, double bateriaFinal) {
        this.padre = padre;
        this.estacion = destino;
        this.movimiento = movimiento;
        this.tiempo = tiempoLlegada;
        this.bateria = bateriaFinal;
        this.nivel = padre.nivel + 1;

        this.visitados = new HashSet<>(padre.visitados);
        this.visitados.add(destino);

        this.objetivosPendientes = new HashSet<>(padre.objetivosPendientes);
        this.objetivosPendientes.remove(destino);
    }
}