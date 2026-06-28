package model;

/**
 * Linha do histórico de entregas (só de leitura).
 * Representa uma carga que foi efetivamente entregue numa viagem concluída,
 * com o seu percurso (porto de carga -> porto de descarga) e datas.
 *
 * Não é uma entidade persistida própria: resulta de uma consulta que junta
 * CARGA + VIAGEM + NAVIO + TIPO_CARGA + PORTO para viagens no estado CONCLUIDA.
 */
public class EntregaHistorico {

    private final int    idViagem;
    private final String navio;
    private final String carga;
    private final String tipo;
    private final double peso;
    private final String tanque;
    private final String portoCarga;
    private final String portoDescarga;
    private final String partida;
    private final String chegada;
    private final String estado;

    public EntregaHistorico(int idViagem, String navio, String carga, String tipo, double peso,
                            String tanque, String portoCarga, String portoDescarga,
                            String partida, String chegada, String estado) {
        this.idViagem = idViagem;
        this.navio = navio;
        this.carga = carga;
        this.tipo = tipo;
        this.peso = peso;
        this.tanque = tanque;
        this.portoCarga = portoCarga;
        this.portoDescarga = portoDescarga;
        this.partida = partida;
        this.chegada = chegada;
        this.estado = estado;
    }

    public int    getIdViagem()      { return idViagem; }
    public String getNavio()         { return navio; }
    public String getCarga()         { return carga; }
    public String getTipo()          { return tipo; }
    public double getPeso()          { return peso; }
    public String getTanque()        { return tanque; }
    public String getPortoCarga()    { return portoCarga; }
    public String getPortoDescarga() { return portoDescarga; }
    public String getPartida()       { return partida; }
    public String getChegada()       { return chegada; }
    public String getEstado()        { return estado; }

    /** Texto do percurso para pesquisa/apresentação. */
    public String getPercurso() { return portoCarga + " -> " + portoDescarga; }
}
