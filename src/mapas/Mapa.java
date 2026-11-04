package mapas;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mapa {

    private final Map<String, Estacion> estaciones = new HashMap<>();
    private final List<Conexion> conexionesHabilitadas = new ArrayList<>();
    private final List<Ventana> ventanas = new ArrayList<>();
    private final Set<String> objetivosObligatorios = new HashSet<>();

    private String origen;
    private double vMax;

    public static Mapa prepararMapa(Path archivo) throws IOException {
        Mapa mapa = new Mapa();
        List<Conexion> conexionesLeidas = new ArrayList<>();

        // 1. Leer el archivo línea por línea
        try (BufferedReader lector = Files.newBufferedReader(archivo)) {

            for (String linea = lector.readLine(); linea != null; linea = lector.readLine()) {

                linea = linea.trim();

                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] campos = linea.split(";");

                switch (campos[0]) {
                    case "LUGAR":
                        String nombre = campos[1];
                        int aula = Integer.parseInt(campos[2]);
                        double x = Double.parseDouble(campos[3]);
                        double y = Double.parseDouble(campos[4]);
                        boolean obligatorio = campos[5].equals("1");
                        boolean prohibido = campos[6].equals("1");

                        Estacion estacion = new Estacion(aula, x, y, obligatorio, prohibido);

                        mapa.estaciones.put(nombre, estacion);

                        if (obligatorio) {
                            mapa.objetivosObligatorios.add(nombre);
                        }
                        break;

                    case "ARISTA":
                        String desde = campos[1];
                        String hasta = campos[2];
                        double tiempoCaminando = Double.parseDouble(campos[3]);
                        String movimientosPermitidos = campos[4];

                        Conexion conexion = new Conexion(desde, hasta, tiempoCaminando, movimientosPermitidos);

                        conexionesLeidas.add(conexion);
                        break;

                    case "VENTANA":
                        String lugar = campos[1];
                        int inicioCierre = Integer.parseInt(campos[2]);
                        int finCierre = Integer.parseInt(campos[3]);

                        Ventana ventana = new Ventana(lugar, inicioCierre, finCierre);
                        mapa.ventanas.add(ventana);
                        break;

                    case "INICIO":
                        mapa.origen = campos[1];
                        break;

                    default: throw new IOException("Tipo de línea desconocido: " + campos[0]);
                }
            }
        }

        // 2. Conservar las conexiones cuyos dos extremos están habilitados
        for (Conexion conexion : conexionesLeidas) {
            Estacion salida = mapa.estaciones.get(conexion.getDesde());
            Estacion destino = mapa.estaciones.get(conexion.getHasta());

            if (salida == null || destino == null) {
                throw new IOException("Estación desconocida en la conexión de " + conexion.getDesde() + " a " + conexion.getHasta());
            }

            if (!salida.esProhibida() && !destino.esProhibida()) {
                mapa.conexionesHabilitadas.add(conexion);
            }
        }

        // 3. Calcular las duraciones de cada conexión habilitada y obtener vMax
        mapa.vMax = 0;

        for (Conexion conexion : mapa.conexionesHabilitadas) {
            double tiempo = conexion.getTiempoCaminando();

            if (tiempo <= 0 || !Double.isFinite(tiempo) || conexion.getMovimientosPermitidos().isEmpty()) {
                throw new IOException("Tiempo o movimientos inválidos en la conexión de " + conexion.getDesde() + " a " + conexion.getHasta());
            }

            conexion.calcularDuraciones();

            double distancia = mapa.distanciaEnLineaRecta(conexion.getDesde(), conexion.getHasta());

            double velocidad = distancia / conexion.getMenorDuracion();
            mapa.vMax = Math.max(mapa.vMax, velocidad);
        }

        if (!mapa.estaciones.containsKey(mapa.origen)) {
            throw new IOException("El mapa debe tener un INICIO con una estación válida.");
        }

        return mapa;
    }

    public double distanciaEnLineaRecta(String desde, String hasta) {
        Estacion salida = estaciones.get(desde);
        Estacion destino = estaciones.get(hasta);

        double diferenciaX = destino.getX() - salida.getX();
        double diferenciaY = destino.getY() - salida.getY();

        return Math.hypot(diferenciaX, diferenciaY);
    }

    public Map<String, Estacion> getEstaciones() {
        return Collections.unmodifiableMap(estaciones);
    }

    public List<Conexion> getConexionesHabilitadas() {
        return Collections.unmodifiableList(conexionesHabilitadas);
    }

    public List<Ventana> getVentanas() {
        return Collections.unmodifiableList(ventanas);
    }

    public Set<String> getObjetivosObligatorios() {
        return Collections.unmodifiableSet(objetivosObligatorios);
    }

    public String getOrigen() {
        return origen;
    }

    public double getVMax() {
        return vMax;
    }
}