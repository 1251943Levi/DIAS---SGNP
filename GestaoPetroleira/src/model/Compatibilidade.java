package model;

public class Compatibilidade {
    private int idTipoNavio;
    private int idTipoCarga;
    public Compatibilidade(int idTipoNavio, int idTipoCarga) {
        this.idTipoNavio = idTipoNavio;
        this.idTipoCarga = idTipoCarga;
    }
    // Getters e Setters
    public int getIdTipoNavio() { return idTipoNavio; }
    public int getIdTipoCarga() { return idTipoCarga; }
}
