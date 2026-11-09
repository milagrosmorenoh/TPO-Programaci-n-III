package busqueda;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        // Para probar otro mapa, cambiár el nombre del archivo aca
        Path archivo = Paths.get("datos", "mapa_grande.txt");
        Path archivoLog = Paths.get("log_ejecucion.txt");

        try (BufferedWriter registro = Files.newBufferedWriter(archivoLog)) {
            Buscador buscador = new Buscador();

            long inicio = System.currentTimeMillis();
            ResultadoMision resultado = buscador.resolverMision(archivo, registro);
            long fin = System.currentTimeMillis();

            mostrarYRegistrar(registro, "\nRESULTADO FINAL");

            if (resultado.esMisionCompleta()) {
                mostrarYRegistrar(registro, "Rutas completas encontradas: " + resultado.getRecorridos().size());
            } else {
                mostrarYRegistrar(registro, "No se pudo completar la misión. Mejor recorrido parcial:");
            }

            mostrarYRegistrar(registro, "Movimientos: C = caminando; S = saltando; P = patas para arriba.");

            int numero = 1;

            for (List<NodoBusqueda> recorrido : resultado.getRecorridos()) {
                mostrarYRegistrar(registro, "\nRecorrido " + numero);

                for (NodoBusqueda nodo : recorrido) {
                    if (nodo.getPadre() == null) {
                        mostrarYRegistrar(registro, String.format("Salida: %s | Tiempo: %.2f s | Batería: %.2f", nodo.getEstacion(), nodo.getTiempo(), nodo.getBateria()));
                    } else {
                        mostrarYRegistrar(registro, String.format("%s -> %s (%s) | Tiempo acumulado: %.2f s | Batería: %.2f", nodo.getPadre().getEstacion(), nodo.getEstacion(), nodo.getMovimiento(), nodo.getTiempo(), nodo.getBateria()));
                    }
                }

                NodoBusqueda ultimo = recorrido.get(recorrido.size() - 1);

                mostrarYRegistrar(registro, String.format("Tiempo total: %.2f s | Batería final: %.2f", ultimo.getTiempo(), ultimo.getBateria()));
                numero++;
            }

            mostrarYRegistrar(registro, "\nFIN DE LA EJECUCIÓN");
            mostrarYRegistrar(registro, "Nodos analizados: " + resultado.getNodosAnalizados());
            mostrarYRegistrar(registro, "Nodos expandidos: " + resultado.getNodosExpandidos());
            mostrarYRegistrar(registro, "Tiempo de ejecución práctico: " + (fin - inicio) + " ms");
        }

        System.out.println("\nLog guardado correctamente.");
    }

    // Muestra línea en consola y a su vez la escribe en el archivo
    private static void mostrarYRegistrar(BufferedWriter registro, String texto) throws IOException {
        System.out.println(texto);
        registro.write(texto);
        registro.newLine();
    }
}