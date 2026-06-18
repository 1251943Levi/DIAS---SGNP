package model;

public class Navio {
    private int id;
    private String nome;
    private String codigoImo;
    private TipoNavio tipoNavio;
    private double capacidadeMaxima;
    private int numCompartimentos;
    private String bandeira;
    private int anoFabrico;
    private EstadoOperacional estadoOperacional;
    private Integer idPortoAtual;

    public Navio() {}

    public Navio(int id, String nome, String codigoImo, TipoNavio tipoNavio,
                 double capacidadeMaxima, int numCompartimentos, String bandeira, int anoFabrico,
                 EstadoOperacional estadoOperacional, Integer idPortoAtual) {
        this.id = id; this.nome = nome; this.codigoImo = codigoImo;
        this.tipoNavio = tipoNavio; this.capacidadeMaxima = capacidadeMaxima;
        this.numCompartimentos = numCompartimentos; this.bandeira = bandeira;
        this.anoFabrico = anoFabrico; this.estadoOperacional = estadoOperacional;
        this.idPortoAtual = idPortoAtual;
    }

    public boolean podeIniciarViagem() { return estadoOperacional == EstadoOperacional.ATIVO; }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getNome() { return nome; } public void setNome(String nome) { this.nome = nome; }
    public String getCodigoImo() { return codigoImo; } public void setCodigoImo(String v) { codigoImo = v; }
    public TipoNavio getTipoNavio() { return tipoNavio; } public void setTipoNavio(TipoNavio v) { tipoNavio = v; }
    public double getCapacidadeMaxima() { return capacidadeMaxima; } public void setCapacidadeMaxima(double v) { capacidadeMaxima = v; }
    public int getNumCompartimentos() { return numCompartimentos; } public void setNumCompartimentos(int v) { numCompartimentos = v; }
    public String getBandeira() { return bandeira; } public void setBandeira(String v) { bandeira = v; }
    public int getAnoFabrico() { return anoFabrico; } public void setAnoFabrico(int v) { anoFabrico = v; }
    public EstadoOperacional getEstadoOperacional() { return estadoOperacional; }
    public void setEstadoOperacional(EstadoOperacional v) { estadoOperacional = v; }
    public Integer getIdPortoAtual() { return idPortoAtual; } public void setIdPortoAtual(Integer v) { idPortoAtual = v; }

    @Override public String toString() { return nome + " (" + codigoImo + ")"; }
}
