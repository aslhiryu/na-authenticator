package neoAtlantis.utilidades.accessController.audit;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import neoAtlantis.utilidades.objects.Event;
import org.apache.log4j.Logger;

/**
 * Medio Bitacorizador operado a traves de BD, para lo cual utiliza una tabla con
 * la siguiente estructura:<br>
 * <pre>
 * CREATE TABLE bitacora_na(
 *   usuario     VARCHAR(30),
 *   terminal    VARCHAR(60),
 *   origen      VARCHAR(30),
 *   fecha       DATETIME,
 *   evento      VARCHAR(1),
 *   detalle     VARCHAR(255)
 * );
 * </pre>
 * Para poder generar el objeto se hace uso de un documento XML con la siguiente 
 * estructura:<br>
 * <pre>
 * &lt;bd&gt;
 *   &lt;driver&gt;<i>clase_driver_a_utilizar</i>&lt;/driver&gt;
 *   &lt;url&gt;<i>url_de_conexion</i>&lt;/url&gt;
 *   &lt;user&gt;<i>usuario_de_bd</i>&lt;/user&gt;
 *   &lt;pass&gt;<i>contrase&ntilde;a_del_usuario</i>&lt;/pass&gt;
 * &lt;/bd&gt;
 * </pre>
 * o tambien puede contar con la siguiente estructura:<br>
 * <pre>
 * &lt;bd&gt;
 *   &lt;jndi&gt;<i>datasource_configurado_en_el_contenedor</i>&lt;/jndi&gt;
 * &lt;/bd&gt;
 * </pre>
 * <br>
 * Otra opci&oacute;n para generar el objeto es con un <b>java.util.Properties</b>, para lo cual 
 * deber&aacute; contar con los siguientes datos:
 * <pre>
 * driver=<i>clase_driver_a_utilizar</i>
 * url=<i>url_de_conexion</i>
 * user=<i>usuario_de_bd</i>
 * pass=<i>contrase&ntilde;a_del_usuario</i>
 * </pre>
 * o en su defecto solo puede contener el siguiente dato:
 * <pre>
 * jndi=<i>datasource_configurado_en_el_contenedor</i>
 * </pre>
 * En ambas configuraciones siempre que encuentre el parametro <i>jndi</i> se har&aacute;
 * caso omiso de los demas parametros.
 * <br><br>
 * Para trabajar adecuamente este objeto requiere la libreria de <b>NA_Utils</b>.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 4.0
 */
