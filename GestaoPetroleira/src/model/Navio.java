package model;

public class Navio {

    private int id;
    private String nome;
    private String codigoImo;
    private TipoNavio tipoNavio;
    private EstadoOperacional estadoOperacional;
    private Integer idPortoAtual;

    public Navio() {}

    public Navio(int id, String nome, String codigoImo, TipoNavio tipoNavio,
                 EstadoOperacional estadoOperacional, Integer idPortoAtual) {
        this.id = id;
        this.nome = nome;
        this.codigoImo = codigoImo;
        this.tipoNavio = tipoNavio;
        this.estadoOperacional = estadoOperacional;
        this.idPortoAtual = idPortoAtual;
    }

    public boolean podeIniciarViagem() {
        return estadoOperacional == EstadoOperacional.ATIVO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigoImo() { return codigoImo; }
    public void setCodigoImo(String codigoImo) { this.codigoImo = codigoImo; }

    public TipoNavio getTipoNavio() { return tipoNavio; }
    public void setTipoNavio(TipoNavio tipoNavio) { this.tipoNavio = tipoNavio; }

    public EstadoOperacional getEstadoOperacional() { return estadoOperacional; }
    public void setEstadoOperacional(EstadoOperacional estadoOperacional) { this.estadoOperacional = estadoOperacional; }

    public Integer getIdPortoAtual() { return idPortoAtual; }
    public void setIdPortoAtual(Integer idPortoAtual) { this.idPortoAtual = idPortoAtual; }

    @Override
    public String toString() { return nome + " (" + codigoImo + ")"; }
}