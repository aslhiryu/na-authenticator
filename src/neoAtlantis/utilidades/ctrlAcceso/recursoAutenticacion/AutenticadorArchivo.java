package neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion;

import java.io.*;
import java.util.ArrayList;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.RecusoAutenticador;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.ResultadoAutenticacion;

/**
 * Autenticador que utiliza un archivo de texto como medio para validar los usuarios.
 * <br><br>
 * El archivo de texto sigue una estructura parecida al de los passwd de Unix:
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<i>&lt;user&gt;:&lt;pass&gt;:&lt;nombre&gt;</i>
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class AutenticadorArchivo extends RecusoAutenticador {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";

    private File archivo;

    /**
     * Genera un Autenticador por Archivo de Texto.
     * @param archivo Archivo de texto
     * @throws java.lang.Exception
     */
    public AutenticadorArchivo(File archivo) throws Exception {
        this.archivo=archivo;

        if( archivo==null || !archivo.canRead() ){
            throw new Exception("No se puede acceder al archivo de cuentas"+(archivo!=null? " '"+archivo.getAbsolutePath()+"'": "")+".");
        }
    }

    /**
     * Genera un Autenticador por Archivo de Texto.
     * @param archivo Ruta completa del archivo de texto
     * @throws java.lang.Exception
     */
    public AutenticadorArchivo(String archivo) throws Exception {
        this(new File(archivo));
    }

    private String[][] parseaCuentas() throws Exception {
        ArrayList al=new ArrayList();
        FileInputStream fis=new FileInputStream(this.archivo);
        StringBuffer sb = new StringBuffer("");
        int c;

        //leo los datos
        while ((c = fis.read()) != -1) {
            if ( (c == '\n') && sb.toString().trim().length()>0 ) {
                this.mDebug.escribeDebug(this.getClass(), "Linea -> "+sb.toString());
                al.add( sb.toString().replaceAll("\n", "").replaceAll("\r", "").split(":") );
                sb = new StringBuffer("");
            } else {
                sb.append((char) c);
            }
        }
        if( sb.toString().length()>0 ){
            al.add( sb.toString().replaceAll("\n", "").replaceAll("\r", "").split(":") );
        }

        fis.close();

        String[][] d=new String[al.size()][];
        for(int i=0; i<al.size(); i++){
            d[i]=(String[])al.get(i);
        }
        this.mDebug.escribeDebug(this.getClass(), "Obtiene "+al.size()+" cuentas.");

        return d;
    }

    /**
     * Valida un usuario mediante el archivo de texto.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return Evento resultado de la validaci&oacute;n:
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#ACCESO_AUTORIZADO ACCESO_AUTORIZADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#ACCESO_AUTORIZADO_CADUCADO ACCESO_AUTORIZADO_CADUCADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#ACCESO_AUTORIZADO_TEMPORAL ACCESO_AUTORIZADO_TEMPORAL},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#ACCESO_DENEGADO ACCESO_DENEGADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#ACCESO_FUERA_DE_TIEMPO ACCESO_FUERA_DE_TIEMPO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#USUARIO_BLOQUEADO USUARIO_BLOQUEADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#USUARIO_CONECTADO USUARIO_CONECTADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#USUARIO_NO_ACTIVO USUARIO_NO_ACTIVO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#USUARIO_NO_ENCONTRADO USUARIO_NO_ENCONTRADO},
     * {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador#LIMITE_REVASADO LIMITE_REVASADO}
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaUsuario(String user, String pass) throws Exception {
        ResultadoAutenticacion val = ResultadoAutenticacion.USUARIO_NO_ENCONTRADO;
        String[][] data=this.parseaCuentas();

        for(int i=0; data!=null&&i<data.length; i++){
            this.mDebug.escribeDebug(this.getClass(), "Compara el usuario '"+user+"' con '"+data[i][0]+"'.");
            if( data[i][0].equalsIgnoreCase(user) ){
                if( pass.equals( (this.cifrador != null ? this.cifrador.cifra(data[i][1]) : data[i][1]) ) ){
                    val = ResultadoAutenticacion.ACCESO_AUTORIZADO;
                    break;
                }
                else{
                    val = ResultadoAutenticacion.ACCESO_DENEGADO;
                    break;
                }
            }
        }

        return val;
    }

    /**
     * Agrega una cuenta al archivo de texto.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public boolean agregaCuenta(Usuario user, String pass) throws Exception {
        String[][] data=this.parseaCuentas();

        //busco si existe el usuario
        for(int i=0; data!=null&&i<data.length; i++){
            //System.out.println("Compara '"+data[i][0]+"' con '"+user.getUser()+"'");
            if( data[i][0].equalsIgnoreCase(user.getUser()) ){
                return false;
            }
        }

        //agrego el usuario
        FileInputStream fis=new FileInputStream(this.archivo);
        int c;
        boolean finlinea=false;
        if( this.archivo.length()>0 ){
            fis.skip(this.archivo.length()-1);
        }
        while ((c = fis.read()) != -1) {
            finlinea=false;
            //System.out.println("-> "+c);

            if( c=='\n' ){
                finlinea=true;
            }
        }
        fis.close();

        FileOutputStream out=new FileOutputStream(this.archivo, true);
        StringBuffer sb=new StringBuffer("");
        sb.append(user.getUser()).append(":").append((this.cifrador != null ? this.cifrador.cifra(pass) : pass)).append(":").append(user.getNombre());
        if(!finlinea){
            out.write(System.getProperty("line.separator").getBytes());
        }
        out.write(sb.toString().getBytes());
        out.close();

        return true;
    }

    /**
     * Agrega una cuenta al archivo de texto, con la contrase&ntilde;a temporal configurada.
     * @param user Nickname del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public boolean agregaCuentaTemporal(Usuario user) throws Exception {
        return agregaCuenta(user, "");
    }

    /**
     * Modifica la contrase&ntilde;a de un usuario en el archivo de texto.
     * @param user Nickname del usuario
     * @param pass Nueva contraseña para el usuario contrase&ntilde;a
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    public boolean modificaContrasena(Usuario user, String pass) throws Exception {
        String[][] data=this.parseaCuentas();
        FileOutputStream fos=new FileOutputStream(this.archivo, false);

        //busco al usuario
        for(int i=0; data!=null&&i<data.length; i++){
            //System.out.println("Compara '"+data[i][0]+"' con '"+user.getUser()+"'");
            if( data[i][0].equalsIgnoreCase(user.getUser()) ){
                data[i][1]=(this.cifrador != null ? this.cifrador.cifra(pass) : pass);
            }
            for(int j=0; j<data[i].length; j++){
                if(j>0){
                    fos.write(':');
                }
                fos.write(data[i][j].getBytes());
            }
            if(i<data.length-1){
                fos.write(System.getProperty("line.separator").getBytes());
            }
        }
        fos.close();

        return true;
    }

    /**
     * Obtiene el valor de un elemento mapeado en el archivo de texto. &lt;user&gt;, &lt;nombre&gt;.
     * @param user Nickname del usuario
     * @param elemento Nombre del elemento deseado
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElemento(String user, String elemento) throws Exception {
        String[][] data=this.parseaCuentas();

        //busco al usuario
        for(int i=0; data!=null&&i<data.length; i++){
            if( data[i][0].equalsIgnoreCase(user) ){
                if( elemento.equalsIgnoreCase("name") && data[i].length>=3 ){
                    return data[i][2];
                }
                else if( elemento.equalsIgnoreCase("id") ){
                    return data[i][0];
                }
            }
        }

        return null;
    }

    /**
     * Obtiene el valor del elemento 'name' mapeado en el archivo de texto.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElementoNombre(String user) throws Exception {
        return this.obtieneElemento(user, "name");
    }

    /**
     * Obtiene el valor del elemento 'id' mapeado en el archivo de texto.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElementoId(String user) throws Exception {
        return this.obtieneElemento(user, "id");
    }

}
