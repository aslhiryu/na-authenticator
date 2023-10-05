package neoAtlantis.utilidades.accessController.cipher.interfaces;

import neoAtlantis.utilidades.accessController.exceptions.EncryptionException;

/**
 * Interface que define el comportamiento que debe de tener un Cifrador de Datos.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public interface DataCipher {

    /**
     * Definici&oacute;n del metodo para cifrar un dato
     * @param str Dato a cifrar
     * @return Dato cifrado
     * @throws EncryptionException
     */
    public String cifra(String str) throws EncryptionException;

    /**
     * Definici&oacute;n del metodo para descifrar un dato
     * @param str Dato a descifrar
     * @return Dato descifrado
     * @throws EncryptionException
     */
    public String descifra(String str) throws EncryptionException;
}
