package busqueda;

import mapas.Conexion;
import mapas.Mapa;
import mapas.Ventana;

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
}