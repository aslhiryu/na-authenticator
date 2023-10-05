/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package neoAtlantis.utilidades.ctrlAcceso;

import java.util.ArrayList;
import java.util.List;
import org.jdom.*;

/**
 * Definici&oacute;n de un recurso de sistema.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class Recurso {
    private String nombre;
    private ArrayList<String>rolesPermitidos;
    private ArrayList<String>permisosPermitidos;

    /**
     * Genera un Recurso indicando con la posibilidad de activar como permitidos el permiso 'logeado' y el rol 'usuarios'.
     * @param nombre Nombre del recurso
     * @param permisoLogeado true si se desea activar el permiso 'logeado' como valido
     * @param rolUsuario true si se desea activar el rol 'usuarios' como valido
     */
    public Recurso(String nombre, boolean permisoLogeado, boolean rolUsuario){
        this.nombre=nombre;
        this.permisosPermitidos=new ArrayList();
        this.rolesPermitidos=new ArrayList();
        if( permisoLogeado ){
            this.permisosPermitidos.add("LOGEADO");
        }
        if( rolUsuario ){
            this.rolesPermitidos.add("USUARIOS");
        }
    }

    /**
     * Genera un Recurso indicando al permiso 'logeado' como permitido y con la posibilidad de activar al rol 'usuarios' de igual manera.
     * @param nombre Nombre del recurso
     * @param rolUsuario true si se desea activar el rol 'usuarios' como valido
     */
    public Recurso(String nombre, boolean rolUsuario){
        this(nombre, true, rolUsuario);
    }

    /**
     * Genera un Recurso indicando al permiso 'logeado' y al rol 'usuarios' como permitidos.
     * @param nombre
     */
    public Recurso(String nombre){
        this(nombre, true, true);
    }

    /**
     * Agrega un rol a la lista de permitidos para el recurso.
     * @param rol Nombre del rol
     */
    public void agregaRolPermitido(String rol){
        this.rolesPermitidos.add(rol.toUpperCase());
    }

    /**
     * Agrega un permiso a la lista de permitidos para el recurso.
     * @param permiso Nombre del permiso
     */
    public void agregaPermisoPermitido(String permiso){
        this.permisosPermitidos.add(permiso.toUpperCase());
    }

    /**
     * Valida si un rol es permitido para el recurso.
     * @param rol Nombre del rol
     * @return true si es permitido
     */
    public boolean isRolPermitido(String rol){
        return this.rolesPermitidos.contains(rol.toUpperCase());
    }

    /**
     * Valida si un permiso es permitido para el recurso.
     * @param permiso Nombre del permiso
     * @returntrue si es permitido
     */
    public boolean isPermisoPermitido(String permiso){
        return this.permisosPermitidos.contains(permiso.toUpperCase());
    }

    /**
     * Recupera los permisos permitidos para el recurso.
     * @return Arreglo de permisos permitidos
     */
    public String[] getPermisosPermitidos(){
        String[] c=new String[this.permisosPermitidos.size()];

        for(int i=0; i<this.permisosPermitidos.size(); i++){
            c[i]=this.permisosPermitidos.get(i);
        }

        return c;
    }

    /**
     * Recupera el nombre del recurso.
     * @return Nombre del recurso
     */
    public String getNombre(){
        return this.nombre;
    }

    /**
     * Recupera los roles permitidos para el recurso.
     * @return Arreglo de roles permitidos
     */
    public String[] getRolesPermitidos(){
        String[] c=new String[this.rolesPermitidos.size()];

        for(int i=0; i<this.rolesPermitidos.size(); i++){
            c[i]=this.rolesPermitidos.get(i);
        }

        return c;
    }

    /**
     * Genera la informac&oacute;n del recurso.
     * @return Informaci&oacute;n del recurso
     */
    @Override
    public String toString(){
        StringBuffer sb=new StringBuffer();

        sb.append("|------------------------------  Recurso(").append(this.nombre).append(")  -------------------------------|\n");
        sb.append("Permisos Permitidos: ").append(this.permisosPermitidos).append("\n");
        sb.append("Roles Permitidos: ").append(this.rolesPermitidos).append("\n");
        sb.append("|-----------------------------------------------------------------------|\n");

        return sb.toString();
    }

    /**
     * Parsea un archivo XML a una colecci&oacute;n de recursos.
     * <br><br>
     * El archivo debe de cumplir con la siguiente estructura:<br>
     * <pre>
     * <b>&lt;recursos&gt;</b>
     *     <b>&lt;recurso nombre="nombre_del_recurso"&gt;</b>
     *         <b>&lt;permisos&gt;</b>
     *             <b>&lt;permiso&gt;</b><i>permiso_permitido_para_el_recurso</i><b>&lt;/permiso&gt;</b>
     *         <b>&lt;/permisos&gt;</b>
     *         <b>&lt;roles&gt;</b>
     *             <b>&lt;rol&gt;</b><i>rol_permitido_para_el_recurso</i><b>&lt;/rol&gt;</b>
     *         <b>&lt;/roles&gt;</b>
     *     <b>&lt;/recurso&gt;</b>
     * <b>&lt;/recursos&gt;</b>
     * </pre>
     * @param doc Documento XML
     * @return Colecci&oacute;n de recursos obtenidos
     * @throws java.lang.Exception
     */
    public static ArrayList<Recurso> parseaArchivoRecursos(Document doc) throws Exception {
        Element raiz = doc.getRootElement();
        ArrayList<Recurso> lTmp=new ArrayList<Recurso>();
        Recurso r;

        if (raiz != null && raiz.getName().equalsIgnoreCase("recursos")) {
            List<Element> perms, hojas = raiz.getChildren("recurso");
            for(int i=0; hojas!=null&&i<hojas.size(); i++){
                r=new Recurso(hojas.get(i).getAttributeValue("nombre"), false, false);
                perms=hojas.get(i).getChild("roles").getChildren("rol");
                for(int j=0; perms!=null&&j<perms.size(); j++){
                    r.agregaRolPermitido( perms.get(j).getText() );
                }
                perms=hojas.get(i).getChild("permisos").getChildren("permiso");
                for(int j=0; perms!=null&&j<perms.size(); j++){
                    r.agregaPermisoPermitido( perms.get(j).getText() );
                }
                lTmp.add(r);
            }
        }

        return lTmp;
    }

}
