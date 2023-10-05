package neoAtlantis.utilidades.ctrlAcceso.recursoBitacora;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.EventoBitacora;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.RecursoBitacorador;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * RecursoBitacorador que utiliza una base de datos para registrar los eventos.
 * <br><br>
 * La tabla en la BD debe contar con los siguientes campos:
 * <pre>
 * ===============================================================
 * |   fecha  | usuario | terminal |  origen |  evento | detalle |
 * | DATETIME | VARCHAR |  VARCHAR | VARCHAR | VARCHAR | VARCHAR |
 * ===============================================================
 * </pre>
 * Para facilitar la generaci&oacute;n de la tabla, se proporciona el script '<i>generaTablaBitacora.sql</i>' para la generaci&oacute;n de la misma.
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
public class BitacoraBD extends RecursoBitacorador {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";

    private Properties config;
    private String tabla="bitacora_na_data";

    /**
     * Genera una RecursoBitacorador por BD.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BitacoraBD(InputStream xml) throws Exception {
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

        this.validaConfiguracion(p);
    }

    /**
     * Genera una RecursoBitacorador por BD.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BitacoraBD(File xml) throws Exception {
        this(new FileInputStream(xml));
    }

    /**
     * Genera una RecursoBitacorador por BD.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BitacoraBD(String xml) throws Exception {
        this(new File(xml));
    }

    /**
     * Genera una RecursoBitacorador por BD.
     * @param config Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public BitacoraBD(Properties config) throws Exception {
        this.validaConfiguracion(config);
    }

    /**
     * Valida la correcta constitución de la configuración de conexión a BD.
     * @param p Properties con la configuración.
     * @throws java.lang.Exception
     */
    private void validaConfiguracion(Properties p) throws Exception {
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

        this.config = p;
    }

    /**
     * Genera la conexión a BD
     * @return
     * @throws java.lang.Exception
     */
    private Connection generaConexion() throws Exception {
        Class.forName(this.config.getProperty("driver"));
        return DriverManager.getConnection(this.config.getProperty("url"), this.config.getProperty("user"), this.config.getProperty("pass"));
    }

    /**
     * Contruye la tabla de bitacora en la BD configurada.
     * @return true si se logro generar
     * @throws java.lang.Exception
     */
    public boolean generaTablaBitacora() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("generaTablaBitacora.sql");

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
     * Registra un evento en el archivo de texto.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se realiza
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    public void escribeBitacora(String usuario, String terminal, String origen, EventoBitacora evento, String detalle) throws Exception {
        Connection con = this.generaConexion();
        PreparedStatement ps = con.prepareStatement("INSERT INTO "+this.tabla+" VALUES(?, ?, ?, ?, ?, ?)");
        ps.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
        ps.setString(2, (usuario.length() > 30 ? usuario.substring(0, 30) : usuario));
        ps.setString(3, (terminal.length() > 60 ? terminal.substring(0, 60) : terminal));
        ps.setString(4, (origen.length() > 30 ? origen.substring(0, 30) : origen));
        ps.setString(5, ""+evento);
        ps.setString(6, (detalle.length() > 255 ? detalle.substring(0, 30) : detalle));

        ps.execute();

        ps.close();
        con.close();
    }

    /**
     * Extrae los datos del archivo de texto para poder trabajar con ellos.
     * @param ini Registro desde donde se desea obtener la informaci&oacute;n
     * @param tam Numero de registro que se desean obtener
     * @param fil Filtros que se desean aplicar para obtener los registros
     * @return Registros obtenidos
     * @throws java.lang.Exception
     */
    public String[][] generaReporte(int ini, int tam, Properties fil) throws Exception {
        Connection con = this.generaConexion();
        Statement st=con.createStatement();
        ResultSet rs=st.executeQuery("SELECT fecha, usuario, terminal, origen, evento, detalle FROM "+this.tabla+" ORDER BY fecha DESC");
        ArrayList al=new ArrayList();
        long pos=0;
        int i=0;

        while(rs.next()){
            if(pos>=ini){
                al.add(parseaRegistro(rs));
                i++;
            }
            if( i>=tam ){
                break;
            }
            pos++;
            //System.out.println("-- "+i+", "+pos);
        }

        rs.close();
        st.close();
        con.close();

        this.posicion=pos+1;
        String[][] res = new String[al.size()][6];

        for (int j = 0; j < al.size(); j++) {
            res[j] = (String[]) al.get(j);
        }

        return res;
    }

    private String[] parseaRegistro(ResultSet rs) throws SQLException{
        String[] s=new String[6];

        s[0]=this.sdf.format(new java.util.Date(rs.getTimestamp("fecha").getTime()));
        s[1]=rs.getString("usuario");
        s[2]=rs.getString("terminal");
        s[3]=rs.getString("origen");
        s[4]=rs.getString("evento");
        s[5]=rs.getString("detalle");

        return s;
    }
}
