package neoatlantis.accesscontroller.resourceAccessAllower;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.Resource;
import neoatlantis.accesscontroller.objects.ResourceType;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.resourceAccessAllower.interfaces.AccessEstatus;
import neoatlantis.accesscontroller.resourceAccessAllower.interfaces.DenialType;
import neoatlantis.accesscontroller.resourceAccessAllower.utils.ValidatorConfigurator;
import neoatlantis.accesscontroller.web.listeners.BlockerSessionListener;
import neoatlantis.applications.web.listeners.SessionListener;
import neoatlantis.applications.web.objects.UserRequest;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author desarrollo.alberto
 */
public class XmlRoleResourceAccess extends XmlSessionResourceAccess {
    private static final Logger DEBUGER = Logger.getLogger(XmlRoleResourceAccess.class);


    
    
    
    
    // Contructores ------------------------------------------------------------
    
    public XmlRoleResourceAccess(String xml) throws WayConfigurationException{
        super(false, xml);
    }
    
    public XmlRoleResourceAccess(boolean restrictivo, String xml) throws WayConfigurationException{
        super(restrictivo, xml);
    }

    
    
    
    // Metodos publicos---------------------------------------------------------
    
    /**
     * Valida el acceso a un recurso.
     * @param user Usuario que intenta acceder al recurso
     * @param recurso Recurso a validar
     * @return Estus del acceso
     */
    @Override
    public AccessEstatus validateAccess(User user, String recurso) {
        //revisa que el recurso tenga permisos y roles requeridos
        DEBUGER.debug("Valida los permisos de acceso del recurso: "+recurso+", en modo "+(this.restrictivo? "Restrictivo": "Permisivo")+", con el usuario: "+user.getUser());
        for(int i=0; i<this.recursos.size(); i++){
            //valida si es un recurso
            if( this.recursos.get(i).getTipo()==ResourceType.RESOURCE && recurso.endsWith(this.recursos.get(i).getNombre()) ){
                DEBUGER.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validatePermissions(user, this.recursos.get(i));
            }
            else if( this.recursos.get(i).getTipo()==ResourceType.DIRECTORY && recurso.startsWith("/"+this.recursos.get(i).getNombre()) ){
                DEBUGER.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validatePermissions(user, this.recursos.get(i));
            }
            else if( this.recursos.get(i).getTipo()==ResourceType.PAGE && recurso.equals("/"+this.recursos.get(i).getNombre()) ){
                DEBUGER.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validatePermissions(user, this.recursos.get(i));
            }
        }

        return AccessEstatus.NOT_FOUND;
    }

