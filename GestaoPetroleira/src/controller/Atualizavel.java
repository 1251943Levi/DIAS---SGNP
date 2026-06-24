package controller;

/**
 * Implementada pelos controllers cujas tabelas/combos dependem de dados
 * que podem ser criados noutros separadores. O Main chama {@link #atualizar()}
 * sempre que o separador correspondente é selecionado, mantendo tudo sincronizado.
 */
public interface Atualizavel {
    void atualizar();
}
