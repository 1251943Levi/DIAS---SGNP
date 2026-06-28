package model;

public class Compatibilidade {
    private int id;
    private TipoNavio tipoNavio;
    private TipoCarga tipoCarga;

    public Compatibilidade() {}
    public Compatibilidade(int id, TipoNavio tipoNavio, TipoCarga tipoCarga) {
        this.id = id; this.tipoNavio = tipoNavio; this.tipoCarga = tipoCarga;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public TipoNavio getTipoNavio() { return tipoNavio; } public void setTipoNavio(TipoNavio v) { tipoNavio = v; }
    public TipoCarga getTipoCarga() { return tipoCarga; } public void setTipoCarga(TipoCarga v) { tipoCarga = v; }

    @Override public String toString() { return tipoNavio.getNome() + " ↔ " + tipoCarga.getNome(); }
}
