package model;

import java.time.LocalDate;

public class Viagem {

    private int id;
    private Navio navio;
    private Porto portoOrigem;
    private Porto portoDestino;
    private LocalDate dataPartida;
    private LocalDate dataChegada;   // null enquanto nao concluida
    private EstadoViagem estado;

    public Viagem() {}

    public Viagem(int id, Navio navio, Porto portoOrigem, Porto portoDestino,
                  LocalDate dataPartida, LocalDate dataChegada, EstadoViagem estado) {
        this.id = id;
        this.navio = navio;
        this.portoOrigem = portoOrigem;
        this.portoDestino = portoDestino;
        this.dataPartida = dataPartida;
        this.dataChegada = dataChegada;
        this.estado = estado;
    }

    /** Uma viagem que ainda "ocupa" o navio: planeada ou em curso. */
    public boolean estaAtiva() {
        return estado == EstadoViagem.PLANEADA || estado == EstadoViagem.EM_CURSO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Navio getNavio() { return navio; }
    public void setNavio(Navio navio) { this.navio = navio; }

    public Porto getPortoOrigem() { return portoOrigem; }
    public void setPortoOrigem(Porto portoOrigem) { this.portoOrigem = portoOrigem; }

    public Porto getPortoDestino() { return portoDestino; }
    public void setPortoDestino(Porto portoDestino) { this.portoDestino = portoDestino; }

    public LocalDate getDataPartida() { return dataPartida; }
    public void setDataPartida(LocalDate dataPartida) { this.dataPartida = dataPartida; }

    public LocalDate getDataChegada() { return dataChegada; }
    public void setDataChegada(LocalDate dataChegada) { this.dataChegada = dataChegada; }

    public EstadoViagem getEstado() { return estado; }
    public void setEstado(EstadoViagem estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Viagem " + id + " - " + navio.getNome()
                + " (" + portoOrigem + " -> " + portoDestino + ") [" + estado + "]";
    }
}
