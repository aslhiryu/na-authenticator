package neoAtlantis.utilidades.accessController.resourcesFilter;

import java.io.*;
import java.util.*;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.Resource;
import neoAtlantis.utilidades.accessController.objects.ResourceType;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Validador de Acceso a Recursos operado a traves de un XML, cual debe apegarse 
 * a la siguiente estructura:
 * <br>
 * <pre>
 * &lt;pagesAccess&gt;
 *   &lt;directory name="<i>nombre_del_directorio</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_el_directorio</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_el_directorio</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/directory&gt;
 *   &lt;page name="<i>nombre_de_la_p&aacute;gina</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_la_p&aacute;gina</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_la p&aacute;gina</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/page&gt;
 *   &lt;resource name="<i>recurso</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_el_recurso</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_el_recurso</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/resource&gt;
 * &lt;/pagesAccess&gt;
 * </pre>
 * Donde se pueden agregar tantos elementos de <i>directory</i>, <i>page</i> y <i>
 * resource</i> sean necesarios.<br><br>
 * Por default a los recursos de <i>directory</i> y <i>page</i> se les incorpora 
 * una / al inicio, para denotar busque el elemento a partir del contexto de la 
 * aplicaci&oacute;n.<br><br>
 * Por default a los recursos de <i>resource</i> se les incorpora un * al inicio, 
 * para denotar busque todas las coincidencias del elemento.<br><br>
 * Se pueden incorporar tantos elementos <i>rol</i> y <i>permission</i> sean necesarios,
 * en el momento que el validador encuentre al menos un rol y permiso asignado al 
 * usuario permitir&aacute; el acceso al recurso.<br><br>
 * Se pueden utilizar de comodin el rol <b>USUARIO</b> y el permiso <b>LOGEADO</b>
 * dado que estos valores los asigna el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n} 
 * a todos los usuarios al momento de iniciar sesi&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class XmlResourceAccess extends ResourceAccessAllower {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(XmlResourceAccess.class);

    /**
     * Genera un ResourceAccessAllower por XML.
     * @param xml Ruta del archivo que contienen las reglas de acceso en XML
     * @throws WayConfigurationException 
     */
    public XmlResourceAccess(String xml) throws WayConfigurationException{
        this(false, xml);
    }

    /**
     * Genera un ResourceAccessAllower por XML.
     * @param restrictivo trus si desea que el acceso a los recursos sea restrictivo
     * @param xml Ruta del archivo que contienen las reglas de acceso en XML
     * @throws WayConfigurationException 
     */
    public XmlResourceAccess(boolean restrictivo, String xml) throws WayConfigurationException{
        super(restrictivo);

        //validamos la existencia del archivo de configuracion
        File fTmp=new File(xml);
        if( fTmp.exists() ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(xml);
                this.recursos=cargaRecursos(doc);
            }
            catch(Exception ex){
                throw new WayConfigurationException(ex);
            }
        }
        else{
            throw new WayConfigurationException("No se ha definido la configuración para el Acceso a los recursos.");
        }
    }

    /**
     * Valida el acceso a un recurso.
     * @param user Usuario que intenta acceder al recurso
     * @param recurso Recurso a validar
     * @return Estus del acceso
     * @throws WayAccessException 
     */
    @Override
    public AccessEstatus validaAcceso(User user, String recurso) {
        //revisa que el recurso tenga permisos y roles requeridos
        logger.debug("Valida los permisos de acceso del recurso: "+recurso+", en modo "+(this.restrictivo? "Restrictivo": "Permisivo")+", con el usuario: "+user.getUser());
        for(int i=0; i<this.recursos.size(); i++){
            //valida si es un recurso
            if( this.recursos.get(i).getTipo()==ResourceType.RESOURCE && recurso.endsWith(this.recursos.get(i).getNombre()) ){
                logger.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validaPermisos(user, this.recursos.get(i));
            }
            else if( this.recursos.get(i).getTipo()==ResourceType.DIRECTORY && recurso.startsWith("/"+this.recursos.get(i).getNombre()) ){
                logger.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validaPermisos(user, this.recursos.get(i));
            }
            else if( this.recursos.get(i).getTipo()==ResourceType.PAGE && recurso.equals("/"+this.recursos.get(i).getNombre()) ){
                logger.debug("Regla encontrada: "+this.recursos.get(i));
                return this.validaPermisos(user, this.recursos.get(i));
            }
        }

        return AccessEstatus.NO_ENCONTRADO;
    }

    //--------------------------------------------------------------------------
    
    /**
     * Valida los permisos del recurso
     * @param user Usuario que desea acceder al recurso
     * @param recurso Recurso a validar
     * @return Estatus del cceso al recurso
     */
    protected AccessEstatus validaPermisos(User user, Resource recurso){
        boolean rolPer=false;
        boolean perPer=false;

        //valida que se tenga el rol y el permiso
        rolPer = user.perteneceAlgunRol(recurso.getRolesPermitidos());
        perPer = user.tieneAlgunPermiso(recurso.getPermisosPermitidos());
        logger.debug("Permisos de usuario: " + perPer);
        logger.debug("Permisos de rol: " + rolPer);
        if (perPer && rolPer) {
            return AccessEstatus.VALIDO;
        } else {
            return AccessEstatus.DENEGADO;
        }
    }
    
    /**
     * Carga la configuraci´&oacute;n de los accesos a partir de un XML
     * @param config Documento raiz XML
     * @return  Lista de recursos
     */
    protected List<Resource> cargaRecursos(Document config){
        ArrayList recs=new ArrayList<Resource>();
        Element raiz = config.getRootElement();
        List<Element> lTmp, perms;
        Resource r;
        ResourceType tipo;

        //logger.debug("Obtengo el listado de recursos.");
        lTmp=raiz.getChildren();
        for(Element eTmp: lTmp){
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
            //logger.debug(r.toString());
        }
        //logger.debug(recs.size()+" recursos cargados.");

        return recs;
    }
}