public class DbAudit extends AuditWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(DbAudit.class);

    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;

    /**
     * Genera un AuditWay por BD.
     * @param xml Flujo de bits que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(InputStream xml) throws WayConfigurationException{
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuditWay por BD.
     * @param xml Archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuditWay por BD.
     * @param xml Ruta del archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AuditWay por BD.
     * @param configBD Propiedades con la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un AuditWay por BD.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(String driver, String url, String user, String pass) throws WayConfigurationException {
        this.config.setProperty("driver", driver);
        this.config.setProperty("url", url);
        this.config.setProperty("user", user);
        this.config.setProperty("pass", pass);
        try{
            ConfigurationDB.validaConfigProperties(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    //---------------------------------------------------------------------------------

    /**
     * Genera el query utilizado para la inserci&ocute;n de registros en la bitacora.
     * @return Cadena con el query
     */
    protected String getQueryInsertBitacora(){
        //                                   1      2        3           4           5      6        7
        return "INSERT INTO bitacora_na (usuario, origen, terminal, tipo_terminal, fecha, evento, detalle)  VALUES(?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * Genera el query utilizado para la recuperaci&ocute;n de registros de la bitacora.
     * @return Cadena con el query
     */
    protected String getQueryGetBitacora(){
        //                1       2        3           4           5      6        7
        return "SELECT usuario, origen, terminal, tipo_terminal, fecha, evento, detalle FROM  bitacora_na ";
    }
    
    protected Map<String,String> getNombreCampos(){
        HashMap<String,String> m=new HashMap<String,String>();
        
        m.put("user", "usuario");
        m.put("origin", "origen");
        m.put("terminal", "terminal");
        m.put("terminalType", "tipo_terminal");
        m.put("date", "fecha");
        m.put("event", "evento");
        m.put("detail", "detalle");
        
        return m;
    }
    
    /**
     * Genera el query utilizado para la recuperaci&ocute;n de registros de la bitacora
     * con parametros.
     * @param params Filtro a utilizar para la consulta:<br>
     * <ul>
     * <li><b>user:</b> Usuario</li>
     * <li><b>origin:</b> IP</li>
     * <li><b>terminal:</b> Nombre de la terminal</li>
     * <li><b>terminalType:</b> Tipo de terminal</li>
     * <li><b>date:</b> Fecha del registro</li>
     * <li><b>event:</b> Tipo de evento</li>
     * <li><b>detail:</b> Detalle del evento</li>
     * </ul>
     * @return Cadena con el query
     */
    protected String getQueryGetBitacoraParams(Map<String,Object> params){
        StringBuilder where=new StringBuilder();
        StringBuilder order=new StringBuilder();
        String cTmp;
        
        if( params!=null ){
            //para where
            Iterator i=params.keySet().iterator();
            int j=0;
            while(i.hasNext()){
                cTmp=(String)i.next();
                if( cTmp.equals("order") || cTmp.equals("orderType") || cTmp.equals("regs") || cTmp.equals("page") ){
                    continue;
                }
                
                if(j>0){
                    where.append(" AND ");
                }

                where.append(getNombreCampos().get(cTmp)).append(" LIKE ?");
                j++;
            }
            
            //para order
            if( params!=null&&params.get("order")!=null ){
                order.append(" ORDER BY ");
                if( params.get("order").equals("date") ){
                    order.append(" 5 ");
                }
                else if( params.get("order").equals("origin") ){
                    order.append(" 2 ");
                }
                else if( params.get("order").equals("user") ){
                    order.append(" 1 ");
                }
                else if( params.get("order").equals("type") ){
                    order.append(" 6 ");
                }
            }
            if( order.length()>0 && params.get("orderType")!=null&&params.get("orderType").equals("desc") ){
                order.append(" DESC");
            }
        }

        return getQueryGetBitacora()+(where.length()>0? " WHERE "+where.toString(): "")+order;
    }

    /**
     * Carga un valor de los filtros para la consulta.
     * @param ps PreparedStatement que contiene la consulta
     * @param param Nombre del parametro:<br>
     * <ul>
     * <li><b>user:</b> Usuario</li>
     * <li><b>origin:</b> IP</li>
     * <li><b>terminal:</b> Nombre de la terminal</li>
     * <li><b>terminalType:</b> Tipo de terminal</li>
     * <li><b>date:</b> Fecha del registro</li>
     * <li><b>event:</b> Tipo de evento</li>
     * <li><b>detail:</b> Detalle del evento</li>
     * </ul>
     * @param value Valor del parametro
     * @param pos Posicion del parametro con respecto al PreparedStatement
     * @throws SQLException 
     */
    protected void asignaValorBitacora(PreparedStatement ps, String param, Object value, int pos) throws SQLException{
        if( param.equals("order") || param.equals("orderType") || param.equals("regs") || param.equals("page") ){
            return;
        }
        
        if(param.equalsIgnoreCase("user")){
            ps.setString(pos, (String)value);
            pos++;
        }
        else if(param.equalsIgnoreCase("origin")){
            ps.setString(pos, (String)value);
            pos++;
        }
        else if(param.equalsIgnoreCase("terminal")){
            ps.setString(pos, (String)value);
            pos++;
        }
        else if(param.equalsIgnoreCase("terminalType")){
            ps.setString(pos, (String)value);
            pos++;
        }
        else if(param.equalsIgnoreCase("date")){
            ps.setTimestamp(pos, new Timestamp(((java.util.Date)value).getTime()));
            pos++;
        }
        else if(param.equalsIgnoreCase("event")){
            ps.setString(pos, (String)value);
            pos++;
        }
        else if(param.equalsIgnoreCase("detail")){
            ps.setString(pos, (String)value);
            pos++;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Escribir registro en la bitacora.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se registra:
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    @Override
    public void escribeBitacora(String usuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle) throws WayAccessException {
        this.sql = new StringBuffer(this.getQueryInsertBitacora());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
        
        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, usuario);
            ps.setString(2, origen);
            ps.setString(3, terminal);
            ps.setString(4, tipoTerminal.toString());
            ps.setTimestamp(5, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(6, evento.toString());
            ps.setString(7, detalle);

            ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro generar la bitacora de '" + usuario + "'.", ex);
            throw new WayAccessException(ex);
        }
    }

    /**
     * Recupera los registros de la bitacora.
     * @param param Filtros que se desean aplicar para obtener los registros
     * @param regs Numero de registro que se desean obtener
     * @param offset Registro desde donde se desea obtener la informaci&oacute;n
     * @return Registros obtenidos
     * @throws java.lang.Exception
     */
    @Override
    public List<Event> recuperaBitacora(Map<String,Object> param, int regs, int offset) throws WayAccessException{
        List<Event> evts=new ArrayList<Event>();        
        AuditEvent e=null;
        String cTmp;
        int c=0;
        boolean cortado=false;
        
        this.sql = new StringBuffer(this.getQueryGetBitacoraParams(param));
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
        
        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            
            if( param!=null ){
                //cargo los datos
                Iterator i=param.keySet().iterator();                
                int j=1;
                while(i.hasNext()){              
                    cTmp=(String)i.next();
                    asignaValorBitacora(ps, cTmp, param.get(cTmp), j);
//                    j++;
                }
            }

            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                c++;
                //se brinca los registros que no son requeridos
                if( c<offset ){
                    continue;
                }
                
                //logger.debug("Inicia los registros en: "+c);
                e=new AuditEvent();
                
                e.setUsuario(rs.getString(1));
                e.setOrigen(rs.getString(2));
                e.setTerminal(rs.getString(3));
                cTmp=rs.getString(4);
                if( cTmp.equalsIgnoreCase("web")){
                    e.setTipoTerminal(EnvironmentType.WEB);
                }
                else{
                    e.setTipoTerminal(EnvironmentType.STANDALONE);
                }                
                e.setFecha(new java.util.Date(rs.getTimestamp(5).getTime()));
                cTmp=rs.getString(6);
                if( cTmp.equalsIgnoreCase("r")){
                    e.setEvento(EventAudit.RECHAZO);
                }
                else if( cTmp.equalsIgnoreCase("a")){
                    e.setEvento(EventAudit.ACCESO);
                }
                else if( cTmp.equalsIgnoreCase("b")){
                    e.setEvento(EventAudit.BLOQUEO);
                }
                else if( cTmp.equalsIgnoreCase("o")){
                    e.setEvento(EventAudit.EGRESO);
                }
                else if( cTmp.equalsIgnoreCase("i")){
                    e.setEvento(EventAudit.INGRESO);
                }
                else if( cTmp.equalsIgnoreCase("n")){
                    e.setEvento(EventAudit.NEGOCIO);
                }
                else{
                    e.setEvento(EventAudit.LOGEO);
                }                
                e.setDetalle(rs.getString(7));
                
                evts.add(e);
                
                //valida si llego a la cantidad solicitada
                if( c>=offset-1+regs ){
                    logger.debug("Terminan los registros en: "+c);
                    cortado=true;
                    break;
                }
            }
            
            //si se terminaron los registros marco el fin
            if( !cortado ){
                evts.add(null);
            }

            rs.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro obtener la bitacora con '" + param + "'.", ex);
            throw new WayAccessException(ex);
        }
        
        return evts;
    }
    
}
