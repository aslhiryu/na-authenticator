package neoatlantis.accesscontroller.profiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.Role;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.printer.interfaces.UserAdministratorPrinter;
import neoatlantis.accesscontroller.profiler.interfaces.ProfilerWay;
import neoatlantis.utils.dataBase.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class DbProfiler extends ProfilerWay {
    protected static final Logger DEBUGER = Logger.getLogger(DbProfiler.class);

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
        campos.setProperty("ID", "id_rol");
        campos.setProperty("NAME", "nombre");
        campos.setProperty("TABLE", "rol_na");
        campos.setProperty("TABLE_ASIGNED_ROLES", "usuario_rol_na");
        campos.setProperty("ID_USER", "id_usuario");
    }
 
    
    
    
    
    
    
    
    // Contructores ------------------------------------------------------------
    
    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(Properties configBD, Properties fields) throws WayConfigurationException {
        try{
            ConfigurationDB.validateConfiguration(configBD);
            this.assignFields(fields);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbProfiler(String driver, String url, String user, String pass) throws WayConfigurationException {
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




    // Metodos protegidos-------------------------------------------------------

    /**
     * Genera el query utilizado para recuperar un rol por nombre
     * @return Cadena con el query
     */
    protected String getQuerySelectRole(){
        //                             1                        
        return "SELECT "+campos.getProperty("ID")+"  FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty("NAME")+"=?";
    }

    /**
     * Genera el query utilizado para obtener la lista de roles
     * @return Cadena con el query
     */
    protected String getQuerySelectRoles(){
        //                             1                             2            
        return "SELECT "+campos.getProperty("ID")+", "+campos.getProperty("NAME")+" FROM "+campos.getProperty("TABLE");
    }

    protected String getQuerySelectExistRole(){
        //                             1                             2            
        return "SELECT "+campos.getProperty("ID")+", "+campos.getProperty("NAME")+" FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty("NAME")+"=?";
    }

    protected String getQuerySelectRoleById(){
        //                             1                             2            
        return "SELECT "+campos.getProperty("ID")+", "+campos.getProperty("NAME")+" FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty("ID")+"=?";
    }

    protected String getQueryInsertRole(){
        //                                                                1                             2            
        return "INSERT INTO "+campos.getProperty("TABLE")+" ("+campos.getProperty("ID")+", "+campos.getProperty("NAME")+") VALUES (?, ?)";
    }

    protected String getQueryUpdateRole(){
        //                                                                1                             2            
        return "UPDATE "+campos.getProperty("TABLE")+" SET "+campos.getProperty("NAME")+"=? WHERE "+campos.getProperty("ID")+"=?";
    }

    protected String getQueryAddRole(){
        //                                                                1                             2            
        return "INSERT INTO "+campos.getProperty("TABLE_ASIGNED_ROLES")+ " ("+campos.getProperty("ID")+", "+campos.getProperty("ID_USER")+") VALUES (?,?)";
    }

    protected String getQueryRemoveRole(){
        //                                                                                             1                             2            
        return "DELETE FROM "+campos.getProperty("TABLE_ASIGNED_ROLES")+ " WHERE "+campos.getProperty("ID")+"=? AND "+campos.getProperty("ID_USER")+"=?";
    }

    /**
     * Genera el query utilizado para obtener la lista de roles asignados a un usuario
     * @return Cadena con el query
     */
    protected String getQuerySelectRolesUser(){
        //                             1                             2            
        return "SELECT RU."+campos.getProperty("ID")+", R."+campos.getProperty("NAME")+" FROM "+campos.getProperty("TABLE_ASIGNED_ROLES")+ " RU LEFT JOIN "+campos.getProperty("TABLE")+ " R ON RU."+campos.getProperty("ID")+"=R."+campos.getProperty("ID")+" WHERE RU."+campos.getProperty("ID_USER")+"=?";
    }


    
    


    // Metodos publicos-------------------------------------------------------

    @Override
    public List<Role> getRolesFromUser(User user, Object... param) throws WayAccessException {
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        ArrayList<Role> lTmp=new ArrayList<Role>();
        
        this.sql = new StringBuffer(this.getQuerySelectRolesUser());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getId());

            res = ps.executeQuery();
            while(res.next()){
                lTmp.add(new Role(res.getString(1), res.getString(2)));                
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro obtener los roles del usuario: "+user+".", ex);
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
        
        return lTmp;
    }

    @Override
    public boolean canAsignRoles() throws WayAccessException {
        return true;
    }
    
    @Override
    public List<Role> getRegisteredRoles() throws WayAccessException {
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        ArrayList<Role> lTmp=new ArrayList<Role>();
        
        this.sql = new StringBuffer(this.getQuerySelectRoles());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());

            res = ps.executeQuery();
            while(res.next()){
                lTmp.add(new Role(res.getString(1), res.getString(2)));                
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro obtener la lista de roles registrados", ex);
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
        
        return lTmp;
    }

    @Override
    public List<Role> getActivedRoles() throws WayAccessException {
        return this.getRegisteredRoles();
    }

    @Override
    public boolean canEditRoles() throws WayAccessException {
        return true;
    }

    @Override
    public Map<String, Object> createRole(Map<String, Object> datos) throws WayAccessException {
        HashMap<String, Object> data=new HashMap<String, Object>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        boolean existe=false;
        String id;
        
        //verifico que se cuente con los campos necesarios
        if( datos==null ){
            DEBUGER.debug("No existen datos");
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporcionaron los datos necesarios"});
        }
        else if(datos!=null && (datos.get(ProfilerWay.ROLE_DATA)==null || datos.get(ProfilerWay.ROLE_DATA).equals("")) ){
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporciono el nombre del rol"});
        }
        else{
            //verifico que no existe el rol
            this.sql = new StringBuffer(this.getQuerySelectExistRole());
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

            try{
                con = ConfigurationDB.createConection(this.config);
                ps = con.prepareStatement(sql.toString());
                ps.setString(1, (String)(datos.get(ProfilerWay.ROLE_DATA)));

                res = ps.executeQuery();
                //si existe el usuario
                if (res.next()) {
                    DEBUGER.debug("Ya existe el rol");
                    existe=true;
                }
            }catch(Exception ex){
                DEBUGER.fatal("No se logro consultar la existencia del rol", ex);
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

            //si no existe el rol
            if( !existe ){
                //genero la llave del rol
                id=UUID.randomUUID().toString();

                //genero el rol
                this.sql = new StringBuffer(this.getQueryInsertRole());

                try{
                    DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
                    con = ConfigurationDB.createConection(this.config);
                    ps = con.prepareStatement(sql.toString());
                    ps.setString(1, id);
                    ps.setString(2, (String)(datos.get(ProfilerWay.ROLE_DATA)));
                    
                    ps.executeUpdate();
                    
                    data.put(UserAdministratorPrinter.ID_DATA, id);
                }
                catch(Exception ex){
                    DEBUGER.fatal("No se logro agregar el rol '" + (datos.get(ProfilerWay.ROLE_DATA)) + "'.", ex);
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
            //si existe el rol
            else{
                data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"El rol ya existe"});
            }
        }
        
        return data;
    }

    @Override
    public Map<String, Object> updateRole(Map<String, Object> datos) throws WayAccessException {
        HashMap<String, Object> data=new HashMap<String, Object>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        boolean existe=false;
        
        //verifico que se cuente con los campos necesarios
        if( datos==null ){
            DEBUGER.debug("No existen datos");
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporcionaron los datos necesarios"});
        }
        else if(datos!=null && (datos.get(ProfilerWay.ROLE_DATA)==null || datos.get(ProfilerWay.ROLE_DATA).equals("")) ){
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporciono el nombre del rol"});
        }
        else{
            //verifico que no existe el rol
            this.sql = new StringBuffer(this.getQuerySelectExistRole());
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

            try{
                con = ConfigurationDB.createConection(this.config);
                ps = con.prepareStatement(sql.toString());
                ps.setString(1, (String)(datos.get(ProfilerWay.ROLE_DATA)));

                res = ps.executeQuery();
                //si existe el usuario
                if (res.next()) {
                    DEBUGER.debug("Ya existe el rol");
                    existe=true;
                }
            }catch(Exception ex){
                DEBUGER.fatal("No se logro consultar la existencia del rol", ex);
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

            //si no existe el rol
            if( !existe ){
                //actualizo el rol
                this.sql = new StringBuffer(this.getQueryUpdateRole());

                try{
                    DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
                    con = ConfigurationDB.createConection(this.config);
                    ps = con.prepareStatement(sql.toString());
                    ps.setString(1, (String)(datos.get(ProfilerWay.ROLE_DATA)));
                    ps.setString(2, (String)(datos.get(ProfilerWay.ID_DATA)));
                    
                    if(ps.executeUpdate()>0){
                        data.put(UserAdministratorPrinter.ID_DATA, (String)datos.get(ProfilerWay.ID_DATA));
                    }
                }
                catch(Exception ex){
                    DEBUGER.fatal("No se logro actualizar el rol '" + (datos.get(ProfilerWay.ROLE_DATA)) + "'.", ex);
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
            //si existe el rol
            else{
                data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"El rol ya existe"});
            }
        }
        
        return data;
    }

    @Override
    public boolean asignRoleToUser(String idUser, String idRole) throws WayAccessException{
        boolean exito=false;
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryAddRole());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, idRole);
            ps.setString(2, idUser);

            if( ps.executeUpdate()>0 ){
                exito=true;
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro asignar el rol solicitado", ex);
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
        
        return exito;
    }
    
    @Override
    public boolean removeRoleFromUser(String idUser, String idRole) throws WayAccessException{
        boolean exito=false;
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryRemoveRole());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, idRole);
            ps.setString(2, idUser);

            if( ps.executeUpdate()>0 ){
                exito=true;
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro remover el rol solicitado", ex);
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
        
        return exito;        
    }
    
    @Override
    public Role getRoleData(String id){
        Role r=null;
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        
        this.sql = new StringBuffer(this.getQuerySelectRoleById());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, id);

            res=ps.executeQuery();
            if( res.next() ){
                r=new Role(id, res.getString(2));
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro recuperar el rol solicitado", ex);
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

        return r;
    }
    
    @Override
    public boolean allowsUpdateRoles() throws WayAccessException{
        return true;
    }
    
    @Override
    public boolean allowsAsignPermissions() throws WayAccessException{
        return true;
    }
    
    
    
    /**
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean createRoleTable() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("rol.sql");

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
     * @param field Nombre del campo {ID, NAME, TABLE}
     * @param value Valor a asignar
     */
    public void setField(String field, String value){
        if(field!=null && field.equalsIgnoreCase("ID")){
            campos.setProperty("ID", value);
        }
        else if(field!=null && field.equalsIgnoreCase("NAME")){
            campos.setProperty("NAME", value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE")){
            campos.setProperty("TABLE", value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE_ASIGNED_ROLES")){
            campos.setProperty("TABLE_ASIGNED_ROLES", value);
        }
        else if(field!=null && field.equalsIgnoreCase("ID_USER")){
            campos.setProperty("ID_USER", value);
        }
    }

    
    private void assignFields(Properties p){
        if( p.getProperty("ID")!=null && !p.getProperty("ID").isEmpty() ){
            campos.setProperty("ID", p.getProperty("ID"));
        }
        if( p.getProperty("NAME")!=null && !p.getProperty("NAME").isEmpty() ){
            campos.setProperty("NAME", p.getProperty("NAME"));
        }
        if( p.getProperty("ID_USER")!=null && !p.getProperty("ID_USER").isEmpty() ){
            campos.setProperty("ID_USER", p.getProperty("ID_USER"));
        }
        if( p.getProperty("TABLE_ASIGNED_ROLES")!=null && !p.getProperty("TABLE_ASIGNED_ROLES").isEmpty() ){
            campos.setProperty("TABLE_ASIGNED_ROLES", p.getProperty("TABLE_ASIGNED_ROLES"));
        }
        if( p.getProperty("TABLE")!=null && !p.getProperty("TABLE").isEmpty() ){
            campos.setProperty("TABLE", p.getProperty("TABLE"));
        }
    }
    
}
