package neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.RecusoAutenticador;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.ResultadoAutenticacion;
import neoAtlantis.utilidades.logger.interfaces.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Autenticador que utiliza base de datos como medio para validar los usuarios.
 * <br><br>
 * La tabla en la BD debe contar con almenos los campos:
 * <pre>
 * =====================
 * |   user  |  pass   |
 * | VARCHAR | VARCHAR |
 * =====================
 * </pre>
 * Lo minimo recomendado es:
 * <pre>
 * ===================================================
 * |   id    |   user  |  pass   |  name   |  active |
 * | VARCHAR | VARCHAR | VARCHAR | VARCHAR | NUMERIC |
 * ===================================================
 * </pre>
 * Y lo recomendado es:
 * <pre>
 * =================================================================================
 * |   id    |   user  |  pass   |  name   |  active |  access  | expire |  type   |
 * | VARCHAR | VARCHAR | VARCHAR | VARCHAR | NUMERIC | DATETIME |  DATE  | VARCHAR |
 * =================================================================================
 * </pre>
 * Para facilitar la generaci&oacute;n de la tabla, se proporciona el script '<i>generaTablaUsuario.sql</i>' para la generaci&oacute;n de la misma.
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
 * <h4>Configuraci&oacute;n del mapeo de campos</h4>
 * Para la configurac&oacute;n de los campos que tede de utilizar el autenticador se utiliza un {@link java.util.Properties Properties},
 * en el cual se debe de definir de que tabla y campos obtendra la informaci&oacute;n el autenticador. La estructura que se bede de seguir es la siguiente:
 * <pre>
 * <b>table</b> = tabla_con_los_usuarios
 * <b>id</b> = campo_id_de_usuario (en caso de no definir se tomara el campo asignado a <i>user</i>)
 * <b>name</b> = campo_nombre_de_usuario (en caso de no definir se tomara el campo asignado a <i>user</i>)
 * <b>user</b> = campo_usuario (obligatorio)
 * <b>pass</b> = campo_contrase&ntilde;a (obligatorio)
 * <b>active</b> = campo_usuario_activo (este debe de ser numerico, donde 0 indica 'inactivo' y 1 'activo')
 * <b>expire</b> = campo_fecha_expiraci&oacute;n (este debe de ser de tipo DATE)
 * <b>access</b> = campo_ultimo_acceso (este debe de ser de tipo DATETIME)
 * <b>type</b> = campo_tipo_temporal
 * </pre>
 * Esta configurac&oacute;n puede incluir el mapeo de mas campos, y estos pueden ser utilizados por el metodo <i>getElemento</i>.
 *
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class AutenticadorBD extends RecusoAutenticador {

    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION = "1.0";
    /**
     * Constante que define el nombre por default para la tabla de usuarios.
     */
    public static final String DEFAULT_USUARIOS = "usuario_na_data";
    private Properties configBD;
    private Properties configTabla;
    private String tabla = DEFAULT_USUARIOS;
    private boolean nativa = true;
    private StringBuffer sql;

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param configCampos Mapeo de los campos requeridos con respecto a los campos en la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(InputStream xml, Properties configCampos) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(xml);
        Element e, raiz = doc.getRootElement();

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
        this.validaConfiguracionTablas(configCampos);
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabla Nombre de la tabla de usuarios
     * @throws java.lang.Exception
     */
    public AutenticadorBD(InputStream xml, String tabla) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(xml);
        Element e, raiz = doc.getRootElement();


        //valida el nombre de la tabla
        if (tabla == null || tabla.length() == 0) {
            throw new Exception("El nombre para la tabla de usuarios no es valido.");
        }
        this.tabla = tabla;

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
        this.validaConfiguracionTablas(null);
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(InputStream xml) throws Exception {
        this(xml, new Properties());
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param configCampos Mapeo de los campos requeridos con respecto a los campos en la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(File xml, Properties configCampos) throws Exception {
        this(new FileInputStream(xml), configCampos);
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabla Nombre de la tabla de usuarios
     * @throws java.lang.Exception
     */
    public AutenticadorBD(File xml, String tabla) throws Exception {
        this(new FileInputStream(xml), tabla);
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(File xml) throws Exception {
        this(xml, new Properties());
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param configCampos Mapeo de los campos requeridos con respecto a los campos en la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(String xml, Properties configCampos) throws Exception {
        this(new File(xml), configCampos);
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @param tabla Nombre de la tabla de usuarios
     * @throws java.lang.Exception
     */
    public AutenticadorBD(String xml, String tabla) throws Exception {
        this(new File(xml), tabla);
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(String xml) throws Exception {
        this(xml, new Properties());
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @param configCampos Mapeo de los campos requeridos con respecto a los campos en la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(Properties configBD, Properties configCampos) throws Exception {
        this.validaConfiguracionBD(configBD);
        this.validaConfiguracionTablas(configCampos);
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public AutenticadorBD(Properties configBD, String tabla) throws Exception {
        this.tabla = tabla;

        this.validaConfiguracionBD(configBD);
        this.validaConfiguracionTablas(null);
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

    private void validaConfiguracionTablas(Properties p) throws Exception {
        if (p == null || p.isEmpty()) {
            this.configTabla = new Properties();
            this.configTabla.setProperty("table", this.tabla);
            this.configTabla.setProperty("id", "id_usuario");
            this.configTabla.setProperty("name", "nombre");
            this.configTabla.setProperty("user", "login");
            this.configTabla.setProperty("pass", "pass");
            this.configTabla.setProperty("active", "activo");
            this.configTabla.setProperty("expire", "expira");
            this.configTabla.setProperty("access", "acceso");
            this.configTabla.setProperty("type", "tipo");

            this.nativa = true;
        } else {
            //para la tabla
            if (p.getProperty("table") == null || p.getProperty("table").length() == 0) {
                throw new Exception("No se cuenta con la tabla de usuarios.");
            }
            //para el login
            if (p.getProperty("user") == null || p.getProperty("user").length() == 0) {
                throw new Exception("No se cuenta con el campo de usuario.");
            }
            //para el pass
            if (p.getProperty("pass") == null || p.getProperty("pass").length() == 0) {
                throw new Exception("No se cuenta con el campo de contraseña.");
            }
            //para el id
            if (p.getProperty("id") == null || p.getProperty("id").length() == 0) {
                p.setProperty("id", p.getProperty("user"));
            }
            //para el nombre
            if (p.getProperty("name") == null || p.getProperty("name").length() == 0) {
                p.setProperty("name", p.getProperty("user"));
            }

            this.configTabla = p;
            this.nativa = false;
        }
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
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean generaTablaUsuario() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("generaTablaUsuario.sql");

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
     * Valida un usuario mediante la BD.
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

        try {
            this.sql = new StringBuffer("SELECT ").append(this.configTabla.getProperty("pass")).append((this.configTabla.getProperty("expire") != null ? ", " + this.configTabla.getProperty("expire") : "")).append((this.configTabla.getProperty("active") != null ? ", " + this.configTabla.getProperty("active") : "")).append((this.configTabla.getProperty("type") != null ? ", " + this.configTabla.getProperty("type") : "")).append(" FROM ").append(this.configTabla.getProperty("table")).append(" WHERE ").append(this.configTabla.getProperty("user")).append("=?");
            this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user);

            ResultSet res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                val = ResultadoAutenticacion.ACCESO_AUTORIZADO;
                this.mDebug.escribeDebug(this.getClass(), "Intenta la validacion de '" + user + "'.");
//System.out.println("---> "+pass+", "+res.getString(this.configTabla.getProperty("pass"))+", "+(this.cifrador != null ? this.cifrador.cifra(res.getString(this.configTabla.getProperty("pass"))) : res.getString(this.configTabla.getProperty("pass"))));
                //valida si la cuenta es temporal
                if (this.configTabla.getProperty("type") != null &&
                        res.getString(this.configTabla.getProperty("type")) != null &&
                        res.getString(this.configTabla.getProperty("type")).equalsIgnoreCase("T")) {
                    val = ResultadoAutenticacion.ACCESO_AUTORIZADO_TEMPORAL;
                    modificaFechaAcceso(user);
                } //valida la contraseña
                else if (res.getString(this.configTabla.getProperty("pass")) == null ||
                        !res.getString(this.configTabla.getProperty("pass")).equals((this.cifrador != null ? this.cifrador.cifra(pass) : pass))) {
                    val = ResultadoAutenticacion.ACCESO_DENEGADO;
                } //valida si esta activo
                else if (this.configTabla.getProperty("active") != null &&
                        res.getString(this.configTabla.getProperty("active")) != null &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("1") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("Y") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("YES") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("S") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("SI") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("T") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("TRUE") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("A") &&
                        !res.getString(this.configTabla.getProperty("active")).equalsIgnoreCase("ACTIVO")) {
                    val = ResultadoAutenticacion.USUARIO_NO_ACTIVO;
                } //valida si esta vigente la cuenta
                else if (this.configTabla.getProperty("expire") != null &&
                        res.getTimestamp(this.configTabla.getProperty("expire")) != null &&
                        (new java.util.Date()).getTime() > res.getTimestamp(this.configTabla.getProperty("expire")).getTime()) {
                    val = ResultadoAutenticacion.ACCESO_AUTORIZADO_TEMPORAL;
                }
                else{
                    modificaFechaAcceso(user);
                }
            }


            /*if (val == RecusoAutenticador.ACCESO_AUTORIZADO && this.configTabla.getProperty("expire") != null) {
            //registro el accerso
            PreparedStatement ps2 = con.prepareStatement("UPDATE " + this.configTabla.getProperty("table") +
            " SET " + this.configTabla.getProperty("expire") + "=?");
            ps2.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
            ps2.execute();
            ps2.close();
            }*/

            res.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            this.mLog.escribeLog(this.getClass(), Logger.TiposEvento.CRTITICO, "No se logro autenticar la cuenta '" + user + "'.", ex);
            throw new Exception("Error al validar cuenta, " + ex.getMessage());
        }

        return val;
    }

    /**
     * Obtiene el valor de un elemento mapeado la BD.
     * @param user Nickname del usuario
     * @param elemento Nombre del elemento deseado
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElemento(String user, String elemento) throws Exception {
        String c = null;

        if (this.configTabla.getProperty(elemento) != null) {
            try {
                this.sql = new StringBuffer("SELECT ").append(this.configTabla.getProperty(elemento)).append(" FROM ").append(this.configTabla.getProperty("table")).append(" WHERE ").append(this.configTabla.getProperty("user")).append("='").append(user).append("'");
                this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

                Connection con = this.generaConexion();
                Statement st = con.createStatement();
                ResultSet res = st.executeQuery(sql.toString());
                if (res.next()) {
                    c = res.getString(1);
                }
                res.close();
                st.close();
                con.close();


            } catch (SQLException ex) {
                this.mLog.escribeLog(this.getClass(), Logger.TiposEvento.CRTITICO, "No se logro recuperar el elemento '" + elemento + "'.", ex);
                throw new Exception("Error al obtener el elemento '" + elemento + "', " + ex.getMessage());
            }

        }

        return c;
    }

    private synchronized boolean existeCuenta(Connection con, String cuenta) throws Exception {
        boolean existe = false;

        this.sql = new StringBuffer("SELECT ").append(this.configTabla.getProperty("user")).append(" FROM ").append(this.configTabla.getProperty("table")).append(" WHERE ").append(this.configTabla.getProperty("user")).append("=?");
        this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        PreparedStatement ps = con.prepareStatement(sql.toString());
        ps.setString(1, cuenta);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            existe = true;
        }

        rs.close();
        ps.close();
        return existe;
    }

    private synchronized int getIdSiguiente(Connection con) throws Exception {
        int id = 1;

        this.sql = new StringBuffer("SELECT ").append(this.configTabla.getProperty("id")).append(" FROM ").append(this.configTabla.getProperty("table")).append(" ORDER BY ").append(this.configTabla.getProperty("id")).append(" DESC");
        this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql.toString());

        if (rs.next()) {
            id = rs.getInt(this.configTabla.getProperty("id")) + 1;
        }

        rs.close();
        st.close();
        return id;
    }

    /**
     * Agrega una cuenta a la BD.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public synchronized boolean agregaCuenta(Usuario user, String pass) throws Exception {
        boolean agrego = false;

        try {
            int id = 1;
            Connection con = this.generaConexion();
            //valido la existenci del usuario
            if (existeCuenta(con, user.getUser())) {
                con.close();
                return false;
            }

            //obtengo el ultimon numero del id
            id = getIdSiguiente(con);

            this.sql = new StringBuffer("INSERT INTO ").append(this.configTabla.getProperty("table")).append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setInt(1, id);//id
            ps.setString(2, user.getNombre());//nombre
            ps.setString(3, user.getUser());//login
            ps.setString(4, (this.cifrador != null ? this.cifrador.cifra(pass) : pass));//pass
            ps.setInt(5, 1);//activo
            ps.setString(6, null);//fecha exp
            ps.setString(7, null);//acceso
            ps.setString(8, "N");//tipo

            ps.execute();

            ps.close();
            con.close();
        } catch (SQLException ex) {
            this.mLog.escribeLog(this.getClass(), "No se logro generar la cuenta '" + user.getUser() + "'.", ex);
            throw new Exception("Error al generar cuenta, " + ex.getMessage());
        }

        return agrego;
    }

    /**
     * Agrega una cuenta a la BD, con la contrase&ntilde;a temporal configurada.
     * @param user Nickname del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public synchronized boolean agregaCuentaTemporal(Usuario user) throws Exception {
        boolean agrego = false;

        try {
            int id = 1;
            Connection con = this.generaConexion();
            //valido la existenci del usuario
            if (existeCuenta(con, user.getUser())) {
                con.close();
                return false;
            }

            //obtengo el ultimon numero del id
            id = getIdSiguiente(con);

            this.sql = new StringBuffer("INSERT INTO ").append(this.configTabla.getProperty("table")).append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setInt(1, id);//id
            ps.setString(2, user.getNombre());//nombre
            ps.setString(3, user.getUser());//login
            ps.setString(4, (this.cifrador != null ? this.cifrador.cifra("") : ""));//pass
            ps.setInt(5, 1);//activo
            ps.setString(6, null);//fecha exp
            ps.setString(7, null);//acceso
            ps.setString(8, "T");//tipo

            ps.execute();

            ps.close();
            con.close();
        } catch (SQLException ex) {
            this.mLog.escribeLog(this.getClass(), "No se logro generar la cuenta temporal.", ex);
            throw new Exception("Error al generar cuenta temporal, " + ex.getMessage());
        }

        return agrego;
    }

    /**
     * Modifica la contrase&ntilde;a de un usuario en la BD.
     * @param user Nickname del usuario
     * @param pass Nueva contraseña para el usuario contrase&ntilde;a
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    public boolean modificaContrasena(Usuario user, String pass) throws Exception {
        int i = 0;

        try {
            this.sql = new StringBuffer("UPDATE ").append(this.configTabla.getProperty("table")).append(" SET ").append(this.configTabla.getProperty("pass")).append("=? WHERE ").append(this.configTabla.getProperty("user")).append("=?");
            this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, (this.cifrador != null ? this.cifrador.cifra(pass) : pass));//pass
            ps.setString(2, user.getUser());

            i = ps.executeUpdate();

            ps.close();
            con.close();
        } catch (SQLException ex) {
            this.mLog.escribeLog(this.getClass(), "No se logro generar la cuenta temporal.", ex);
            throw new Exception("Error al modificar la contraseña, " + ex.getMessage());
        }

        return (i > 0);
    }

    private boolean modificaFechaAcceso(String user) throws Exception{
        int i = 0;

        try{
            this.sql = new StringBuffer("UPDATE ").append(this.configTabla.getProperty("table")).append(" SET ").append(this.configTabla.getProperty("access")).append("=? WHERE ").append(this.configTabla.getProperty("user")).append("=?");
            this.mDebug.escribeDebug(this.getClass(), "Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = this.generaConexion();
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(2, user);

            i = ps.executeUpdate();

            ps.close();
            con.close();
        } catch (SQLException ex) {
            this.mLog.escribeLog(this.getClass(), "No se logro actualizar la fecha de acceso.", ex);
            throw new Exception("Error al modificar la fecha de acceso, " + ex.getMessage());
        }

        return (i > 0);
    }

    /**
     * Obtiene el valor del elemento 'name' mapeado en la BD.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElementoNombre(String user) throws Exception {
        return this.obtieneElemento(user, "name");
    }

    /**
     * Obtiene el valor del elemento 'id' mapeado en la BD.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public String obtieneElementoId(String user) throws Exception {
        return this.obtieneElemento(user, "id");
    }
}
