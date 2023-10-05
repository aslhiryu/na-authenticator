package neoAtlantis.utilidades.ctrlAcceso.cipher.interfaces;

/**
 * Interface que define el comportamiento que debe de tener un <i>Cifrador de Datos</i>.
 * @version 2.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public interface CifradorDatos {

    /**
     * Definici&oacute;n del metodo para cifrar un dato
     * @param str Dato a cifrar
     * @return Dato cifrado
     * @throws Exception
     */
    public String cifra(String str) throws Exception;

    /**
     * Definici&oacute;n del metodo para descifrar un dato
     * @param str Dato a descifrar
     * @return Dato descifrado
     * @throws Exception
     */
    public String descifra(String str) throws Exception;
}
