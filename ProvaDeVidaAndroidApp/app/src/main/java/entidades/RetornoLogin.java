package entidades;

/**
 * Created by josen on 24/10/2017.
 */
public class RetornoLogin {
    public static final int OK = 1;
    public static final int ERRO = 2;
    private int codigo;
    private String cpf;
    private String nome;

    public RetornoLogin() {
    }

    public RetornoLogin(int codigo, String cpf, String nome) {
        this.codigo = codigo;
        this.cpf = cpf;
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
