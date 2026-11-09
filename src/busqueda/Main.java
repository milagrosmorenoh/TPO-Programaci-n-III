package busqueda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        Path archivoLog = Paths.get("log_ejecucion.txt");
        Files.write(archivoLog, resultado.getRegistro());
        System.out.println("\nLog guardado con éxito en el archivo: " + archivoLog.toAbsolutePath());
    }
}