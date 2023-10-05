/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.cipher.CifradorMd5Des;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.AutenticadorArchivo;
import neoAtlantis.utilidades.debuger.ConsoleDebuger;
import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.ConsoleLogger;
import neoAtlantis.utilidades.logger.interfaces.Logger;
import org.junit.Test;

/**
 *
 * @author economia
 */
public class ProbadorAutenticadorFile {
    public static Debuger generaDebuger(){
        return new ConsoleDebuger("TEST");
    }

    public static Logger generaLoger(){
        return new ConsoleLogger("TEST");
    }

    public static Usuario generaUsuario(){
        Usuario u=new Usuario("hiryu");

        u.setNombre("Alberto");

        return u;
    }

    public static CifradorMd5Des generaCifrador(){
        return new CifradorMd5Des("bluemary");
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}


    @Test
    public void pruebaGenCuenta() throws Exception{
        AutenticadorArchivo f=new AutenticadorArchivo("c:/tmp/passwd.txt");
        f.setMDebug( generaDebuger() );
        f.setMLog( generaLoger() );

        System.out.println("+ "+f.agregaCuenta(generaUsuario(), "mercury"));
    }

    @Test
    public void pruebaModCuenta() throws Exception{
        AutenticadorArchivo f=new AutenticadorArchivo("c:/tmp/passwd.txt");
        f.setMDebug( generaDebuger() );
        f.setMLog( generaLoger() );

        System.out.println("+ "+f.modificaContrasena(generaUsuario(), "pass1"));
    }

    @Test
    public void pruebaValCuenta() throws Exception{
        AutenticadorArchivo f=new AutenticadorArchivo("c:/tmp/passwd.txt");
        f.setMDebug( generaDebuger() );
        f.setMLog( generaLoger() );

        System.out.println("+Val: "+f.validaUsuario(generaUsuario().getUser(), "pas"));
    }
}