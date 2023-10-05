package neoAtlantis.utilidades.accessController.cipher;

import java.security.MessageDigest;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.exceptions.EncryptionException;

/**
 * Cifrador de Datos basado en el algoritmo SHA1.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class CipherSha1 implements DataCipher {

    /**
     * Cifra un dato
     * @param str Dato a cifrar
     * @return Dato cifrado
     * @throws EncryptionException
     */
    public String cifra(String str) throws EncryptionException {
        byte[] digest, buffer = str.getBytes();
        String hash = "";

        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(buffer);
            digest = md.digest();

            for(byte aux : digest) {
                int b = aux & 0xff;
                if (Integer.toHexString(b).length() == 1) hash += "0";
                hash += Integer.toHexString(b);
            }
        }catch(Exception ex){
            throw new EncryptionException(ex);
        }


        return hash;
    }

    /**
     * Descifra un dato
     * @param str Dato a descifrar
     * @return Dato descifrado
     */
    public String descifra(String str) {
        throw new UnsupportedOperationException("No es posible descifrar con SHA1.");
    }

}
