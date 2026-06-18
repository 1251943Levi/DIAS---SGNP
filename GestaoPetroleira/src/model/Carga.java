package model;

public class Carga {
    private int id;
    private String designacao;
    private TipoCarga tipoCarga;
    private double volume; // Em metros cúbicos (m3) ou barris
    private double peso;   // Em toneladas
    private String portoCarga;
    private String portoDescarga;
    public Carga(int id, String designacao, TipoCarga tipoCarga, double volume, double peso, String portoCarga, String portoDescarga) {
        this.id = id;
        this.designacao = designacao;
        this.tipoCarga = tipoCarga;
        this.volume = volume;
        this.peso = peso;
        this.portoCarga = portoCarga;
        this.portoDescarga = portoDescarga;
    }
    // Getters e Setters
    public int getId() { return id; }
    public double getVolume() { return volume; }
    public TipoCarga getTipoCarga() { return tipoCarga; }
}
