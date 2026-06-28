package model;

public class Porto {

    private int id;
    private String nome;
    private String pais;
    private String codigo;   // codigo de porto (ex. UN/LOCODE), opcional

    public Porto() {}

    public Porto(int id, String nome, String pais, String codigo) {
        this.id = id;
        this.nome = nome;
        this.pais = pais;
        this.codigo = codigo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    @Override
    public String toString() {
        return nome + (codigo != null && !codigo.isBlank() ? " (" + codigo + ")" : "");
    }
}
