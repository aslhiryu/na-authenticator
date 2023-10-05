package neoAtlantis.utils.accessController.objects;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;
import neoAtlantis.utils.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utils.apps.web.objects.ApplicationSession;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Representación de un usuario en el sistema
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 3.0
 */
public class User extends PermissionEntity implements Serializable{
    private String id;
    private String nombre;
    private String user;
    private String mail;
    private List<Role> roles;
    private Map<String,List<String>> propiedades;
    private EnvironmentType tipoTerminal;
    private String origen;
    private String terminal;
    private ValidationResult estado;
    private ApplicationSession sesion;
    private Date generacion;
    private Date actividad;
    private Date ultimoAcceso;
    private byte[] photo;
    private boolean active;

    
    
    // Contructores ------------------------------------------------------------
    
    
    /**
     * Genera un Usuario indicando su origen de conexi&oacute;n y tipo de terminal.
     * @param id Identificador del usuario
     * @param user Nickname del usuario
     * @param origen Origen de conecxi&oacute;n
     * @param terminal Terminal de conexi&oacute;n
     * @param iniciaRol Bandera que indica si se necesita inicializar con el rol Usuarios
     */
    public User(String id, String user, String origen, String terminal, EnvironmentType tipoTerminal, boolean iniciaRol) {
        if (id==null || user==null || user.length()==0 || origen == null || terminal == null || origen.length() == 0 ) {
            throw new RuntimeException("No se puede generar un usuario sin id, user, terminal u origen.");
        }

        this.roles=new ArrayList<Role>();
        if( iniciaRol ){
            this.roles.add(new Role("USERS"));
        }
        this.propiedades = new HashMap<String,List<String>>();
        this.origen = origen;
        this.tipoTerminal = (tipoTerminal==null? EnvironmentType.STANDALONE: tipoTerminal);
        this.user = user;
        this.id=id;
        this.actividad=new Date();
        this.estado=ValidationResult.NOT_FOUND;
    }
    
    /**
     * Genera un Usuario indicando su origen de conexi&oacute;n y tipo de terminal.
     * @param id Identificador del usuario
     * @param user Nickname del usuario
     * @param origen Origen de conecxi&oacute;n
     * @param terminal Terminal de conexi&oacute;n
     */
    public User(String id, String user, String origen, String terminal, EnvironmentType tipoTerminal) {
        this(id, user, origen, terminal, tipoTerminal, true);
    }
    
    /**
     * Genera un Usuario indicando su origen de conexi&oacute;n y tipo de terminal.
     * @param user Nickname del usuario
     * @param origen Origen de conecxi&oacute;n
     * @param terminal Terminal de conexi&oacute;n
     */
    public User(String user, String origen, String terminal, EnvironmentType tipoTerminal) {
        this(user, user, origen, terminal, tipoTerminal);
    }

    /**
     * Genera un Usuario indicando su origen de conexi&oacute;n.
     * @param user Nickname del usuario
     * @param origen Origen de conexi&oacute;n
     */
    public User(String user, String origen, String terminal) {
        this(user, origen, terminal, EnvironmentType.WEB);
    }


    
    
    // Metodos publicos---------------------------------------------------------

    /**
     * Recupera un usuario que no representa a nadie
     * @return Usuario desconocido
     */
    public static User getNobody(){
        return new User("-1", "Nobody", "0.0.0.0", "", EnvironmentType.STANDALONE);
    }
    
    /**
     * Regresa el identificador del usuario.
     * @return Identificador
     */
    public String getId() {
        return id;
    }

    /**
     * Regresa el nombre del usuario.
     * @return Nombre
     */
    public String getName() {
        return nombre;
    }

    /**
     * Asigna un nombre al usuario.
     * @param nombre Nombre para el usuario
     */
    public void setName(String nombre) {
        this.nombre = nombre;
//        this.sesion=new AccessSession(this);
    }

    /**
     * Regresa el nickname del usuario.
     * @return Nickname
     */
    public String getUser() {
        return user;
    }

