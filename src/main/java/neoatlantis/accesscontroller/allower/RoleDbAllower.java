package neoatlantis.accesscontroller.allower;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import neoatlantis.accesscontroller.allower.interfaces.AllowerWay;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.Permission;
import neoatlantis.accesscontroller.objects.Role;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.utils.dataBase.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class RoleDbAllower extends AllowerWay {
    protected static final Logger DEBUGER = Logger.getLogger(RoleDbAllower.class);

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
        campos.setProperty("ID", "id_permiso");
        campos.setProperty("NAME", "nombre");
        campos.setProperty("DESCRIPTION", "descripcion");
        campos.setProperty("TABLE", "permiso_na");
        campos.setProperty("TABLE_ASIGNED_PERMISSIONS", "rol_permiso_na");
        campos.setProperty("ID_ROLE", "id_rol");
    }
 
    
    
    
    
    
    
    
    // Contructores ------------------------------------------------------------

    /**
     * Genera un AllowerWay por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public RoleDbAllower(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AllowerWay por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public RoleDbAllower(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AllowerWay por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public RoleDbAllower(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AllowerWay por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public RoleDbAllower(Properties configBD, Properties fields) throws WayConfigurationException {
        try{
            ConfigurationDB.validateConfiguration(configBD);
            this.assignFields(fields);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un AllowerWay por Base de Datos.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public RoleDbAllower(String driver, String url, String user, String pass) throws WayConfigurationException {
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
    
    protected String getQuerySelectPermissions(){
        //                             1                             2                               3  
        return "SELECT "+campos.getProperty("ID")+", "+campos.getProperty("NAME")+", "+campos.getProperty("DESCRIPTION")+" FROM "+campos.getProperty("TABLE");
    }

    protected String getQuerySelectPermissionsRole(){
        //                             1                             2                                     3
        return "SELECT PR."+campos.getProperty("ID")+", P."+campos.getProperty("NAME")+", P."+campos.getProperty("DESCRIPTION")+" FROM "+campos.getProperty("TABLE_ASIGNED_PERMISSIONS")+ " PR LEFT JOIN "+campos.getProperty("TABLE")+ " P ON PR."+campos.getProperty("ID")+"=P."+campos.getProperty("ID")+" WHERE PR."+campos.getProperty("ID_ROLE")+"=?";
    }
    
    protected String getQueryAddPermissionToRole(){
        //                                                                1                             2            
        return "INSERT INTO "+campos.getProperty("TABLE_ASIGNED_PERMISSIONS")+ " ("+campos.getProperty("ID")+", "+campos.getProperty("ID_ROLE")+") VALUES (?,?)";
    }

    protected String getQueryRemovePermissionFromRole(){
        //                                                                                             1                             2            
        return "DELETE FROM "+campos.getProperty("TABLE_ASIGNED_PERMISSIONS")+ " WHERE "+campos.getProperty("ID")+"=? AND "+campos.getProperty("ID_ROLE")+"=?";
    }
    
    protected String getQuerySelectPermissionById(){
        //                             1                             2            
        return "SELECT "+campos.getProperty("ID")+", "+campos.getProperty("NAME")+", "+campos.getProperty("DESCRIPTION")+" FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty("ID")+"=?";
    }

    
    
    
    
    
    
    
    
    
    
    // Metodos publicos-------------------------------------------------------

    @Override
    public List<Permission> getRegisteredPermissions() throws WayAccessException{
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        ArrayList<Permission> lTmp=new ArrayList<Permission>();
        Permission pTmp;
        
        this.sql = new StringBuffer(this.getQuerySelectPermissions());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());

            res = ps.executeQuery();
            while(res.next()){
                pTmp=new Permission(res.getString(1), res.getString(2));
                pTmp.setDescription(res.getString(3));
                lTmp.add(pTmp);                
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro obtener la lista de permisos registrados", ex);
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
    public List<Permission> getActivedPermissions() throws WayAccessException {
        return this.getRegisteredPermissions();
    }

    @Override
    public List<Permission> getAssignedPermissions(Role role, Map<String, Object> param) throws WayAccessException {
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        ArrayList<Permission> lTmp=new ArrayList<Permission>();
        Permission pTmp;
        
        this.sql = new StringBuffer(this.getQuerySelectPermissionsRole());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, role.getId());

            res = ps.executeQuery();
            while(res.next()){
                pTmp=new Permission(res.getString(1), res.getString(2));
                pTmp.setDescription(res.getString(3));
                lTmp.add(pTmp);                
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro obtener los permisos del rol: "+role+".", ex);
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
    public List<Permission> getAssignedPermissions(User user, Map<String, Object> param) throws WayAccessException {
        return new ArrayList<Permission>();
    }
    
    @Override
    public boolean canEditPermissions() throws WayAccessException{
        return true;
    }
    
    @Override
    public boolean asignPermissionToRole(String idRole, String idPermission) throws WayAccessException{
        boolean exito=false;
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryAddPermissionToRole());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, idPermission);
            ps.setString(2, idRole);

            if( ps.executeUpdate()>0 ){
                exito=true;
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro asignar el permiso solicitado", ex);
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
    public boolean removePermissionFromRole(String idRole, String idPermission) throws WayAccessException{
        boolean exito=false;
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryRemovePermissionFromRole());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, idPermission);
            ps.setString(2, idRole);

            if( ps.executeUpdate()>0 ){
                exito=true;
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro remover el permiso solicitado", ex);
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
    public Permission getPermissionData(String id){
        Permission p=null;
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        
        this.sql = new StringBuffer(this.getQuerySelectPermissionById());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, id);

            res=ps.executeQuery();
            if( res.next() ){
                p=new Permission(id, res.getString(2));
                p.setDescription(res.getString(3));
            }            
        }catch(Exception ex){
            DEBUGER.fatal("No se logro recuperar el permiso solicitado", ex);
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

        return p;
    }
    
    
    
    
    /**
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean createRoleTable() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("permiso.sql");

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
        else if(field!=null && field.equalsIgnoreCase("DESCRIPTION")){
            campos.setProperty("DESCRIPTION", value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE")){
            campos.setProperty("TABLE", value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE_ASIGNED_PERMISSIONS")){
            campos.setProperty("TABLE_ASIGNED_PERMISSIONS", value);
        }
        else if(field!=null && field.equalsIgnoreCase("ID_ROLE")){
            campos.setProperty("ID_ROLE", value);
        }
    }

    
    
    
    
    
    
    // Metodos privados---------------------------------------------------------
    
    private void assignFields(Properties p){
        if( p.getProperty("ID")!=null && !p.getProperty("ID").isEmpty() ){
            campos.setProperty("ID", p.getProperty("ID"));
        }
        if( p.getProperty("NAME")!=null && !p.getProperty("NAME").isEmpty() ){
            campos.setProperty("NAME", p.getProperty("NAME"));
        }
        if( p.getProperty("DESCRIPTION")!=null && !p.getProperty("DESCRIPTION").isEmpty() ){
            campos.setProperty("DESCRIPTION", p.getProperty("DESCRIPTION"));
        }
        if( p.getProperty("ID_ROLE")!=null && !p.getProperty("ID_ROLE").isEmpty() ){
            campos.setProperty("ID_ROLE", p.getProperty("ID_ROLE"));
        }
        if( p.getProperty("TABLE_ASIGNED_PERMISSIONS")!=null && !p.getProperty("TABLE_ASIGNED_PERMISSIONS").isEmpty() ){
            campos.setProperty("TABLE_ASIGNED_PERMISSIONS", p.getProperty("TABLE_ASIGNED_PERMISSIONS"));
        }
        if( p.getProperty("TABLE")!=null && !p.getProperty("TABLE").isEmpty() ){
            campos.setProperty("TABLE", p.getProperty("TABLE"));
        }
    }
}
