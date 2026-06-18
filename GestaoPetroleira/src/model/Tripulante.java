package model;

public class Tripulante {
    private int id;
    private String nome;
    private String numeroMatricula;
    private Funcao funcao;
    private boolean disponivel;

    public Tripulante() {}
    public Tripulante(int id, String nome, String numeroMatricula, Funcao funcao, boolean disponivel) {
        this.id = id; this.nome = nome; this.numeroMatricula = numeroMatricula;
        this.funcao = funcao; this.disponivel = disponivel;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getNumeroMatricula() { return numeroMatricula; } public void setNumeroMatricula(String v) { numeroMatricula = v; }
    public Funcao getFuncao() { return funcao; } public void setFuncao(Funcao v) { funcao = v; }
    public boolean isDisponivel() { return disponivel; } public void setDisponivel(boolean v) { disponivel = v; }

    @Override public String toString() { return nome + " (" + (funcao != null ? funcao : "?") + ")"; }
}