    /**
     * Regresa el e-mail del usuario
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * Asigna el e-mail del usuario
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Regresa los roles que tiene asignados el usuario.
     * @return Roles
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Agrega un rol para el usuario.
     * @param rol Rol a agregar
     */
    public void addRole(Role rol) {
        boolean existe=false;

        for(Role r: this.roles){
            if( r.getId().equals(rol.getId()) ){
                existe=true;
                break;
            }
        }

        if( !existe ){
            this.roles.add(rol);
        }
    }

    /**
     * Agrega un grupo de roles al asuario
     * @param roles 
     */
    public void addRoles(List<Role> roles) {
        for(Role r: roles){
            this.addRole(r);
        }
    }

    /**
     * Remueve un rol de los asignados al usuario.
     * @param rol Rol a remover
     */
    public void removeRole(Role rol) {
        this.roles.remove(rol);
    }

    /**
     * Obtiene el nombre de las propiedades asignadas al usuario.
     * @return Arreglo con nombres de las propiedades
     */
    public String[] getProperties() {
        String[] t=new String[this.propiedades.size()];
        Iterator iter=this.propiedades.keySet().iterator();
        int i=0;

        while(iter.hasNext()){
            t[i]=(String)iter.next();
            i++;
        }

        return t;
    }

    /**
     * Regresa los valores que tiene una determinada propiedad del usuario.
     * @param propiedad Propiedad de la que se desean sus valores
     * @return Valores de la propiedad
     */
    public String[] getProperty(String propiedad) {
        String[] a=new String[( this.propiedades.get(propiedad)!=null? this.propiedades.get(propiedad).size(): 0)];

        for(int i=0;  this.propiedades.get(propiedad)!=null&&i< this.propiedades.get(propiedad).size(); i++){
            a[i]= this.propiedades.get(propiedad).get(i);
        }

        return a;
    }

    /**
     * Agrega o actualiza una propiedad del usuario.
     * @param propiedad Propiedad a agregar o actualizar
     * @param valores Valores de la propiedad
     */
    public void setProperty(String propiedad, String[] valores) {
        ArrayList<String> al=new ArrayList<String>();
        al.addAll(Arrays.asList(valores));

        this.setProperty(propiedad, al);
    }

    /**
     * Agrega o actualiza una propiedad del usuario.
     * @param propiedad Propiedad a agregar o actualizar
     * @param valores Valores de la propiedad
     */
    public void setProperty(String propiedad, List<String> valores) {
        this.propiedades.put(propiedad, valores);
    }

