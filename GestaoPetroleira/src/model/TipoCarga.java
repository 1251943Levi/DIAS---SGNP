package model;

public class TipoCarga {
    private int id;
    private String nome;
    private boolean inflamavel;
    private boolean corrosiva;
    private boolean toxica;

    public TipoCarga() {}
    public TipoCarga(int id, String nome, boolean inflamavel, boolean corrosiva, boolean toxica) {
        this.id = id; this.nome = nome;
        this.inflamavel = inflamavel; this.corrosiva = corrosiva; this.toxica = toxica;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public boolean isInflamavel() { return inflamavel; } public void setInflamavel(boolean v) { inflamavel = v; }
    public boolean isCorrosiva() { return corrosiva; } public void setCorrosiva(boolean v) { corrosiva = v; }
    public boolean isToxica() { return toxica; } public void setToxica(boolean v) { toxica = v; }

    public String getPropriedades() {
        StringBuilder sb = new StringBuilder();
        if (inflamavel) sb.append("Inflamável ");
        if (corrosiva)  sb.append("Corrosiva ");
        if (toxica)     sb.append("Tóxica");
        String s = sb.toString().trim();
        return s.isEmpty() ? "—" : s;
    }
    @Override public String toString() { return nome; }
}
