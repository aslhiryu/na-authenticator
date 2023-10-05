package neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.interfaces.RecursoBloqueador;
import neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.interfaces.TipoBloqueo;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Bloqueador de que utiliza una base de datos para registrar bloqueos y conexiones de los usuarios.
 * <br><br>
 * La tabla de bloqueos en la BD debe contar con los siguientes campos:
 * <pre>
 * =====================================================
 * |   fecha  | usuario | terminal |  origen |   tipo  |
 * | DATETIME | VARCHAR |  VARCHAR | VARCHAR | VARCHAR |
 * =====================================================
 * </pre>
 * Y la tabla de conexiones debe contar con los siguientes campos:
 * <pre>
 * ================================
 * |   fecha  | usuario |  origen |
 * | DATETIME | VARCHAR | VARCHAR |
 * ================================
 * </pre>
 * Para facilitar la generaci&oacute;n de las tablas, se proporcionan los scripts '<i>generaTablaBloqueo.sql</i>' y '<i>generaTablaConexion.sql</i>' para la generaci&oacute;n de las mismas.
 * <br><br>
 * <h4>Configuraci&oacute;n del acceso a la BD</h4>
 * Para poder acceder a la BD se dispone de 2 medios, mediante un archivo xml o mediante un {@link java.util.Properties Properties}.
 * <br><br>
 * En caso de ser un archivo XML este debe de seguir cualquiera de las siguientes 2 estruturas:
 * <pre>
 * <b>&lt;mybdcliente&gt;</b>
 *     <b>&lt;DRIVER&gt;</b><i>driver_jdbc</i><b>&lt;/DRIVER&gt;</b>
 *     <b>&lt;vGCONNECCION&gt;</b><i>url_de_conexi&oacute;n</i><b>&lt;/vGCONNECCION&gt;</b>
 *     <b>&lt;vGUSUARIO&gt;</b><i>usuario_de_bd</i><b>&lt;/vGUSUARIO&gt;</b>
 *     <b>&lt;vGPASSWORD&gt;</b><i>contrase&ntilde;a_de_bd</i><b>&lt;/vGPASSWORD&gt;</b>
 * <b>&lt;/mybdcliente&gt;</b>
 *
 * <b>&lt;conexion&gt;</b>
 *     <b>&lt;driver&gt;</b><i>driver_jdbc</i><b>&lt;/driver&gt;</b>
 *     <b>&lt;url&gt;</b><i>url_de_conexi&oacute;n</i><b>&lt;/url&gt;</b>
 *     <b>&lt;user&gt;</b><i>usuario_de_bd</i><b>&lt;/user&gt;</b>
 *     <b>&lt;pass&gt;</b><i>contrase&ntilde;a_de_bd</i><b>&lt;/pass&gt;</b>
 * <b>&lt;/conexion&gt;</b>
 *
 * Y en caso de ser un archivo properties este debe de seguir la siguiente estrutura:
 * <b>driver</b> = driver jdbc
 * <b>url</b> = url de conexi&oacute;n
 * <b>user</b> = usuario de la bd
 * <b>pass</b> = contrase&ntilde;a de la bd
 * </pre>
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class BloqueoBD extends RecursoBloqueador {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";
    /**
     * Constante que define el nombre por defult para la tabla de bloqueos.
     */
    public static final String DEFAULT_BLOQUEOS="usuario_na_blqs";
    /**
     * Constante que define el nombre por defult para la tabla de conexiones.
     */
    public static final String DEFAULT_CONEXIONES="usuario_na_cnxs";
    
    private String tablaBloqueo = DEFAULT_BLOQUEOS;
    private String tablaConexion = DEFAULT_CONEXIONES;
    private Properties configBD;

    /**
     * Genera un Bloqueador por BD.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabConxs Nombre de la tabla de conexiones
     * @param tabBloqs Nombre de la tabla de bloqueos
     * @throws java.lang.Exception
     */
    public BloqueoBD(InputStream xml, String tabConxs, String tabBloqs) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(xml);
        Element e, raiz = doc.getRootElement();

        //valida el nombre de la tablas
        if( tabConxs==null || tabConxs.length()==0 ){
            throw new Exception("El nombre para la tabla de conexiones no es valido.");
        }
        if( tabBloqs==null || tabBloqs.length()==0 ){
            throw new Exception("El nombre para la tabla de bloqieos no es valido.");
        }
        this.tablaBloqueo=tabBloqs;
        this.tablaConexion=tabConxs;

        Properties p = new Properties();

        if (raiz != null && (raiz.getName().equalsIgnoreCase("mybdcliente") || raiz.getName().equalsIgnoreCase("conexion"))) {
            List hojas = raiz.getChildren();
            Iterator i = hojas.iterator();
            while (i.hasNext()) {
                e = (Element) i.next();
                if (e.getName().equalsIgnoreCase("DRIVER")) {
                    p.setProperty("driver", e.getValue());
                } else if (e.getName().equalsIgnoreCase("vGCONNECCION") || e.getName().equalsIgnoreCase("URL")) {
                    p.setProperty("url", e.getValue());
                } else if (e.getName().equalsIgnoreCase("vGUSUARIO") || e.getName().equalsIgnoreCase("USER")) {
                    p.setProperty("user", e.getValue());
                } else if (e.getName().equalsIgnoreCase("vGPASSWORD") || e.getName().equalsIgnoreCase("PASS")) {
                    p.setProperty("pass", e.getValue());
                }
            }
        }

        this.validaConfiguracionBD(p);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BloqueoBD(InputStream xml) throws Exception {
        this(xml, DEFAULT_CONEXIONES, DEFAULT_BLOQUEOS);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BloqueoBD(File xml) throws Exception {
        this(new FileInputStream(xml), DEFAULT_CONEXIONES, DEFAULT_BLOQUEOS);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabConxs Nombre de la tabla de conexiones
     * @param tabBloqs Nombre de la tabla de bloqueos
     * @throws java.lang.Exception
     */
    public BloqueoBD(File xml, String tabConxs, String tabBloqs) throws Exception {
        this(new FileInputStream(xml), tabConxs, tabBloqs);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BloqueoBD(String xml) throws Exception {
        this(new File(xml), DEFAULT_CONEXIONES, DEFAULT_BLOQUEOS);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabConxs Nombre de la tabla de conexiones
     * @param tabBloqs Nombre de la tabla de bloqueos
     * @throws java.lang.Exception
     */
    public BloqueoBD(String xml, String tabConxs, String tabBloqs) throws Exception {
        this(new File(xml), tabConxs, tabBloqs);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BloqueoBD(Properties configBD) throws Exception {
        this(configBD, DEFAULT_CONEXIONES, DEFAULT_BLOQUEOS);
    }

    /**
     * Genera un Bloqueador por BD.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @param tabConxs Nombre de la tabla de conexiones
     * @param tabBloqs Nombre de la tabla de bloqueos
     * @throws java.lang.Exception
     */
    public BloqueoBD(Properties configBD, String tabConxs, String tabBloqs) throws Exception {
        //valida el nombre de la tablas
        if( tabConxs==null || tabConxs.length()==0 ){
            throw new Exception("El nombre para la tabla de conexiones no es valido");
        }
        if( tabBloqs==null || tabBloqs.length()==0 ){
            throw new Exception("El nombre para la tabla de bloqieos no es valido");
        }
        this.tablaBloqueo=tabBloqs;
        this.tablaConexion=tabConxs;

        this.validaConfiguracionBD(configBD);
    }

    /**
     * Valida la correcta constitución de la configuración de conexión a BD.
     * @param p Properties con la configuración.
     * @throws java.lang.Exception
     */
    private void validaConfiguracionBD(Properties p) throws Exception {
        if (p == null) {
            throw new Exception("No existe configuración para la conexión.");
        }
        if (p.getProperty("url") == null || p.getProperty("url").length() == 0) {
            throw new Exception("Falta la URL para la conexión.");
        }
        if (p.getProperty("driver") == null || p.getProperty("driver").length() == 0) {
            throw new Exception("Falta el Driver para la conexión.");
        }
        if (p.getProperty("user") == null || p.getProperty("user").length() == 0) {
            throw new Exception("Falta el Usuario para la conexión.");
        }

        this.configBD = p;
    }

    private Connection generaConexion() throws Exception {
        try {
            this.mDebug.escribeDebug(this.getClass(), "Intenta generar conexión.");

            Class.forName(this.configBD.getProperty("driver"));
            return DriverManager.getConnection(this.configBD.getProperty("url"), this.configBD.getProperty("user"), this.configBD.getProperty("pass"));
        } catch (Exception ex) {
            if (this.mLog != null) {
                this.mLog.escribeLog(this.getClass(), "Error al generar la conexión.", ex);
            }
            throw ex;
        }
    }

    /**
     * Contruye la tabla de bloqueos en la BD configurada.
     * @return true is la logro generar
     * @throws java.lang.Exception
     */
    public boolean generaTablaBloqueos() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("generaTablaBloqueo.sql");

        if (in != null) {
            StringBuffer sb = new StringBuffer("");
            int c;

            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

            Connection con = this.generaConexion();
            Statement st = con.createStatement();
            //System.out.println(sb.toString());
            st.execute(sb.toString());
            st.close();
            con.close();

            return true;
        }


        return false;
    }

    /**
     * Contruye la tabla de conexiones en la BD configurada.
     * @return true si la logro generar
     * @throws java.lang.Exception
     */
    public boolean generaTablaConexion() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("generaTablaConexion.sql");

        if (in != null) {
            StringBuffer sb = new StringBuffer("");
            int c;

            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

            Connection con = this.generaConexion();
            Statement st = con.createStatement();
            //System.out.println(sb.toString());
            st.execute(sb.toString());
            st.close();
            con.close();

            return true;
        }


        return false;
    }

    /**
     * Genera el bloqueo de un usuario en la BD.
     * @param user Usuario a bloquear
     * @throws java.lang.Exception
     */
    public void agregaBloqueo(Usuario user) throws Exception {
        try {
            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement("INSERT INTO " + this.tablaBloqueo + " VALUES(?, ?, ?, ?, ?)");
            ps.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(2, user.getUser());
            ps.setString(3, user.getUser() + ":" + user.getTerminal());
            ps.setString(4, user.getOrigen());
            ps.setString(5, this.modoBloqueo.toString());

            ps.execute();

            ps.close();
            con.close();
        } catch (SQLException ex) {
            throw new Exception("Error al agregar bloqueo, " + ex.getMessage());
        }
    }

    /**
     * Remueve de la BD el bloqueo de un usuario
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     * @throws java.lang.Exception
     */
    public boolean remueveBloqueo(Usuario user) throws Exception {
        boolean b=false;

        try {
            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + this.tablaBloqueo + " WHERE " +
                    (this.modoBloqueo == TipoBloqueo.USUARIO ? "usuario=?" : (this.modoBloqueo == TipoBloqueo.IP ? "origen=?" : "terminal=?")));

            if (this.modoBloqueo == TipoBloqueo.USUARIO) {
                ps.setString(1, user.getUser());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getUser() + ").");
            } else if (this.modoBloqueo == TipoBloqueo.IP) {
                ps.setString(1, user.getOrigen());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getOrigen() + ").");
            } else {
                ps.setString(1, user.getUser() + ":" + user.getTerminal());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getUser() + ":" + user.getTerminal() + ").");
            }

            ps.execute();

            ps.close();
            con.close();
            b=true;
        } catch (Exception ex) {
            throw new Exception("Error al remover bloqueo: " + ex.getMessage());
        }

        return b;
    }

    /**
     * Revisa en la BD y finaliza los bloqueos que hayan concluido.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     * @throws java.lang.Exception
     */
    public ArrayList<String> revisaBloqueosTerminados() throws Exception {
        ArrayList objs = new ArrayList();
        long fecha;
        String obj, ori;

        try {
            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + this.tablaBloqueo + " WHERE usuario=? OR origen=?");
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery("SELECT fecha, usuario, origen FROM " + this.tablaBloqueo + " ORDER BY fecha DESC");

            while (res.next()) {
                fecha = res.getTimestamp("fecha").getTime();
                obj = res.getString("usuario");
                ori = res.getString("origen");
                if (fecha + this.tiempoBloqueo <= (new java.util.Date()).getTime()) {
                    ps.setString(1, obj);
                    ps.setString(2, ori);
                    ps.execute();
                    this.mDebug.escribeDebug(this.getClass(), "Remueve el objeto '" + obj + "' bloqueado.");
                    objs.add(obj);
                }

            }
            ps.close();
            res.close();

            //revisa conexiones
            res = st.executeQuery("SELECT fecha, usuario FROM " + this.tablaConexion);
            ps = con.prepareStatement("DELETE FROM " + this.tablaConexion + " WHERE usuario=?");
            while (res.next()) {
                fecha = res.getTimestamp("fecha").getTime();
                obj = res.getString("usuario");
                if (fecha + tiempoBloqueo <= (new java.util.Date()).getTime()) {
                    ps.setString(1, obj);
                    ps.execute();
                    this.mDebug.escribeDebug(this.getClass(), "Remueve la conexión colgada (" + obj + ").");
                }
            }

            res.close();
            st.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            throw new Exception("Error al revisar bloqueos: " + ex.getMessage());
        }

        return objs;
    }

    /**
     * Revisa en la BD si un usuario tiene un bloqueo activo.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     * @throws java.lang.Exception
     */
    public boolean verificaBloqueo(Usuario user) throws Exception {
        boolean res = false;

        try {
            Connection con = this.generaConexion();
            Statement st = con.createStatement();
            ResultSet rTmp = st.executeQuery("SELECT * FROM " + this.tablaBloqueo + " WHERE " +
                    (this.modoBloqueo == TipoBloqueo.USUARIO ? "usuario='" + user.getUser() + "'" : "") +
                    (this.modoBloqueo == TipoBloqueo.IP ? "origen='" + user.getOrigen() + "'" : "") +
                    (this.modoBloqueo == TipoBloqueo.TERMINAL ? "terminal='" + user.getUser() + ":" + user.getTerminal() + "'" : "")+
                    " ORDER BY fecha DESC");

            if (rTmp.next()) {
                res = true;
            }

            this.mDebug.escribeDebug(this.getClass(), "Revisa el bloqueo de '" + (this.modoBloqueo == TipoBloqueo.USUARIO ? user.getUser() : (this.modoBloqueo == TipoBloqueo.IP ? user.getOrigen() : user.getUser() + ":" + user.getTerminal())) + "' con resultado '" + res + "'.");
            rTmp.close();
            st.close();
            con.close();
        } catch (Exception ex) {
            throw new Exception("Error al revisar bloqueo usuario: " + ex.getMessage());
        }

        return res;
    }

    /**
     * Agrega en la BD la conexi&oacute;n de un usuario
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     * @throws java.lang.Exception
     */
    public void agregaConexion(Usuario user) throws Exception {
        try {
            Connection con = this.generaConexion();
            Statement st = con.createStatement();
            //valida la existencia de la conexión
            ResultSet rTmp = st.executeQuery("SELECT * FROM " + this.tablaConexion + " WHERE usuario='" + user.getUser() + "'");
            PreparedStatement ps;

            //si ya existo solo actualizo
            if (rTmp.next()) {
                ps = con.prepareStatement("UPDATE " + this.tablaConexion + " SET fecha=?");
                this.mDebug.escribeDebug(this.getClass(), "Actualizo la conexión de (" + user.getUser() + ").");
            } //si no existe la creo
            else {
                ps = con.prepareStatement("INSERT INTO " + this.tablaConexion + " VALUES(?, ?, ?)");
                ps.setString(2, user.getUser());
                ps.setString(3, user.getOrigen());
                this.mDebug.escribeDebug(this.getClass(), "Agrega la conexión de (" + user.getUser() + ").");
            }
            ps.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));

            ps.execute();

            rTmp.close();
            st.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            throw new Exception("Error al agregar conexion: " + ex.getMessage());
        }
    }

    /**
     * Remueve de la BD la conexi&oacute;n de un usuario
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     * @throws java.lang.Exception
     */
    public boolean remueveConexion(Usuario user) throws Exception {
        boolean b=false;

        try {
            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + this.tablaConexion + " WHERE " +
                    (this.modoBloqueo == TipoBloqueo.USUARIO ? "usuario=?" : (this.modoBloqueo == TipoBloqueo.IP ? "origen=?" : "terminal=?")));

            if (this.modoBloqueo == TipoBloqueo.USUARIO) {
                ps.setString(1, user.getUser());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getUser() + ").");
            } else if (this.modoBloqueo == TipoBloqueo.IP) {
                ps.setString(1, user.getOrigen());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getOrigen() + ").");
            } else {
                ps.setString(1, user.getUser() + ":" + user.getTerminal());
                this.mDebug.escribeDebug(this.getClass(), "Remuevo la conexión de (" + user.getUser() + ":" + user.getTerminal() + ").");
            }

            ps.execute();

            ps.close();
            con.close();
            b=true;
        } catch (Exception ex) {
            throw new Exception("Error al remover conexion: " + ex.getMessage());
        }

        return b;
    }

    /**
     * Revisa en la BD si un usuario tiene una conexi&oacute;n activa.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     * @throws java.lang.Exception
     */
    public boolean verificaConexion(Usuario user) throws Exception {
        boolean existe = false;

        try {
            Connection con = this.generaConexion();
            Statement st = con.createStatement();
            //valida la existencia de la conexión
            ResultSet rTmp = st.executeQuery("SELECT * FROM " + this.tablaConexion + " WHERE " +
                    (this.modoBloqueo == TipoBloqueo.USUARIO||this.modoBloqueo == TipoBloqueo.TERMINAL ? "usuario='" + user.getUser() + "'" : "origen='" + user.getOrigen() + "'"));


            if (rTmp.next()) {
                existe = true;
            }

            this.mDebug.escribeDebug(this.getClass(), "Revisa la conexion de '" + (this.modoBloqueo == TipoBloqueo.USUARIO|this.modoBloqueo == TipoBloqueo.TERMINAL ? user.getUser() : user.getOrigen())+ "' con resultado '" + existe + "'.");
            rTmp.close();
            st.close();
            con.close();
        } catch (Exception ex) {
            throw new Exception("Error al verificar conexion: " + ex.getMessage());
        }

        return existe;
    }
}
