package test;

import neoAtlantis.utilidades.accessController.authentication.LoginCertificateAuthenticacion;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class ProbadorCert {
    
    public ProbadorCert() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void pruebaCertificado() throws Exception{
        LoginCertificateAuthenticacion lc=new LoginCertificateAuthenticacion(null);
        
        lc.autenticaUsuario(null);
    }
}
