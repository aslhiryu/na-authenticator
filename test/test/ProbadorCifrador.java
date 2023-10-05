package test;

import neoAtlantis.utilidades.accessController.cipher.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class ProbadorCifrador {

    public ProbadorCifrador() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void pruebaMD5(){
        CipherMd5Des c=new CipherMd5Des("ssoPassHiryu");

        try{
            System.out.println("->"+c.cifra("system"));
            System.out.println("->"+c.cifra(""));
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void pruebaSha(){
        CipherSha1 c=new CipherSha1();

        try{
            System.out.println("->"+c.cifra("SECpresenta56"));
            System.out.println("->"+c.cifra(""));
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}