package model;

public class TipoNavio {

    private int id;
    private String nome;
    private double capacidadeMaxima;
    private int maxCargas;

    public TipoNavio() {}

    public TipoNavio(int id, String nome, double capacidadeMaxima, int maxCargas) {
        this.id = id;
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.maxCargas = maxCargas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(double capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public int getMaxCargas() { return maxCargas; }
    public void setMaxCargas(int maxCargas) { this.maxCargas = maxCargas; }

    @Override
    public String toString() { return nome; }
}