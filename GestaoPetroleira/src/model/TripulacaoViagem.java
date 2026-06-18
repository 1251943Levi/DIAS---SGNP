package model;

public class TripulacaoViagem {
    private int id;
    private Viagem viagem;
    private Tripulante tripulante;
    private Funcao funcao;

    public TripulacaoViagem() {}
    public TripulacaoViagem(int id, Viagem viagem, Tripulante tripulante, Funcao funcao) {
        this.id = id; this.viagem = viagem; this.tripulante = tripulante; this.funcao = funcao;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public Viagem getViagem() { return viagem; } public void setViagem(Viagem v) { viagem = v; }
    public Tripulante getTripulante() { return tripulante; } public void setTripulante(Tripulante v) { tripulante = v; }
    public Funcao getFuncao() { return funcao; } public void setFuncao(Funcao v) { funcao = v; }

    @Override public String toString() {
        return tripulante.getNome() + " — " + funcao + " (Viagem " + viagem.getId() + ")";
    }
}
