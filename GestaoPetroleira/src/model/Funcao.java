package model;

public enum Funcao {
    CAPITAO, OFICIAL, ENGENHEIRO, OPERADOR;

    @Override public String toString() {
        return switch (this) {
            case CAPITAO    -> "Capitão";
            case OFICIAL    -> "Oficial";
            case ENGENHEIRO -> "Engenheiro";
            case OPERADOR   -> "Operador";
        };
    }
}
