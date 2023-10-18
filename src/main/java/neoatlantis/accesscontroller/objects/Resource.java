package neoAtlantis.utils.accessController.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Definici&oacute;n de un recurso de sistema.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class Resource implements Serializable{
    private ResourceType tipo=ResourceType.PAGE;
    private String nombre;
    private ArrayList<Role>rolesPermitidos;
    private ArrayList<Permission>permisosPermitidos;

    /**
     * Genera un Recurso indicando con la posibilidad de activar como permitidos 
     * el permiso 'logeado' y el rol 'usuarios'.
     * @param nombre Nombre del recurso
     * @param permisoLogeado true si se desea activar el permiso 'logeado' como valido
     * @param rolUsuario true si se desea activar el rol 'usuarios' como valido
     * @param tipo Tipo de recurso
     */
    public Resource(String nombre, boolean permisoLogeado, boolean rolUsuario, ResourceType tipo){
        if(nombre==null || nombre.length()==0){
            throw new RuntimeException("No se puede generar un recurso sin nombre.");
        }

        this.tipo=tipo;
        if( nombre.startsWith("/") ){
            this.nombre=nombre.substring(1);
        }
        else{
            this.nombre=nombre;
        }
        this.permisosPermitidos=new ArrayList<Permission>();
        this.rolesPermitidos=new ArrayList<Role>();
        if( permisoLogeado ){
            this.permisosPermitidos.add(new Permission("LOGGED"));
        }
        if( rolUsuario ){
            this.rolesPermitidos.add(new Role("USERS"));
        }
    }
    
    /**
     * Genera un Recurso indicando con la posibilidad de activar como permitidos 
     * el permiso 'logeado' y el rol 'usuarios'.
     * @param nombre Nombre del recurso
     * @param permisoLogeado true si se desea activar el permiso 'logeado' como valido
     * @param rolUsuario true si se desea activar el rol 'usuarios' como valido
     */
    public Resource(String nombre, boolean permisoLogeado, boolean rolUsuario){
        this(nombre, permisoLogeado, rolUsuario, ResourceType.PAGE);
    }

    /**
     * Genera un Recurso indicando al permiso 'logeado' como permitido y con la posibilidad de activar al rol 'usuarios' de igual manera.
     * @param nombre Nombre del recurso
     * @param rolUsuario true si se desea activar el rol 'usuarios' como valido
     */
    public Resource(String nombre, boolean rolUsuario){
        this(nombre, true, rolUsuario);
    }

    /**
     * Genera un Recurso indicando al permiso 'logeado' y al rol 'usuarios' como permitidos.
     * @param nombre
     */
    public Resource(String nombre){
        this(nombre, true, true);
    }

    /**
     * Agrega un rol a la lista de permitidos para el recurso.
     * @param rol Rol a agregar
     */
    public void agregaRolPermitido(Role rol){
        this.rolesPermitidos.add(rol);
    }

    /**
     * Agrega un rol a la lista de permitidos para el recurso.
     * @param rol Nombre del rol a agregar
     */
    public void agregaRolPermitido(String rol){
        agregaRolPermitido(new Role(rol));
    }

    /**
     * Agrega un permiso a la lista de permitidos para el recurso.
     * @param permiso Permiso a agregar
     */
    public void agregaPermisoPermitido(Permission permiso){
        this.permisosPermitidos.add(permiso);
    }

    /**
     * Agrega un permiso a la lista de permitidos para el recurso.
     * @param permiso Nombre del permiso a agregar
     */
    public void agregaPermisoPermitido(String permiso){
        agregaPermisoPermitido(new Permission(permiso));
    }

    /**
     * Valida si un rol es permitido para el recurso.
     * @param rol Rol a validar
     * @return true si es permitido
     */
    public boolean isRolPermitido(Role rol){
        for(Role r: this.rolesPermitidos){
            if( r.getName().equals(rol.getName()) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Valida si un rol es permitido para el recurso.
     * @param rol Nombre del rol a validar
     * @return  true si es permitido
     */
    public boolean isRolPermitido(String rol){
        return isRolPermitido(new Role(rol));
    }

    /**
     * Valida si un permiso es permitido para el recurso.
     * @param permiso Permiso a validar
     * @return true si es permitido
     */
    public boolean isPermisoPermitido(Permission permiso){
        for(Permission p: this.permisosPermitidos){
            if( p.getName().equals(permiso.getName()) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Valida si un permiso es permitido para el recurso.
     * @param permiso Nombre del permiso a validar
     * @return  true si es permitido
     */
    public boolean isPermisoPermitido(String permiso){
        return isPermisoPermitido(new Permission(permiso));
    }

    /**
     * Recupera los permisos permitidos para el recurso.
     * @return Arreglo de permisos permitidos
     */
    public String[] getPermisosPermitidos(){
        String[] c=new String[this.permisosPermitidos.size()];

        for(int i=0; i<this.permisosPermitidos.size(); i++){
            c[i]=this.permisosPermitidos.get(i).getName();
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
     * Recupera el tipo de recurso.
     * @return Tipo de recurso
     */
    public ResourceType getTipo(){
        return this.tipo;
    }
    
    /**
     * Recupera los roles permitidos para el recurso.
     * @return Arreglo de roles permitidos
     */
    public String[] getRolesPermitidos(){
        String[] c=new String[this.rolesPermitidos.size()];

        for(int i=0; i<this.rolesPermitidos.size(); i++){
            c[i]=this.rolesPermitidos.get(i).getName();
        }

        return c;
    }

    /**
     * Genera la informac&oacute;n del recurso.
     * @return Informaci&oacute;n del recurso
     */
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();

        sb.append("|------------------------------  Recurso(").append(this.nombre).append(")  -------------------------------|\n");
        sb.append("Tipo: ").append(this.tipo).append("\n");
        sb.append("Permisos Permitidos: ").append(this.permisosPermitidos).append("\n");
        sb.append("Roles Permitidos: ").append(this.rolesPermitidos).append("\n");
        sb.append("|-----------------------------------------------------------------------|\n");

        return sb.toString();
    }
}
