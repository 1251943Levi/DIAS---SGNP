package model;

public class TipoCarga {
    private int id;
    private String nome; // ex: Petróleo bruto, Gasolina, Produtos químicos líquidos
    private boolean inflamavel;
    private boolean corrosiva;
    private boolean toxica;
    public TipoCarga(int id, String nome, boolean inflamavel, boolean corrosiva, boolean toxica) {
        this.id = id;
        this.nome = nome;
        this.inflamavel = inflamavel;
        this.corrosiva = corrosiva;
        this.toxica = toxica;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public boolean isInflamavel() { return inflamavel; }
    public boolean isCorrosiva() { return corrosiva; }
    public boolean isToxica() { return toxica; }
}