    /**
     * M&eacute;todo que se ejecuta cada vez que se solicita un recurso. Durante 
     * este realiza la validación de acceso y en caso de no contar con los privilegios 
     * suficiontes redireciona al recurso mapeado en <b>home</b>. De igual manera
     * valida la existencia del usuario y en caso de no existir redirecciona al 
     * recurso redir.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     * @param chain Flujo de seguimiento del filtro
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        UserRequest peticion;
        DEBUGER.debug("Valida el acceso al recurso.");
        
        BlockerSessionListener.endInactiveSessions((HttpServletRequest)request, -1);        
        DenialType validacion=ValidatorConfigurator.validateResourceAccess(this.excs, this.redir, this.home, this, (HttpServletRequest)request, (HttpServletResponse)response);

        switch(validacion){
            case NOT_CONNECTED: {
                this.sessionExpired(request, response);
                break;
            }
            case NOT_ALLOWED: {
                this.accessDenied(request, response);
                break;
            }
            case LOGIN_ALLOWED: {
                this.existActiveSession(request, response);
                break;
            }
            case RESTRICTIVE: {
                this.accessDenied(request, response);
                break;
            } 
            default: {
                //valido si existe una pagina en cookie para redirecionar
                peticion=SessionListener.getLastRequest((HttpServletRequest)request);
                if( peticion!=null ){
                    SessionListener.clearLastRequest((HttpServletRequest)request);
                    if( !peticion.isPost() ){
                        DEBUGER.debug("Existe la ultima visita, direciona hacia esta");
                        this.goToLastAccess(peticion, request, response);
                    }
                    else{
                        chain.doFilter(request, response);
                    }
                }
                else{
                    chain.doFilter(request, response);
                }
            }
        }
    }

    
    
    
    
    //Metodos protegidos--------------------------------------------------------

    /**
     * Prepara todo el entorno para que trabaje el validador.
     */
    @Override
    protected void createEnvironment(InputStream xml) throws Exception{
        //si no se ha configurado realizo la misma
        if(!configurado){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(xml);
                this.excs = ValidatorConfigurator.getExceptions(doc.getRootElement());
                this.redir=ValidatorConfigurator.getRedirect(doc.getRootElement());
                this.home=ValidatorConfigurator.getHome(doc.getRootElement());
                this.mens=ValidatorConfigurator.getSessionMessage(doc.getRootElement());
                this.mensPer=ValidatorConfigurator.getPermissionMessage(doc.getRootElement());
                //this.pingTime=ValidatorConfigurator.getPingTime(doc);
                this.recursos=loadResources(doc.getRootElement());

                DEBUGER.debug("Validador configurado como restrictivo: "+this.isRestrictive());
            }
            catch(Exception ex){
                DEBUGER.error("Error al cargar el archivo de configuración de validator '"+home+"'.", ex);
            }
            
            
            this.configurado=true;
        }                
    }
    
    /**
     * Valida los permisos del recurso
     * @param user Usuario que desea acceder al recurso
     * @param recurso Recurso a validar
     * @return Estatus del cceso al recurso
     */
    protected AccessEstatus validatePermissions(User user, Resource recurso){
        boolean rolPer;
        boolean perPer;

        //valida que se tenga el rol y el permiso
        rolPer = user.hasAnyRole(recurso.getRolesPermitidos());
        perPer = user.hasAnyPermission(recurso.getPermisosPermitidos());
        DEBUGER.debug("Permisos de usuario: " + perPer);
        DEBUGER.debug("Permisos de rol: " + rolPer);
        if (perPer && rolPer) {
            return AccessEstatus.VALIDATE;
        } else {
            return AccessEstatus.DENIED;
        }
    }
    
    /**
     * Carga la configuraci´&oacute;n de los accesos a partir de un XML
     * @param config Documento raiz XML
     * @return  Lista de recursos
     */
    protected List<Resource> loadResources(Element root){
        ArrayList<Resource> recs=new ArrayList<Resource>();
        List<Element> perms;
        Resource r;
        ResourceType tipo;
        Element eTmp;

        //valido que exista la definicion de los recursos
        if(root.getChild("resourcesPermissions")!=null){
            for(int i=0; root.getChild("resourcesPermissions").getChildren()!=null&&i<root.getChild("resourcesPermissions").getChildren().size(); i++){
                eTmp=(Element)root.getChild("resourcesPermissions").getChildren().get(i);
                
                //valida el tipo de recurso
                if( eTmp.getName().equalsIgnoreCase("page") ){
                    tipo=ResourceType.PAGE;                
                }
                else if( eTmp.getName().equalsIgnoreCase("resource") ){
                    tipo=ResourceType.RESOURCE;                
                }
                else if( eTmp.getName().equalsIgnoreCase("directory") ){
                    tipo=ResourceType.DIRECTORY;                
                }
                else{
                    continue;
                }

                if( eTmp.getAttribute("name")==null || eTmp.getAttributeValue("name").length()<2 ){
                    throw new RuntimeException("No se definio adecuadamente el nombre del recurso en el archivo XML.");
                }
                r=new Resource(eTmp.getAttributeValue("name"), false, false, tipo);

                //cargo los roles del recurso
                perms=(eTmp.getChild("roles")!=null? eTmp.getChild("roles").getChildren("role"): null);
                for(Element eTmp2: perms){
                    r.agregaRolPermitido( eTmp2.getText() );
                }

                //cargo los permisos del recurso
                perms=(eTmp.getChild("permissions")!=null? eTmp.getChild("permissions").getChildren("permission"): null);
                for(Element eTmp2: perms){
                    r.agregaPermisoPermitido( eTmp2.getText() );
                }

                recs.add(r);
            }
        }
        
        DEBUGER.debug("Recursos configurados: ");
        for(Resource rec: recs){
            DEBUGER.debug(rec);
        }

        return recs;
    }
    
}
