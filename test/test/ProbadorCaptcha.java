package test;

import neoAtlantis.utilidades.accessController.captcha.BasicConfirmationCode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class ProbadorCaptcha {

    public ProbadorCaptcha() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void codigo(){
        BasicConfirmationCode c=new BasicConfirmationCode();

        System.out.println(c.genera());
    }
}