    /**
     * Verifica si tiene activo un permiso.
     * @param permiso Permiso a validar
     * @return true si esta activo
     */
    @Override
    public boolean validatePermission(String permiso) {
        if (super.validatePermission(permiso)) {
            return true;
        }

        for(Role r: this.roles){
            if ( r.validatePermission(permiso)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Valida si el usuario cuenta con alguno d elos permisos proporcionados.
     * @param permisos Arreglo con los permisos a validar
     * @return true si cuenta con algun permiso
     */
    public boolean hasAnyPermission(String[] permisos){
        for(String p: permisos){
            if( this.validatePermission(p) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene el valor asignado a un permiso.
     * @param permiso Permiso a obtener
     * @return valor del permiso
     */
    @Override
    public String getPermission(String permiso) {
        String t = super.getPermission(permiso);

        if (t != null) {
            return t;
        }

        for (int i = 0; i < this.roles.size(); i++) {
            t = this.roles.get(i).getPermission(permiso);
            if (t != null) {
                return t;
            }
        }

        return null;
    }

    /**
     * Valida la pertenencia del usuario a un determinado ROL.
     * @param rol Nombre del rol a validar
     * @return true si es que pertenece al rol
     */
    public boolean hasRole(String rol){
        for (int i = 0; i < this.roles.size(); i++) {
            if( this.roles.get(i).getName().equalsIgnoreCase(rol.toUpperCase()) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Valida la pertenencia del usuario a cualquiera de un grupo de roles.
     * @param roles Arreglo con los roles
     * @return true si pertenece a alguno
     */
    public boolean hasAnyRole(String[] roles){
        for (int i = 0; i < this.roles.size(); i++) {
            for(int j=0; roles!=null&&j<roles.length; j++){
                if( this.roles.get(i).getName().equalsIgnoreCase(roles[j].toUpperCase()) ){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Genera la informac&oacute;n del usuario.
     * @return Informaci&oacute;n del usuario
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        Iterator iter=this.propiedades.keySet().iterator();
        String cTmp;

        sb.append("/*********************  USUARIO  ********************//").append(System.getProperty("line.separator"));
        sb.append("ID: ").append(this.id).append(System.getProperty("line.separator"));
        sb.append("Nombre: ").append(this.nombre).append(System.getProperty("line.separator"));
        sb.append("User: ").append(this.user).append(System.getProperty("line.separator"));
        sb.append("Mail: ").append(this.mail).append(System.getProperty("line.separator"));
        while(iter.hasNext()){
            cTmp=(String)iter.next();
            sb.append(cTmp).append(": ");
            for (int j = 0; this.propiedades.get(cTmp)!=null && j<this.propiedades.get(cTmp).size(); j++) {
                if (j > 0) {
                    sb.append(", ");
                }
                sb.append(this.propiedades.get(cTmp).get(j));
            }
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("Roles: ");
        for (int i = 0; this.roles != null && i < this.roles.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(((Role) this.roles.get(i)).getName());
        }
        sb.append(System.getProperty("line.separator"));
        sb.append(super.toString());
        sb.append("Origen: ").append(this.origen).append(System.getProperty("line.separator"));
        sb.append("Terminal: ").append(this.terminal).append(System.getProperty("line.separator"));
        sb.append("Tipo de Terminal: ").append(this.tipoTerminal).append(System.getProperty("line.separator"));
        sb.append("Estado: ").append(this.estado).append(System.getProperty("line.separator"));
        sb.append("Sesion: ").append(this.sesion).append(System.getProperty("line.separator"));
        sb.append("Generación: ").append(this.generacion).append(System.getProperty("line.separator"));
        sb.append("Activo: ").append(this.active).append(System.getProperty("line.separator"));
        sb.append("Ult. Acceso: ").append(this.ultimoAcceso).append(System.getProperty("line.separator"));
        sb.append("Ult. Actividad: ").append(this.actividad).append(System.getProperty("line.separator"));
        sb.append("/****************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }
    
    @Override
    public User clone(){
        User uTmp=new User(this.id, this.user, this.origen, this.terminal, this.tipoTerminal) ;
        
        uTmp.active=this.active;
        uTmp.actividad=this.actividad;
        uTmp.estado=this.estado;
        uTmp.generacion=this.generacion;
        uTmp.mail=this.mail;
        uTmp.nombre=this.nombre;
        uTmp.permisos=(ArrayList)this.permisos.clone();
        uTmp.photo=this.photo;
        uTmp.propiedades=this.propiedades;
        uTmp.roles=(ArrayList)((ArrayList)this.roles).clone();
        uTmp.sesion=this.sesion;
        uTmp.ultimoAcceso=this.ultimoAcceso;
        
        return uTmp;
    }

    /**Regresa el tipo de terminal de conexi&oacute;n.
     * @return Tipo de conexi&oacute;n
     */
    public EnvironmentType getEnvironmentType() {
        return tipoTerminal;
    }

    /**Asigna el tipo de terminal de conexi&oacute;n.
     * @param terminal Tipo de terminal para la conexi&oacute;n
     */
    public void setEnvironmentType(EnvironmentType terminal) {
        this.tipoTerminal = terminal;
    }

    /**Regresa el origen de conexi&oacute;n.
     * @return Origen de conexi&oacute;n
     */
    public String getOrigin() {
        return origen;
    }

    /**Asigna el origen de conexi&oacute;n.
     * @param origen Origen de la conexi&oacute;n
     */
    public void setOrigin(String origen) {
        this.origen = origen;
//        this.sesion=new AccessSession(this);
    }

    /**
     * Obtienen el estado actual del usuario
     * @return the estado
     */
    public ValidationResult getState() {
        return estado;
    }

    /**
     * Asigna el estado del usuario
     * @param estado the estado to set
     */
    public void setState(ValidationResult estado) {
        this.estado = estado;
    }

    /**
     * Recupera la terminal desde donde esta conectado el usuario
     * @return the terminal
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * Asigna la terminal desde donde esta conectado el usuario
     * @param terminal the terminal to set
     */
    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    /**
     * Recupera la sesion del usuario
     * @return the sesion
     */
    public ApplicationSession getSession() {
        return sesion;
    }
    
    /**
     * Define una nueva sesión pra ael usuario
     * @param session 
     */
    public void newSession(ApplicationSession session) {
        this.sesion=session;
    }

    /**
     * Recupera la fecha de gebneracion del usuario
     * @return the generacion
     */
    public Date getCreatedDate() {
        return generacion;
    }

    /**
     * Asigna la fecha de generacion del usuario
     * @return the generacion
     */
    void setCreatedDate(Date generacion) {
        this.generacion=generacion;
    }
    
    public void setActive(boolean active){
        this.active=active;
    }
    
    public boolean isActive(){
        return this.active;
    }

    /**
     * Recupera la fecha de ultima actividad del usuario
     * @return the actividad
     */
    public Date getActivityDate() {
        return actividad;
    }

    /**
     * Asigna la fecha de ultima actividad del usuario
     * @param actividad the actividad to set
     */
    public void setActivityDate(Date actividad) {
        this.actividad = actividad;
        if( this.sesion!=null ){
            this.sesion.setLastActivity(new Date());
        }
    }

    /**
     * Recupera la fecha del ultimo acceso del usuario
     * @return the actividad
     */
    public Date getLastAccessDate() {
        return this.ultimoAcceso;
    }

    /**
     * Asigna la fecha del ultimo acceso del usuario
     * @param actividad the actividad to set
     */
    public void setLastAccessDate(Date acceso) {
        this.ultimoAcceso = acceso;
    }

    public void setPhoto(byte[] photo){
        this.photo=photo;
    }
    
    public byte[] getPhoto(){
        return this.photo;
    }
    
    /**
     * Genera un documento xml que representa a la entidad.
     * @return Documento de JDom.
     */
    public Document toXml(){
        Element raiz=new Element("user");
        Document doc=new Document(raiz);

        this.defineXmlStructure(raiz);

        return doc;
    }

    /**
     * Genera un usuario a partir de un XML
     * @param xml Cadena con el xml
     * @return Usuario generado
     * @throws Exception 
     */
    public static User parseXml(String xml) throws Exception{
        User u;
        boolean exito=false;
        String id=null;
        String usuario=null;
        String origen=null;
        String terminal=null;
        EnvironmentType tipo=null;

        Element r, e;
        Document doc=(new SAXBuilder(false)).build(new StringReader(xml));
        r=doc.getRootElement();

        List<Element> l2, l=r.getChildren("atributte");
        for(Element eTmp: l){
            if(eTmp.getAttributeValue("name").equals("id")){
                id=eTmp.getText();
            }
            else if(eTmp.getAttributeValue("name").equals("user")){
                usuario=eTmp.getText();
            }
            else if(eTmp.getAttributeValue("name").equals("origen")){
                origen=eTmp.getText();
            }
            else if(eTmp.getAttributeValue("name").equals("terminal")){
                terminal=eTmp.getText();
            }
            else if(eTmp.getAttributeValue("name").equals("tipoTerminal")){
                if( eTmp.getText().equals("WEB") ){
                    tipo=EnvironmentType.WEB;
                }
                else {
                    tipo=EnvironmentType.STANDALONE;
                }
            }
        }
        
        if(id==null || usuario==null || origen==null || terminal==null || tipo==null){
            throw new RuntimeException("Usuario no valido");
        }
        
        u=new User(id, usuario, origen, terminal, tipo);
        for(Element eTmp: l){
            if( eTmp.getAttributeValue("name").equals("nombre") ){
                u.setName(eTmp.getText());
                exito=true;
            }
            else if(eTmp.getAttributeValue("name").equals("mail")){
                u.setMail(eTmp.getText());
            }
            else if(eTmp.getAttributeValue("name").equals("estado")){
                if( eTmp.getText().equals("BLOQUEADO") ){
                    u.setState(ValidationResult.BLOCKED);
                }
                else if( eTmp.getText().equals("CADUCADO") ){
                    u.setState(ValidationResult.LAPSED);
                }
                else if( eTmp.getText().equals("EN_USO") ){
                    u.setState(ValidationResult.IN_USE);
                }
                else if( eTmp.getText().equals("ERROR_CODIGO") ){
                    u.setState(ValidationResult.CODE_ERROR);
                }
                else if( eTmp.getText().equals("FUERA_DE_TIEMPO") ){
                    u.setState(ValidationResult.OUTTIME);
                }
                else if( eTmp.getText().equals("INACTIVO") ){
                    u.setState(ValidationResult.INACTIVE);
                }
                else if( eTmp.getText().equals("LIMITE_REBASADO") ){
                    u.setState(ValidationResult.EXCEED_LIMIT);
                }
                else if( eTmp.getText().equals("NO_ENCONTRADO") ){
                    u.setState(ValidationResult.NOT_FOUND);
                }
                else if( eTmp.getText().equals("VALIDADO") ){
                    u.setState(ValidationResult.VALIDATE);
                }
                else if( eTmp.getText().equals("VALIDADO_TEMPORAL") ){
                    u.setState(ValidationResult.TEMPORAL_VALIDATE);
                }
                else{
                    u.setState(ValidationResult.DENIED);
                }
            }
            else if(eTmp.getAttributeValue("name").equals("generacion")){
                u.setCreatedDate(new Date(Long.parseLong(eTmp.getText())));
            }
            else if(eTmp.getAttributeValue("name").equals("actividad")){
                u.setActivityDate(new Date(Long.parseLong(eTmp.getText())));
            }
        }

        //roles
        e=r.getChild("roles");
        l=e.getChildren("role");
        Role rTmp;
        Permission p;
        for(Element eTmp: l){
            rTmp=new Role(eTmp.getAttributeValue("id"), eTmp.getAttributeValue("name"));
            l2=eTmp.getChildren("permission");
            for(Element eTmp2: l2){
                p=new Permission(eTmp2.getAttributeValue("name"));
                p.setValue(eTmp2.getText());
                p.setDescription(eTmp2.getAttributeValue("description"));
                rTmp.agregarPermiso(p);
            }
            u.addRole(rTmp);
        }

        //propiedades
        e=r.getChild("properties");
        l=e.getChildren("property");
        String cTmp;
        ArrayList<String> lTmp;
        for(Element eTmp: l){
            cTmp=eTmp.getAttributeValue("name");
            lTmp=new ArrayList<String>();
            l2=eTmp.getChildren("value");
            for(Element eTmp2: l2){
                lTmp.add(eTmp2.getText());
            }
            u.setProperty(cTmp, lTmp);
        }

        //permisos
        e=r.getChild("permissions");
        l=e.getChildren("permission");
        for(Element eTmp: l){
            p=new Permission(eTmp.getAttributeValue("name"));
            p.setValue(eTmp.getText());
            p.setDescription(eTmp.getAttributeValue("description"));
            u.agregarPermiso(p);
        }

        if( exito ){
            return u;
        }
        else{
            return null;
        }
    }

    /**
     * Genera una estructura de XML que representa a la entidad.
     * @return Cadena con en XML.
     */
    public String toStringXml(){
        XMLOutputter xml = new XMLOutputter();
        ByteArrayOutputStream out=new ByteArrayOutputStream();

        xml.setFormat(Format.getPrettyFormat());
        try{
            xml.output(this.toXml(), out);
            out.flush();
            out.close();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }

        return out.toString();
    }
    
    public void updateId(String id){
        this.id=id;
    }
    
//-------------------------------------------------------------- metodos protegidos  ----------------------------------------------------------------------------------


    /**
     * Genera la estructura del usuario en un xml.
     * @param raiz Nodo a partir del cual se genera la estructura.
     */
    protected void defineXmlStructure(Element raiz){
        Element nodo, nodo2, nodo3;

        //para id
        nodo=new Element("atributte");
        nodo.setAttribute("name", "id");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setAttribute("id", "true");
        nodo.setText(this.id);
        raiz.addContent(nodo);

        //para nombre
        nodo=new Element("atributte");
        nodo.setAttribute("name", "nombre");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.nombre);
        raiz.addContent(nodo);

        //para user
        nodo=new Element("atributte");
        nodo.setAttribute("name", "user");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.user);
        raiz.addContent(nodo);

        //para mail
        nodo=new Element("atributte");
        nodo.setAttribute("name", "mail");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.mail);
        raiz.addContent(nodo);

        //para tipoTerminal
        nodo=new Element("atributte");
        nodo.setAttribute("name", "tipoTerminal");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.tipoTerminal.toString());
        raiz.addContent(nodo);

        //para origen
        nodo=new Element("atributte");
        nodo.setAttribute("name", "origen");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.origen);
        raiz.addContent(nodo);

        //para terminal
        nodo=new Element("atributte");
        nodo.setAttribute("name", "terminal");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.terminal);
        raiz.addContent(nodo);

        //para estado
        nodo=new Element("atributte");
        nodo.setAttribute("name", "estado");
        nodo.setAttribute("type", "java.lang.String");
        nodo.setText(this.estado.toString());
        raiz.addContent(nodo);

        //para generacion
        if( this.generacion!=null){
            nodo=new Element("atributte");
            nodo.setAttribute("name", "generacion");
            nodo.setAttribute("type", "java.util.Date");
            nodo.setText(""+this.generacion.getTime());
            raiz.addContent(nodo);
        }

        //para actividad
        nodo=new Element("atributte");
        nodo.setAttribute("name", "actividad");
        nodo.setAttribute("type", "java.util.Date");
        nodo.setText(""+this.actividad.getTime());
        raiz.addContent(nodo);

        //para roles
        nodo=new Element("roles");
        raiz.addContent(nodo);
        for(Role r: this.roles){
            nodo2=new Element("role");
            nodo2.setAttribute("id", r.getId());
            nodo2.setAttribute("name", r.getName());
            for(Permission p: r.getPermissions()){
                nodo3=new Element("permission");
                nodo3.setAttribute("name", p.getName());
                nodo3.setAttribute("description", p.getDescription());
                nodo3.setText(p.getValue());
                nodo2.addContent(nodo3);
            }
            nodo.addContent(nodo2);
        }

        //para propiedades
        nodo=new Element("properties");
        raiz.addContent(nodo);
        Iterator<String> iTmp=this.propiedades.keySet().iterator();
        String cTmp;
        List<String> lTmp;
        while( iTmp.hasNext() ){
            cTmp=iTmp.next();
            lTmp=this.propiedades.get(cTmp);
            nodo2=new Element("propertie");
            nodo2.setAttribute("name", cTmp);
            for(String cTmp2: lTmp){
                nodo3=new Element("value");
                nodo3.setText(cTmp2);
            }
            nodo.addContent(nodo2);
        }

        //para permisos
        nodo=new Element("permissions");
        raiz.addContent(nodo);
        for(Permission p: this.permisos){
            nodo2=new Element("permission");
            nodo2.setAttribute("name", p.getName());
            nodo2.setAttribute("description", p.getDescription());
            nodo2.setText(p.getValue());
            nodo.addContent(nodo2);
        }
    }
}
