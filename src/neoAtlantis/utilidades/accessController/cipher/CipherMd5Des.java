package neoAtlantis.utilidades.accessController.cipher;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.exceptions.EncryptionException;

/**
 * Cifrador de Datos basado en el algoritmo MD5-DES.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class CipherMd5Des implements DataCipher {
    private static byte[] SALT_BYTES = {
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };
    private static int ITERATION_COUNT = 19;
    private String llave;

    /**
     * Constructor base.
     * @param llave Cadena que se utilizar&aacute; como frase para cifrar y descifrar
     */
    public CipherMd5Des(String llave) {
        this.llave = llave;
    }

    /**
     * Cifra una Cadena
     * @param str Cadena a cifrar
     * @return Cadena cifrada
     * @throws EncryptionException
     */
    public String cifra(String str) throws EncryptionException {
        Cipher ecipher = null;

        try{
            // Crear la llave
            KeySpec keySpec = new PBEKeySpec(this.llave.toCharArray(), SALT_BYTES, ITERATION_COUNT);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(key.getAlgorithm());

            // Preparar los parametros para los ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT_BYTES, ITERATION_COUNT);

            // Crear los ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

            // Cifra la cadena a bytes usando utf-8
            byte[] enc = ecipher.doFinal(str.getBytes("UTF8"));
            return new sun.misc.BASE64Encoder().encode(enc);
        }
        catch(Exception ex){
            throw new EncryptionException(ex);
        }
    }

    /**
     * Descifra una cadena
     * @param str Cadena a descifrar
     * @return Cadena descifrada
     * @throws EncryptionException
     */
    public String descifra(String str) throws EncryptionException {
        Cipher dcipher = null;

        try{
            // Crear la key
            KeySpec keySpec = new PBEKeySpec(this.llave.toCharArray(), SALT_BYTES, ITERATION_COUNT);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            dcipher = Cipher.getInstance(key.getAlgorithm());

            // Preparar los parametros para los ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT_BYTES, ITERATION_COUNT);

            // Crear los ciphers
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

            // Decodear base64 y obtener bytes
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

            // Descifra usando utf-8
            return new String(dcipher.doFinal(dec), "UTF8");
        }
        catch(Exception ex){
            throw new EncryptionException(ex);
        }
    }

    /**
     * Asigna la llave con la que se cifrar&aacute;n los datos.
     * @param llave Cadena que se utilizar&aacute; como frase para cifrar y descifrar
     */
    public void setLlave(String llave) {
        this.llave = llave;
    }
}
