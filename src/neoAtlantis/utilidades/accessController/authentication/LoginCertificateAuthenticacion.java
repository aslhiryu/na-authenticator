package neoAtlantis.utilidades.accessController.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.User;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;


//
/**
 * Medio Autenticador operado a traves de certificados X509.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.5
 */
public class LoginCertificateAuthenticacion extends AuthenticationWay {
    static final Logger logger = Logger.getLogger(LoginCertificateAuthenticacion.class);
    protected Properties config;
    protected X509Certificate cert;

    /**
     * Genera un AuthenticationWay por certificado.
     * @param config Configuraci&oacute;n del certificado
     * @throws java.lang.Exception
     */
    public LoginCertificateAuthenticacion(Properties config) throws WayConfigurationException {
        this.config=config;
    }

    //--------------------------------------------------------------------------------

    /**
     * DValida un usuario.
     * @param datos Variables que se deben de utilizar para realizar la autenticación
     * @return Usuario que se encontro al realizar la autenticacion:
     * @throws java.lang.Exception
     */
    @Override
    public User autenticaUsuario(Map<String, Object> datos) throws WayAccessException {
        /*InputStream inStream = new FileInputStream("d:/informacion/micert.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        cert = (X509Certificate)cf.generateCertificate(inStream);
        inStream.close();
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        
        System.out.println(cert.getSubjectDN().getName());
        System.out.println(cert.getSerialNumber());*/
        //System.out.println(cert.getExtensionValue("SubjectKeyIdentifier"));
        
        /*File f=new File("d:/informacion/mikey.key");
        byte[] fBytes=new byte[(int)f.length()];
        FileInputStream dis=new FileInputStream(f);        
        dis.read(fBytes);
        dis.close();
        KeyFactory kf=KeyFactory.getInstance("RSA");
        PrivateKey pk=kf.generatePrivate(new PKCS8EncodedKeySpec(fBytes));
        System.out.println(pk);
         */
        
        /*Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = readKeyPair(new File("d:/informacion/mikey.key"), "password".toCharArray());
        System.out.println(keyPair);*/
        
        try{
            PEMReader pemReader = new PEMReader(new InputStreamReader(new FileInputStream("d:/informacion/mikey.key")));
            Object obj;
            while ((obj = pemReader.readObject()) != null) {
                    if (obj instanceof X509Certificate) {
                            System.out.println("X509Certificate found");
                    } else if (obj instanceof PrivateKey) {
                            PrivateKey key = (PrivateKey) obj;
                            System.out.println("Private Key found");
                    } else if (obj instanceof KeyPair) {
                            KeyPair keyPair = (KeyPair) obj;
                            System.out.println("KeyPair found");
                    } else {
                            System.out.println(" SOMETING ELSE FOUND");
                    }
            }
        }
        catch(Exception ex){
            throw new WayAccessException(ex);
        }
        
        /*PKCS8Key key= new PKCS8Key(new FileInputStream("d:/informacion/mikey.key"), ("bluemary13569").toCharArray());
        System.out.println(key);*/
        
        return null;
    }

    @Override
    public boolean agregaCuenta(User user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean agregaCuentaTemporal(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean modificaContrasena(User user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean restauraContrasena(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String generaEntornoAutenticacionWeb(String action, String captchaService) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String validacionAdicional(User usuario) {
        if( this.cert!=null ){
            try{
                this.cert.checkValidity(new Date());
            }
            catch(Exception ex){
                return "El Certificado ha expirado";
            }
        }
        
        return null;
    }
    
    public static KeyPair readKeyPair(File privateKey, char [] keyPassword) throws IOException {
        FileReader fileReader = new FileReader(privateKey);
        PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword));
        try {System.out.println(r.readPemObject());
            return (KeyPair) r.readObject();
        } catch (IOException ex) {
            throw new IOException("The private key could not be decrypted", ex);
        } finally {
            r.close();
            fileReader.close();
        }
    }

    @Override
    public List<User> buscaUsuarios(Map<String, Object> param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
class DefaultPasswordFinder implements PasswordFinder {
        private final char [] password;

        public DefaultPasswordFinder(char [] password) {
            this.password = password;
        }

        @Override
        public char[] getPassword() {
            return Arrays.copyOf(password, password.length);
        }
    } 