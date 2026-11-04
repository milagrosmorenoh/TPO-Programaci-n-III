package busqueda;

import mapas.Mapa;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class CalculadorDeCotas {

    private final Mapa mapa;
    // Clave: conjunto de pendientes. Valor: costo de árbol en segundos.
    private final Map<Set<String>, Double> costosMST = new HashMap<>();

    public CalculadorDeCotas(Mapa mapa) {
        this.mapa = mapa;
    }

    //Funcion h -> tiempo optimista entre dos estaciones.
    private double h(String a, String b) {
        if (mapa.getVMax() == 0) {
            return 0;
        }

        double distancia = mapa.distanciaEnLineaRecta(a, b);
        return distancia / mapa.getVMax();
    }

    //Prim
    private double costoMST(Set<String> pendientes) {
        // Caso Base: con cero o un objetivo, no hay aristas que sumar.
        if (pendientes.size() <= 1) {
            return 0;
        }

        // Si otra rama tenía los mismos pendientes, reutilizamos el costo.
        Double costoGuardado = costosMST.get(pendientes);
        if (costoGuardado != null) {
            return costoGuardado;
        }

        // Elegimos cualquier objetivo como inicio del árbol.
        Set<String> fueraDelArbol = new HashSet<>(pendientes);
        String inicial = fueraDelArbol.iterator().next();
        fueraDelArbol.remove(inicial);

        // Por ahora, el único objetivo dentro del árbol es el inicial.
        Map<String, Double> menorPeso = new HashMap<>();
        for (String objetivo : fueraDelArbol) {
            menorPeso.put(objetivo, h(inicial, objetivo));
        }

        double costoArbol = 0;

        while (!fueraDelArbol.isEmpty()) {
            // Elegimos el objetivo que cuesta menos incorporar.
            String elegido = null;
            double pesoElegido = Double.POSITIVE_INFINITY;

            for (String objetivo : fueraDelArbol) {
                double peso = menorPeso.get(objetivo);

                if (peso < pesoElegido) {
                    elegido = objetivo;
                    pesoElegido = peso;
                }
            }

            costoArbol += pesoElegido;
            fueraDelArbol.remove(elegido);

            // El recién incorporado puede ofrecer una conexión más barata.
            for (String objetivo : fueraDelArbol) {
                double pesoNuevo = h(elegido, objetivo);

                if (pesoNuevo < menorPeso.get(objetivo)) {
                    menorPeso.put(objetivo, pesoNuevo);
                }
            }
        }

        costosMST.put(new HashSet<>(pendientes), costoArbol);
        return costoArbol;
    }

    // Filtro barato: tiempo acumulado más el regreso optimista al origen.
    public double calcularCotaSimple(NodoBusqueda nodo) {
        return nodo.getTiempo() + h(nodo.getEstacion(), mapa.getOrigen());
    }

    // Cota completa: estima el tiempo total de la misión desde esta rama.
    public double calcularCotaInferior(NodoBusqueda nodo) {
        String actual = nodo.getEstacion();
        String origen = mapa.getOrigen();
        Set<String> pendientes = nodo.getObjetivosPendientes();

        // Caso Base: si ya visitó todos los objetivos, solo queda volver al origen.
        if (pendientes.isEmpty()) {
            return calcularCotaSimple(nodo);
        }

        double tiempoEntrada = Double.POSITIVE_INFINITY;
        double tiempoRegreso = Double.POSITIVE_INFINITY;

        for (String objetivo : pendientes) {
            tiempoEntrada = Math.min(tiempoEntrada, h(actual, objetivo));
            tiempoRegreso = Math.min(tiempoRegreso, h(objetivo, origen));
        }

        return nodo.getTiempo() + tiempoEntrada + costoMST(pendientes) + tiempoRegreso;
    }

}