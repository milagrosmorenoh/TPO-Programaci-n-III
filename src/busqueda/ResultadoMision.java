package busqueda;

import java.util.List;

public class ResultadoMision {

    private final boolean misionCompleta;
    private final List<List<NodoBusqueda>> recorridos;
    private final int nodosAnalizados;
    private final int nodosExpandidos;

    public ResultadoMision(boolean misionCompleta, List<List<NodoBusqueda>> recorridos, int nodosAnalizados, int nodosExpandidos) {
        this.misionCompleta = misionCompleta;
        this.recorridos = recorridos;
        this.nodosAnalizados = nodosAnalizados;
        this.nodosExpandidos = nodosExpandidos;
    }

    public boolean esMisionCompleta() {
        return misionCompleta;
    }

    public List<List<NodoBusqueda>> getRecorridos() {
        return recorridos;
    }


    public int getNodosAnalizados() {
        return nodosAnalizados;
    }

    public int getNodosExpandidos() {
        return nodosExpandidos;
    }
}