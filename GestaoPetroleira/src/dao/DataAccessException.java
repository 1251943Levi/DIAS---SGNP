package dao;

/**
 * Exceção não verificada que sinaliza uma falha de acesso à base de dados.
 * Os DAOs lançam-na (em vez de engolir o erro) para que a camada de serviço
 * ou os controllers possam tratar e mostrar uma mensagem ao utilizador.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
