package model;

public class Carga {
    private int id;
    private String designacao;
    private TipoCarga tipoCarga;
    private double volume;
    private double peso;
    private Porto portoCarga;
    private Porto portoDescarga;
    private Integer numeroTanque;   // tanque do navio que esta carga ocupa (null = sem tanque/sem viagem)

    public Carga() {}
    public Carga(int id, String designacao, TipoCarga tipoCarga,
                 double volume, double peso, Porto portoCarga, Porto portoDescarga) {
        this.id = id; this.designacao = designacao; this.tipoCarga = tipoCarga;
        this.volume = volume; this.peso = peso;
        this.portoCarga = portoCarga; this.portoDescarga = portoDescarga;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getDesignacao() { return designacao; } public void setDesignacao(String v) { designacao = v; }
    public TipoCarga getTipoCarga() { return tipoCarga; } public void setTipoCarga(TipoCarga v) { tipoCarga = v; }
    public double getVolume() { return volume; } public void setVolume(double v) { volume = v; }
    public double getPeso() { return peso; } public void setPeso(double v) { peso = v; }
    public Porto getPortoCarga() { return portoCarga; } public void setPortoCarga(Porto v) { portoCarga = v; }
    public Porto getPortoDescarga() { return portoDescarga; } public void setPortoDescarga(Porto v) { portoDescarga = v; }
    public Integer getNumeroTanque() { return numeroTanque; } public void setNumeroTanque(Integer v) { numeroTanque = v; }

    @Override public String toString() {
        return designacao + " (" + (tipoCarga != null ? tipoCarga.getNome() : "?") + ", " + peso + " t)";
    }
}
