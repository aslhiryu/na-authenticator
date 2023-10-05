package neoAtlantis.utilidades.ctrlAcceso.cipher;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import neoAtlantis.utilidades.ctrlAcceso.cipher.interfaces.CifradorDatos;

/**
 * Cifrador de Datos que utiliza el algoritmo MD5-DES.
 * @version 2.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class CifradorMd5Des implements CifradorDatos {
    /**
     * Versi&oacute;n del cifrador.
     */
    public static final String VERSION = "2.0";

    private static byte[] SALT_BYTES = {
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };
    private static int ITERATION_COUNT = 19;
    private String llave;

    /**
     * Genera un Cifrador de Datos MD5-DES
     * @param llave Frase para cifrar y descifrar
     */
    public CifradorMd5Des(String llave) {
        this.llave = llave;
    }

    /**
     * Cifra un dato
     * @param str Dato a cifrar
     * @return dato cifrado
     * @throws Exception
     */
    public String cifra(String str) throws Exception {
        Cipher ecipher = null;

        // Crear la llave
        KeySpec keySpec = new PBEKeySpec(this.llave.toCharArray(), CifradorMd5Des.SALT_BYTES, CifradorMd5Des.ITERATION_COUNT);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        ecipher = Cipher.getInstance(key.getAlgorithm());

        // Preparar los parametros para los ciphers
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(CifradorMd5Des.SALT_BYTES, CifradorMd5Des.ITERATION_COUNT);

        // Crear los ciphers
        ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        // Cifra la cadena a bytes usando utf-8
        byte[] enc = ecipher.doFinal(str.getBytes("UTF8"));
        return new sun.misc.BASE64Encoder().encode(enc);
    }

    /**
     * Descifra un dato
     * @param str dato a descifrar
     * @return Dato descifrado
     * @throws Exception
     */
    public String descifra(String str) throws Exception {
        Cipher dcipher = null;

        // Crear la key
        KeySpec keySpec = new PBEKeySpec(this.llave.toCharArray(), CifradorMd5Des.SALT_BYTES, CifradorMd5Des.ITERATION_COUNT);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        dcipher = Cipher.getInstance(key.getAlgorithm());

        // Preparar los parametros para los ciphers
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(CifradorMd5Des.SALT_BYTES, CifradorMd5Des.ITERATION_COUNT);

        // Crear los ciphers
        dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        // Decodear base64 y obtener bytes
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

        // Descifra usando utf-8
        return new String(dcipher.doFinal(dec), "UTF8");
    }

    /**
     * Asigna una frase para cifrado / descifrado
     * @param llave Frase a utilizar
     */
    public void setLlave(String llave) {
        this.llave = llave;
    }
}
