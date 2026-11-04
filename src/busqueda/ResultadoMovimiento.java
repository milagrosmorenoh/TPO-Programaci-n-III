package busqueda;

public class ResultadoMovimiento {

    private final NodoBusqueda hijo;
    private final String motivo;

    public ResultadoMovimiento(NodoBusqueda hijo, String motivo) {
        this.hijo = hijo;
        this.motivo = motivo;
    }

    public NodoBusqueda getHijo() {
        return hijo;
    }

    public String getMotivo() {
        return motivo;
    }
}