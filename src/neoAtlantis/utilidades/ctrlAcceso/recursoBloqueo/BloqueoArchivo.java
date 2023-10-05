package neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.interfaces.RecursoBloqueador;

/**
 * Bloqueador de que utiliza un archivo de texto para registrar los bloqueos y conexiones de los usuarios.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class BloqueoArchivo extends RecursoBloqueador {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";

    private File fBloqueos;
    private File fConexiones;
    private SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera un Bloqueador por archivo de texto.
     * @param bloqueos Ruta completa del archivo de texto donde se registran los bloqueos
     * @param conexiones Ruta completa del archivo de texto donde se registran los usuarios conectados
     * @throws java.lang.Exception
     */
    public BloqueoArchivo(String bloqueos, String conexiones) throws Exception {
        this(new File(bloqueos), new File(conexiones));
    }

    /**
     * Genera un Bloqueador por archivo de texto.
     * @param bloqueos Archivo de texto donde se registran los bloqueos
     * @param conexiones Archivo de texto donde se registran los usuarios conectados
     * @throws java.lang.Exception
     */
    public BloqueoArchivo(File bloqueos, File conexiones) throws Exception {
        this.fBloqueos = bloqueos;
        this.fConexiones=conexiones;

        File fp = this.fBloqueos.getParentFile();
        if( !this.fBloqueos.exists() ){
            fp.mkdirs();
            this.fBloqueos.createNewFile();
        }

        fp = this.fConexiones.getParentFile();
        if( !this.fConexiones.exists() ){
            fp.mkdirs();
            this.fConexiones.createNewFile();
        }
    }

    private String[][] parseaBloqueos() throws Exception {
        ArrayList al=new ArrayList();
        FileInputStream fis=new FileInputStream(this.fBloqueos);
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
        this.mDebug.escribeDebug(this.getClass(), "Obtiene "+al.size()+" bloqueos.");

        return d;
    }

    private String[][] parseaConexiones() throws Exception {
        ArrayList al=new ArrayList();
        FileInputStream fis=new FileInputStream(this.fConexiones);
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
        this.mDebug.escribeDebug(this.getClass(), "Obtiene "+al.size()+" conexiones.");

        return d;
    }

    private String[][] agregaBloq(String[][] ori, String[] nue){
        String[][] tmp;

        if( ori==null ){
            tmp=new String[1][];
        }
        else{
            tmp=new String[ori.length+1][];
        }

        for(int i=0; ori!=null&&i<ori.length; i++){
            tmp[i]=ori[i];
        }
        tmp[tmp.length-1]=nue;

        return tmp;
    }

    private synchronized void almacenaBloqueos(String[][] bloqs) throws Exception{
        FileOutputStream fos=new FileOutputStream(this.fBloqueos, false);

        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            for(int j=0; j<bloqs[i].length; j++){
                if(j>0){
                    fos.write(':');
                }
                fos.write(bloqs[i][j].getBytes());
            }
            if(i<bloqs.length-1){
                fos.write(System.getProperty("line.separator").getBytes());
            }
        }

        fos.close();
    }

    private synchronized void almacenaConexiones(String[][] bloqs) throws Exception{
        FileOutputStream fos=new FileOutputStream(this.fConexiones, false);

        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            for(int j=0; j<bloqs[i].length; j++){
                if(j>0){
                    fos.write(':');
                }
                fos.write(bloqs[i][j].getBytes());
            }
            if(i<bloqs.length-1){
                fos.write(System.getProperty("line.separator").getBytes());
            }
        }

        fos.close();
    }

    private String[][] limpiaBloqs(String[][] b){
        ArrayList<String[]> al=new ArrayList<String[]>();

        for(int i=0; b!=null&&i<b.length; i++){
            if( !b[i][0].equals("||> MARK <||") ){
                al.add(b[i]);
            }
        }

        String[][] tmp=new String[al.size()][];
        for(int i=0; i<al.size(); i++){
            tmp[i]=al.get(i);
        }

        return tmp;
    }

    /**
     * Genera el bloqueo de un usuario en el archivo de texto.
     * @param user Usuario a bloquear
     * @throws java.lang.Exception
     */
    public void agregaBloqueo(Usuario user) throws Exception {
        String[][]bloqs=this.parseaBloqueos();
        boolean existe=false;

        //revisa si existe el bloqueo
        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            if( bloqs[i][0].equalsIgnoreCase(user.getUser()) ){
                bloqs[i][1]=this.sdf.format(new Date());
                existe=true;
                this.mDebug.escribeDebug(this.getClass(), "Actualizo el bloqueo para '"+user.getUser()+"'.");
                break;
            }
        }

        if(!existe){
            this.mDebug.escribeDebug(this.getClass(), "Genero el bloqueo para '"+user.getUser()+"'.");
            String[] c=new String[5];
            c[0]=user.getUser();
            c[1]=this.sdf.format(new Date());
            c[2]=user.getUser() + ":" + user.getTerminal();
            c[3]=user.getOrigen();
            c[4]=""+this.modoBloqueo;
            bloqs=this.agregaBloq(bloqs, c);
        }

        this.almacenaBloqueos(bloqs);
    }

    /**
     * Revisa en el archivo de texto y finaliza los bloqueos que hayan concluido.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     * @throws java.lang.Exception
     */
    public ArrayList<String> revisaBloqueosTerminados() throws Exception {
        String[][]bloqs=this.parseaBloqueos();
        ArrayList terminados=new ArrayList();
        Date d;

        //revisa los bloqueos
        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            d=this.sdf.parse(bloqs[i][1]);
            //reviso la caducidad
            if( ((new Date()).getTime()-d.getTime())>this.tiempoBloqueo ){
                bloqs[i][0]="||> MARK <||";
            }
        }
        bloqs=this.limpiaBloqs(bloqs);

        this.almacenaBloqueos(bloqs);
        return terminados;
    }

    /**
     * Revisa en el archivo de texto si un usuario tiene un bloqueo activo.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     * @throws java.lang.Exception
     */
    public boolean verificaBloqueo(Usuario user) throws Exception {
        String[][]bloqs=this.parseaBloqueos();

        //revisa los bloqueos
        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            if( bloqs[i][0].equalsIgnoreCase(user.getUser()) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Remueve del archivo de texto el bloqueo de un usuario
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     * @throws java.lang.Exception
     */
    public boolean remueveBloqueo(Usuario user) throws Exception {
        String[][]bloqs=this.parseaBloqueos();
        boolean enc=false;

        //revisa los bloqueos
        for(int i=0; bloqs!=null&&i<bloqs.length; i++){
            if( bloqs[i][0].equalsIgnoreCase(user.getUser()) ){
                bloqs[i][0]="||> MARK <||";
                enc=true;
            }
        }

        if(enc){
            bloqs=this.limpiaBloqs(bloqs);
        }

        this.almacenaBloqueos(bloqs);
        return enc;
    }

    /**
     * Agrega al archivo de texto la conexi&oacute;n de un usuario
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     * @throws java.lang.Exception
     */
    public void agregaConexion(Usuario user) throws Exception {
        String[][]conxs=this.parseaConexiones();
        boolean existe=false;

        //revisa si existe el bloqueo
        for(int i=0; conxs!=null&&i<conxs.length; i++){
            if( conxs[i][0].equalsIgnoreCase(user.getUser()) ){
                conxs[i][1]=this.sdf.format(new Date());
                existe=true;
                this.mDebug.escribeDebug(this.getClass(), "Actualizo la conexión para '"+user.getUser()+"'.");
                break;
            }
        }

        if(!existe){
            this.mDebug.escribeDebug(this.getClass(), "Genero la conexión para '"+user.getUser()+"'.");
            String[] c=new String[3];
            c[0]=user.getUser();
            c[1]=this.sdf.format(new Date());
            c[2]=user.getOrigen();
            conxs=this.agregaBloq(conxs, c);
        }

        this.almacenaConexiones(conxs);
    }

    /**
     * Remueve del archivo de texto la conexi&oacute;n de un usuario
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     * @throws java.lang.Exception
     */
    public boolean remueveConexion(Usuario user) throws Exception {
        String[][]conxs=this.parseaConexiones();
        boolean enc=false;

        //revisa los bloqueos
        for(int i=0; conxs!=null&&i<conxs.length; i++){
            if( conxs[i][0].equalsIgnoreCase(user.getUser()) ){
                conxs[i][0]="||> MARK <||";
                enc=true;
            }
        }

        if(enc){
            conxs=this.limpiaBloqs(conxs);
        }

        this.almacenaConexiones(conxs);
        return enc;
    }

    /**
     * Revisa en el archivo de texto si un usuario tiene una conexi&oacute;n activa.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     * @throws java.lang.Exception
     */
    public boolean verificaConexion(Usuario user) throws Exception {
        String[][]conxs=this.parseaConexiones();

        //revisa los bloqueos
        for(int i=0; conxs!=null&&i<conxs.length; i++){
            if( conxs[i][0].equalsIgnoreCase(user.getUser()) ){
                return true;
            }
        }

        return false;
    }

}
