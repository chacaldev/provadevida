package entidades;

/**
 * Created by josen on 25/10/2017.
 */
public class RetornoValidar {
    public static final Long OK  = 0l;
    public static final Long ERRO_QUALIDADE  = 1l;
    public static final Long ERRO_NAO_RECONHECEU  = 2l;
    public static final Long ERRO_FACE  = -1l;

    private Long codigo;

    public RetornoValidar(Long codigo) {
        this.codigo = codigo;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }
}
