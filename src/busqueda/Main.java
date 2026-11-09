package busqueda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        // Para probar otro mapa, cambiá el nombre del archivo en esta línea.
        Path archivo = Paths.get("datos", "mapa_grande.txt");

        Buscador buscador = new Buscador();

        long tiempoInicio = System.currentTimeMillis();
        ResultadoMision resultado = buscador.resolverMision(archivo);
        long tiempoFin = System.currentTimeMillis();

        if (resultado.esMisionCompleta()) {
            System.out.println("Rutas completas encontradas: "
                    + resultado.getRecorridos().size());
        } else {
            System.out.println("No se pudo completar la misión. Mejor recorrido parcial:");
        }

        int numero = 1;
        for (List<NodoBusqueda> recorrido : resultado.getRecorridos()) {
            System.out.println("\nRecorrido " + numero);

            for (NodoBusqueda nodo : recorrido) {
                if (nodo.getPadre() == null) {
                    System.out.println("Salida: " + nodo.getEstacion());
                } else {
                    System.out.printf("  %s -> %s (%s)%n",
                            nodo.getPadre().getEstacion(),
                            nodo.getEstacion(),
                            nodo.getMovimiento());
                }
            }

            NodoBusqueda ultimo = recorrido.get(recorrido.size() - 1);
            System.out.printf("Tiempo total: %.2f segundos%n", ultimo.getTiempo());
            System.out.printf("Batería final: %.2f%n", ultimo.getBateria());
            numero++;
        }

        System.out.println("\nNodos analizados: " + resultado.getNodosAnalizados());
        System.out.println("Nodos expandidos: " + resultado.getNodosExpandidos());
        System.out.println("Tiempo de ejecución práctico: " + (tiempoFin - tiempoInicio) + " ms");

        List<String> logOrdenado = new ArrayList<>();


        if (resultado.esMisionCompleta()) {
            logOrdenado.add("Rutas completas distintas: " + resultado.getRecorridos().size());
        } else {
            logOrdenado.add("No se encontró una solución completa.");
            logOrdenado.add("Se devuelve el mejor recorrido parcial.");
        }

        int puesto = 1;

        for (List<NodoBusqueda> recorrido : resultado.getRecorridos()) {
            NodoBusqueda ultimo = recorrido.get(recorrido.size() - 1);

            logOrdenado.add("");
            logOrdenado.add("Recorrido " + puesto);

            for (NodoBusqueda nodo : recorrido) {
                if (nodo.getPadre() == null) {
                    logOrdenado.add("Salida: " + nodo.getEstacion());
                } else {
                    logOrdenado.add(String.format("%s -> %s (%s) | Tiempo acumulado: %.2f s" + " | Batería: %.2f", nodo.getPadre().getEstacion(), nodo.getEstacion(), nodo.getMovimiento(), nodo.getTiempo(), nodo.getBateria()));
                }
            }

            logOrdenado.add(String.format("Tiempo total: %.2f s | Batería final: %.2f", ultimo.getTiempo(), ultimo.getBateria()));
            puesto++;
        }

        logOrdenado.add("");
        logOrdenado.add("=== SOLUCIONES DEL GREEDY INICIAL ===");

        boolean huboSolucionGreedy = false;

        for (String evento : resultado.getRegistro()) {
            if (evento.startsWith("Solución greedy:")) {
                logOrdenado.add(evento);
                huboSolucionGreedy = true;
            }
        }

        if (!huboSolucionGreedy) {
            logOrdenado.add("Los intentos greedy no encontraron rutas completas.");
        }

        logOrdenado.add("");
        logOrdenado.add("=== REGISTRO COMPLETO EN ORDEN DE EJECUCIÓN ===");

        logOrdenado.addAll(resultado.getRegistro());

        logOrdenado.add("");
        logOrdenado.add("=== FIN DE LA EJECUCIÓN ===");
        logOrdenado.add("Nodos analizados: " + resultado.getNodosAnalizados());
        logOrdenado.add("Nodos expandidos: " + resultado.getNodosExpandidos());

        Path archivoLog = Paths.get("log_ejecucion.txt");
        Files.write(archivoLog, logOrdenado);

        System.out.println("\nLog guardado correctamente.");
    }
}