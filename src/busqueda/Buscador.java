package busqueda;

import mapas.Conexion;
import mapas.Mapa;
import mapas.Ventana;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Buscador {

    private ResultadoMovimiento evaluarMovimiento(Mapa mapa, NodoBusqueda nodoActual, Conexion conexion, char movimiento) {

        // Comprobar la conexión, su sentido y el movimiento elegido.
        if (!mapa.getConexionesHabilitadas().contains(conexion)) {
            return new ResultadoMovimiento(null, "Conexión no habilitada");
        }

        if (!conexion.getDesde().equals(nodoActual.getEstacion())) {
            return new ResultadoMovimiento(null, "La conexión no sale de la estación actual");
        }

        if (conexion.getMovimientosPermitidos().indexOf(movimiento) == -1) {
            return new ResultadoMovimiento(null, "Movimiento no permitido");
        }

        // Comprobar si se puede visitar el destino.
        String destino = conexion.getHasta();
        boolean vuelveAlOrigen = destino.equals(mapa.getOrigen());

        if (vuelveAlOrigen && !nodoActual.getObjetivosPendientes().isEmpty()) {
            return new ResultadoMovimiento(null, "Regreso al origen con objetivos pendientes");
        }

        if (!vuelveAlOrigen && nodoActual.yaVisito(destino)) {
            return new ResultadoMovimiento(null, "Estación ya visitada");
        }

        // Leer la duración ya calculada y obtener el consumo de batería.
        double duracion = conexion.getDuracion(movimiento);
        double tiempoCaminando = conexion.getTiempoCaminando();
        double consumo;

        switch (movimiento) {
            case 'C':
                consumo = tiempoCaminando / 60.0;
                break;
            case 'S':
                consumo = 2.2 * (tiempoCaminando / 60.0);
                break;
            case 'P':
                consumo = 0.45 * (tiempoCaminando / 60.0);
                break;
            default:
                return new ResultadoMovimiento(null, "Movimiento desconocido");
        }

        // Comprobar si el destino está abierto al llegar.
        double tiempoLlegada = nodoActual.getTiempo() + duracion;

        for (Ventana ventana : mapa.getVentanas()) {
            if (ventana.getLugar().equals(destino) && tiempoLlegada >= ventana.getInicioCierre() && tiempoLlegada <= ventana.getFinCierre()) {
                return new ResultadoMovimiento(null, "Destino cerrado al llegar");
            }
        }

        // Comprobar la batería antes de aplicar la recarga.
        double bateriaLlegada = nodoActual.getBateria() - consumo;
        int numeroAula = mapa.getEstaciones().get(destino).getAula();

        if (bateriaLlegada < 0) {
            return new ResultadoMovimiento(null, "Batería insuficiente para llegar");
        }

        if (bateriaLlegada == 0 && numeroAula == 0) {
            return new ResultadoMovimiento(null, "Batería cero en una estación sin recarga");
        }

        double bateriaFinal = bateriaLlegada;

        if (numeroAula != 0) {
            double recarga = sumaDeLosDigitos(numeroAula) / 5.0;
            bateriaFinal = Math.min(100, bateriaLlegada + recarga);
        }

        // El paso es válido. El constructor del hijo actualiza sus conjuntos.
        NodoBusqueda hijo = new NodoBusqueda(nodoActual, destino, movimiento, tiempoLlegada, bateriaFinal);

        return new ResultadoMovimiento(hijo, null);
    }

    private int sumaDeLosDigitos(int numero) {
        int suma = 0;
        while (numero > 0) {
            suma += numero % 10;
            numero = numero / 10;
        }
        return suma;
    }

    private NodoBusqueda buscarRutaInicial(Mapa mapa, NodoBusqueda primerHijo) {
        NodoBusqueda nodoActual = primerHijo;

        // Seguir mientras falte visitar algún objetivo o volver al origen.
        while (!nodoActual.getObjetivosPendientes().isEmpty() || !nodoActual.getEstacion().equals(mapa.getOrigen())) {

            NodoBusqueda mejorHijo = null;
            double menorDuracion = Double.POSITIVE_INFINITY;

            for (Conexion conexion : mapa.getConexionesHabilitadas()) {
                // Solo se puede avanzar por conexiones que salen de esta estación.
                if (!conexion.getDesde().equals(nodoActual.getEstacion())) {
                    continue;
                }

                for (char movimiento : conexion.getMovimientosPermitidos().toCharArray()) {
                    ResultadoMovimiento resultado = evaluarMovimiento(mapa, nodoActual, conexion, movimiento);

                    NodoBusqueda hijo = resultado.getHijo();
                    if (hijo == null) {
                        continue;
                    }

                    double duracion = conexion.getDuracion(movimiento);
                    if (duracion < menorDuracion) {
                        menorDuracion = duracion;
                        mejorHijo = hijo;
                    }
                }
            }

            // Si ningún movimiento fue válido, este intento quedó trabado.
            if (mejorHijo == null) {
                return null;
            }

            // Recién después de comparar todas las alternativas avanzamos un paso.
            nodoActual = mejorHijo;
        }

        return nodoActual;
    }

    public ResultadoMision resolverMision(Path archivo) throws IOException {

        // 1. Preparar el mapa y crear la raíz
        Mapa mapa = Mapa.prepararMapa(archivo);
        CalculadorDeCotas calculador = new CalculadorDeCotas(mapa);

        NodoBusqueda raiz = new NodoBusqueda(mapa);
        raiz.setId(1);

        // 2. Inicializar los datos de esta ejecución
        List<NodoBusqueda> mejoresRutas = new ArrayList<>();
        NodoBusqueda mejorParcial = null;
        double l3 = Double.POSITIVE_INFINITY;

        List<String> registro = new ArrayList<>();
        int nodosAnalizados = 1;       // La raíz ya cuenta
        int nodosExpandidos = 0;
        long siguienteOrden = 0;

        PriorityQueue<NodoBusqueda> cola = new PriorityQueue<>(
                Comparator.comparingDouble(NodoBusqueda::getCotaInferior)
                        .thenComparingLong(NodoBusqueda::getOrdenIngreso)
        );

        registro.add("Raíz 1: " + raiz.getEstacion());

        // 3. Intentar rutas greedy desde los hijos válidos de la raíz
        List<NodoBusqueda> primerosHijos = new ArrayList<>();

        for (Conexion conexion : mapa.getConexionesHabilitadas()) {
            if (!conexion.getDesde().equals(raiz.getEstacion())) {
                continue;
            }

            for (char movimiento : conexion.getMovimientosPermitidos().toCharArray()) {
                ResultadoMovimiento paso =
                        evaluarMovimiento(mapa, raiz, conexion, movimiento);

                if (paso.getHijo() != null) {
                    primerosHijos.add(paso.getHijo());
                }
            }
        }

        // El tiempo de un primer hijo es la duración del primer paso.
        // En empates se conserva el orden en que se agregaron a la lista.
        primerosHijos.sort(Comparator.comparingDouble(NodoBusqueda::getTiempo));

        for (NodoBusqueda primerHijo : primerosHijos) {
            NodoBusqueda rutaInicial = buscarRutaInicial(mapa, primerHijo);

            if (rutaInicial != null) {
                l3 = registrarRutaCompleta(rutaInicial, mejoresRutas);
                registro.add("Solución greedy: tiempo=" + rutaInicial.getTiempo()
                        + ", batería=" + rutaInicial.getBateria() + ", L3=" + l3);
            }

            if (mejoresRutas.size() == 3) {
                break;                // Termina el greedy; después empieza B&B
            }
        }

        // 4. Iniciar Branch and Bound desde la raíz
        raiz.setCotaInferior(calculador.calcularCotaInferior(raiz));
        raiz.setOrdenIngreso(siguienteOrden++);
        cola.add(raiz);
        registro.add("Entra raíz a la cola: CI=" + raiz.getCotaInferior());

        while (!cola.isEmpty()) {
            NodoBusqueda actual = cola.poll();
            registro.add("Sale de la cola el nodo " + actual.getId()
                    + ": CI=" + actual.getCotaInferior() + ", L3=" + l3);

            if (actual.getCotaInferior() > l3) {
                registro.add("Fin: la menor CI de la cola supera L3.");
                break;
            }

            nodosExpandidos++;

            for (Conexion conexion : mapa.getConexionesHabilitadas()) {
                if (!conexion.getDesde().equals(actual.getEstacion())) {
                    continue;
                }

                for (char movimiento : conexion.getMovimientosPermitidos().toCharArray()) {
                    ResultadoMovimiento paso =
                            evaluarMovimiento(mapa, actual, conexion, movimiento);

                    NodoBusqueda hijo = paso.getHijo();

                    if (hijo == null) {
                        registro.add("Desde nodo " + actual.getId()
                                + ", " + conexion.getDesde() + " -> " + conexion.getHasta()
                                + " (" + movimiento + "): " + paso.getMotivo()
                                + ". CI no calculada.");
                        continue;
                    }

                    nodosAnalizados++;
                    hijo.setId(nodosAnalizados);

                    registro.add("Nodo " + hijo.getId() + ", padre=" + actual.getId() + ", nivel=" + hijo.getNivel() + ", " + actual.getEstacion() + " -> " + hijo.getEstacion() + " (" + movimiento + "), tiempo=" + hijo.getTiempo() + ", batería=" + hijo.getBateria() + ", L3=" + l3);

                    // 5. Si completó la misión, registrar la ruta sin calcular el MST
                    if (hijo.getEstacion().equals(mapa.getOrigen())
                            && hijo.getObjetivosPendientes().isEmpty()) {
                        l3 = registrarRutaCompleta(hijo, mejoresRutas);
                        registro.add("Solución completa en nodo " + hijo.getId() + ". Mejor tiempo=" + mejoresRutas.get(0).getTiempo() + ", L3=" + l3);
                        continue;
                    }

                    // 6. Guardar el mejor parcial para la contingencia
                    if (esMejorParcial(hijo, mejorParcial, mapa)) {
                        mejorParcial = hijo;
                    }

                    // 7. Aplicar los filtros baratos antes de calcular la cota completa
                    if (l3 != Double.POSITIVE_INFINITY) {
                        if (hijo.getTiempo() > l3) {
                            registro.add("Poda nodo " + hijo.getId() + ": tiempo acumulado > L3. CI no calculada.");
                            continue;
                        }

                        double cotaSimple = calculador.calcularCotaSimple(hijo);
                        if (cotaSimple > l3) {
                            registro.add("Poda nodo " + hijo.getId() + ": cota simple=" + cotaSimple + " > L3. CI completa no calculada.");
                            continue;
                        }
                    }

                    // 8. Calcular la cota completa y decidir si el hijo entra a la cola
                    double ci = calculador.calcularCotaInferior(hijo);
                    hijo.setCotaInferior(ci);

                    if (ci > l3) {
                        registro.add("Poda nodo " + hijo.getId() + ": CI=" + ci + " > L3.");
                        continue;
                    }

                    hijo.setOrdenIngreso(siguienteOrden++);
                    cola.add(hijo);
                    registro.add("Entra nodo " + hijo.getId() + " a la cola: CI=" + ci);
                }
            }
        }

        // 9. Reconstruir las rutas en orden de salida a llegada
        List<List<NodoBusqueda>> recorridos = new ArrayList<>();
        boolean misionCompleta = !mejoresRutas.isEmpty();

        if (misionCompleta) {
            for (NodoBusqueda fin : mejoresRutas) {
                recorridos.add(reconstruirRuta(fin));
            }
        } else {
            if (mejorParcial == null) {
                mejorParcial = raiz;
                registro.add("No hubo ningún primer movimiento válido.");
            }
            recorridos.add(reconstruirRuta(mejorParcial));
        }

        return new ResultadoMision(misionCompleta, recorridos, registro, nodosAnalizados, nodosExpandidos);
    }

    private double registrarRutaCompleta(NodoBusqueda nueva, List<NodoBusqueda> mejoresRutas) {
        // Menor tiempo primero. Si empatan, mayor batería final primero.
        Comparator<NodoBusqueda> porCalidad = Comparator.comparingDouble(NodoBusqueda::getTiempo).thenComparing(Comparator.comparingDouble(NodoBusqueda::getBateria).reversed());

        int posicionRepetida = -1;

        for (int i = 0; i < mejoresRutas.size(); i++) {
            if (mismaRuta(nueva, mejoresRutas.get(i))) {
                posicionRepetida = i;
                break;
            }
        }

        if (posicionRepetida == -1) {
            mejoresRutas.add(nueva);
        } else {
            NodoBusqueda guardada = mejoresRutas.get(posicionRepetida);

            // Un valor negativo indica que la nueva queda antes que la guardada.
            if (porCalidad.compare(nueva, guardada) < 0) {
                mejoresRutas.set(posicionRepetida, nueva);
            }
        }

        mejoresRutas.sort(porCalidad);

        if (mejoresRutas.size() > 3) {
            mejoresRutas.remove(3);
        }

        if (mejoresRutas.size() == 3) {
            return mejoresRutas.get(2).getTiempo();
        }
        return Double.POSITIVE_INFINITY;
    }

    private boolean mismaRuta(NodoBusqueda a, NodoBusqueda b) {
        while (a != null && b != null) {
            if (!a.getEstacion().equals(b.getEstacion()) || !java.util.Objects.equals(a.getMovimiento(), b.getMovimiento())) {
                return false;
            }
            a = a.getPadre();
            b = b.getPadre();
        }
        return a == null && b == null;
    }

    private boolean esMejorParcial(NodoBusqueda candidato, NodoBusqueda mejor, Mapa mapa) {
        if (mejor == null) {
            return true;
        }

        int pendientesCandidato = candidato.getObjetivosPendientes().size();
        int pendientesMejor = mejor.getObjetivosPendientes().size();

        if (pendientesCandidato != pendientesMejor) {
            return pendientesCandidato < pendientesMejor;
        }

        double distanciaCandidato = mapa.distanciaEnLineaRecta(candidato.getEstacion(), mapa.getOrigen());
        double distanciaMejor = mapa.distanciaEnLineaRecta(mejor.getEstacion(), mapa.getOrigen());

        return distanciaCandidato < distanciaMejor;
    }

    private List<NodoBusqueda> reconstruirRuta(NodoBusqueda fin) {
        List<NodoBusqueda> ruta = new ArrayList<>();

        while (fin != null) {
            ruta.add(fin);
            fin = fin.getPadre();
        }
        Collections.reverse(ruta);
        return ruta;
    }
}