package model;

import java.time.LocalDate;

public class Manutencao {

    private int id;
    private Navio navio;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String descricao;

    public Manutencao() {}

    public Manutencao(int id, Navio navio, LocalDate dataInicio, LocalDate dataFim, String descricao) {
        this.id = id;
        this.navio = navio;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.descricao = descricao;
    }

    public boolean emCurso() { return dataFim == null; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Navio getNavio() { return navio; }
    public void setNavio(Navio navio) { this.navio = navio; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return "Manutenção " + id + " — " + navio.getNome()
                + " (" + dataInicio + " → " + (dataFim != null ? dataFim : "em curso") + ")";
    }
}