package neoatlantis.accesscontroller.audit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import neoatlantis.accesscontroller.audit.interfaces.AuditWay;
import neoatlantis.accesscontroller.audit.interfaces.EventAudit;
import neoatlantis.accesscontroller.authentication.DbAuthentication;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.LogEvent;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.entity.Event;
import neoatlantis.utils.dataBase.ConfigurationDB;
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
    private static final Logger DEBUGER = Logger.getLogger(DbAudit.class);

    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;
 
    
    private static Properties campos;
    
    static{
        campos=new Properties();
        campos.setProperty(AuditWay.ID_FIELD, "id_evento");
        campos.setProperty(AuditWay.USER_FIELD, "id_usuario");
        campos.setProperty(AuditWay.ORIGIN_FIELD, "origen");
        campos.setProperty(AuditWay.TERMINAL_FIELD, "terminal");
        campos.setProperty(AuditWay.TERMINAL_TYPE_FIELD, "tipo_terminal");
        campos.setProperty(AuditWay.DATE_FIELD, "fecha");
        campos.setProperty(AuditWay.EVENT_FIELD, "evento");
        campos.setProperty(AuditWay.DETAIL_FIELD, "detalle");
        campos.setProperty("TABLE", "bitacora_na");
    }    
    
    
    
    
    
    
    // Contructores ------------------------------------------------------------
    
    /**
     * Genera un AuditWay por BD.
     * @param xml Flujo de bits que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbAudit(InputStream xml) throws WayConfigurationException{
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xml);
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
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xml));
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
    public DbAudit(Properties configBD, Properties fields) throws WayConfigurationException {
        try{
            ConfigurationDB.validateConfiguration(configBD);
            this.assignFields(fields);
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
            ConfigurationDB.validateConfiguration(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    
    
    
    
    
    // metodos protegidos ------------------------------------------------------
    
    /**
     * Genera el query utilizado para la inserci&ocute;n de registros en la bitacora.
     * @return Cadena con el query
     */
    protected String getQueryEventInsert(){
        //                                                                         1                                          2                                          3                                                4                                               5                                                  6                               7                                8
        return "INSERT INTO "+campos.getProperty("TABLE")+" ("+campos.getProperty(AuditWay.ID_FIELD)+", "+campos.getProperty(AuditWay.USER_FIELD)+", "+campos.getProperty(AuditWay.ORIGIN_FIELD)+", "+campos.getProperty(AuditWay.TERMINAL_FIELD)+", "+campos.getProperty(AuditWay.TERMINAL_TYPE_FIELD)+", "+campos.getProperty(AuditWay.DATE_FIELD)+", "+campos.getProperty(AuditWay.EVENT_FIELD)+", "+campos.getProperty(AuditWay.DETAIL_FIELD)+")  VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * Genera el query utilizado para la recuperaci&ocute;n de registros de la bitacora.
     * @return Cadena con el query
     */
    protected String getQueryEventSelect(){
        //                            1                                                 2                                          3                                              4                                                5                                                     6                                            7                                              8
        return "SELECT "+campos.getProperty(AuditWay.ID_FIELD)+", "+campos.getProperty(AuditWay.USER_FIELD)+", "+campos.getProperty(AuditWay.ORIGIN_FIELD)+", "+campos.getProperty(AuditWay.TERMINAL_FIELD)+", "+campos.getProperty(AuditWay.TERMINAL_TYPE_FIELD)+", "+campos.getProperty(AuditWay.DATE_FIELD)+", "+campos.getProperty(AuditWay.EVENT_FIELD)+", "+campos.getProperty(AuditWay.DETAIL_FIELD)+" FROM  "+campos.getProperty("TABLE")+" ";
    }
    
    

    
    
    // metodos publicos --------------------------------------------------------
    
    @Override
    public void writeEvent(String idUsuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle, Map<String,Object> data) throws WayAccessException {
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryEventInsert());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");
        DEBUGER.debug("Con datos '"+idUsuario+"', '"+origen+"', '"+terminal+"', '"+tipoTerminal+"', '"+evento+"', '"+detalle+"'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, idUsuario);
            ps.setString(3, origen);
            ps.setString(4, terminal);
            ps.setString(5, tipoTerminal.toString());
            ps.setTimestamp(6, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(7, evento.toString());
            ps.setString(8, detalle!=null&&detalle.length()>255? detalle.substring(0, 255): detalle);
            

            ps.executeUpdate();
        }catch(Exception ex){
            DEBUGER.fatal("No se logro agregar el evento", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }
    }

    @Override
    public List<Event> getEvents(Map<String, Object> param, int regs, int offset) throws Exception {
        ArrayList<Event> evts=new ArrayList<Event>();        
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        LogEvent ev;
        int c=0;
        
        this.sql = new StringBuffer(this.getQueryEventSelect());
        //valida si tiene una fecha de inicio
        if( param.get(AuditWay.INITIAL_DATE_PARAM)!=null ){
            this.sql.append(" WHERE ").append(campos.getProperty(AuditWay.DATE_FIELD)).append(">=?");
            c++;
        }
        //valida si tiene una fecha de fin
        if( param.get(AuditWay.END_DATE_PARAM)!=null ){
            if(c==0){
                this.sql.append(" WHERE ");
            }
            else{
                this.sql.append(" AND ");
            }
            this.sql.append(campos.getProperty(AuditWay.DATE_FIELD)).append("<=?");
            c++;
        }
        //valida si viene un orden
        if( param.get(AuditWay.ORDER_PARAM)!=null && param.get(AuditWay.ORDER_TYPE_PARAM)!=null ){
            this.sql.append(" ORDER BY ").append(DbAuthentication.getDataOrder((String)param.get(AuditWay.ORDER_PARAM), campos)).append(" ").append(param.get(AuditWay.ORDER_TYPE_PARAM));
        }
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            c=1;

            //valida si tiene una fecha de inicio
            if( param.get(AuditWay.INITIAL_DATE_PARAM)!=null ){
                ps.setTimestamp(c, new Timestamp( ((Date)param.get(AuditWay.INITIAL_DATE_PARAM)).getTime() ));
                c++;
            }
            //valida si tiene una fecha de fin
            if( param.get(AuditWay.END_DATE_PARAM)!=null ){
                ps.setTimestamp(c, new Timestamp( ((Date)param.get(AuditWay.END_DATE_PARAM)).getTime() ));
                c++;
            }
            
            c=0;
            res=ps.executeQuery();
            while(res.next()){
                c++;
                //se brinca los registros que no son requeridos
                if( c<offset ){
                    continue;
                }

                ev=new LogEvent();
                ev.setUserId(res.getString(2));
                ev.setOrigin(res.getString(3));
                ev.setTerminal(res.getString(4));
                ev.setTerminalType( AuditWay.getEnvironmentTypeFromString(res.getString(5)) );
                ev.setEventDate(new java.util.Date(res.getTimestamp(6).getTime()));
                ev.setEvent( AuditWay.getEventAuditFromString(res.getString(7)) );
                ev.setDetail(res.getString(8));
                
                evts.add(ev);
                //valida si llego a la cantidad solicitada
                if( c>=offset-1+regs ){
                    DEBUGER.debug("Terminan los registros en: "+c);
                    break;
                }
            }
        }catch(Exception ex){
            DEBUGER.fatal("No se logro recuperar la lista eventos", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }
        
        return evts;
    }
    
    @Override
    public int getRegistries(Map<String, Object> param) throws WayAccessException{
        int c=0;
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        
        this.sql = new StringBuffer("SELECT COUNT(*) FROM ");
        this.sql.append(campos.getProperty("TABLE"));
        //valida si tiene una fecha de inicio
        if( param.get(AuditWay.INITIAL_DATE_PARAM)!=null ){
            this.sql.append(" WHERE ").append(campos.getProperty(AuditWay.DATE_FIELD)).append(">=?");
            c++;
        }
        //valida si tiene una fecha de fin
        if( param.get(AuditWay.END_DATE_PARAM)!=null ){
            if(c==0){
                this.sql.append(" WHERE ");
            }
            else{
                this.sql.append(" AND ");
            }
            this.sql.append(campos.getProperty(AuditWay.DATE_FIELD)).append("<=?");
            c++;
        }
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            c=1;

            //valida si tiene una fecha de inicio
            if( param.get(AuditWay.INITIAL_DATE_PARAM)!=null ){
                ps.setTimestamp(c, new Timestamp( ((Date)param.get(AuditWay.INITIAL_DATE_PARAM)).getTime() ));
                c++;
            }
            //valida si tiene una fecha de fin
            if( param.get(AuditWay.END_DATE_PARAM)!=null ){
                ps.setTimestamp(c, new Timestamp( ((Date)param.get(AuditWay.END_DATE_PARAM)).getTime() ));
                c++;
            }

            res=ps.executeQuery();
            if(res.next()){
                c=res.getInt(1);
            }
        }catch(Exception ex){
            DEBUGER.fatal("No se logro recuperar la lista eventos", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }
        
        return c;
    }
    

    
    /**
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean createAuditTable() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("bitacora.sql");

        if (in != null) {
            StringBuilder sb = new StringBuilder("");
            int c;

            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

            Connection con = ConfigurationDB.createConection(this.config);
            Statement st = con.createStatement();
            st.execute(sb.toString());
            st.close();
            con.close();

            return true;
        }


        return false;
    }
    
    /**
     * Modifica los campos utilizados por default para las consultas
     * @param field Nombre del campo {ID, ID_USER, ORIGIN, TERMINAL, TERMINAL_TYPE, DATE, EVENT, DETAIL, TABLE}
     * @param value Valor a asignar
     */
    public void setField(String field, String value){
        if(field!=null && field.equalsIgnoreCase("ID")){
            campos.setProperty(AuditWay.ID_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("ID_USER")){
            campos.setProperty(AuditWay.USER_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("ORIGIN")){
            campos.setProperty(AuditWay.ORIGIN_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("TERMINAL")){
            campos.setProperty(AuditWay.TERMINAL_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("TERMINAL_TYPE")){
            campos.setProperty(AuditWay.TERMINAL_TYPE_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("DATE")){
            campos.setProperty(AuditWay.DATE_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("EVENT")){
            campos.setProperty(AuditWay.EVENT_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("DETAIL")){
            campos.setProperty(AuditWay.DETAIL_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE")){
            campos.setProperty("TABLE", value);
        }
    }

    
    private void assignFields(Properties p){
        if( p.getProperty("ID")!=null && !p.getProperty("ID").isEmpty() ){
            campos.setProperty(AuditWay.ID_FIELD, p.getProperty("ID"));
        }
        if( p.getProperty("ID_USER")!=null && !p.getProperty("ID_USER").isEmpty() ){
            campos.setProperty(AuditWay.USER_FIELD, p.getProperty("ID_USER"));
        }
        if( p.getProperty("ORIGIN")!=null && !p.getProperty("ORIGIN").isEmpty() ){
            campos.setProperty(AuditWay.ORIGIN_FIELD, p.getProperty("ORIGIN"));
        }
        if( p.getProperty("TERMINAL")!=null && !p.getProperty("TERMINAL").isEmpty() ){
            campos.setProperty(AuditWay.TERMINAL_FIELD, p.getProperty("TERMINAL"));
        }
        if( p.getProperty("TERMINAL_TYPE")!=null && !p.getProperty("TERMINAL_TYPE").isEmpty() ){
            campos.setProperty(AuditWay.TERMINAL_TYPE_FIELD, p.getProperty("TERMINAL_TYPE"));
        }
        if( p.getProperty("DATE")!=null && !p.getProperty("DATE").isEmpty() ){
            campos.setProperty(AuditWay.DATE_FIELD, p.getProperty("DATE"));
        }
        if( p.getProperty("EVENT")!=null && !p.getProperty("EVENT").isEmpty() ){
            campos.setProperty(AuditWay.EVENT_FIELD, p.getProperty("EVENT"));
        }
        if( p.getProperty("DETAIL")!=null && !p.getProperty("DETAIL").isEmpty() ){
            campos.setProperty(AuditWay.DETAIL_FIELD, p.getProperty("DETAIL"));
        }
        if( p.getProperty("TABLE")!=null && !p.getProperty("TABLE").isEmpty() ){
            campos.setProperty("TABLE", p.getProperty("TABLE"));
        }
    }
}
