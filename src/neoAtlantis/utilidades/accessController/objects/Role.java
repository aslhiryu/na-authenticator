package neoAtlantis.utilidades.accessController.objects;

import java.util.List;

/**
 * Definic´&oacute;n de un rol en el sistema.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class Role extends PermissionEntity {
    private String id;
    private String nombre;

  /**
     * Genera un Rol.
     * @param rol Nombre del rol
     */
    public Role(String rol){
        this(rol, rol);
    }

    /**
     * Genera un Rol.
     * @param id Identificador del rol
     * @param rol  Nombre del rol
     */
    public Role(String id, String rol){
        if(rol==null || rol.length()==0){
            throw new RuntimeException("No se puede generar un rol sin nombre.");
        }

        this.nombre=PermissionEntity.limpiaNombre(rol);
        this.id=PermissionEntity.limpiaNombre(id);
    }

    /**
     * Obtiene el identificador del rol
     * @return Identificador asignado al rol
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre del rol
     * @return Nombre asignado al rol
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Asigna un identificador al rol
     * @param id Identificador a asignar
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Asigna un nombre al rol
     * @param nombre Nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Recupera los permisos asignados al rol.
     * @return Lista de permisos
     */
    public List<Permission> getPermisos(){
        return this.permisos;
    }

    /**
     * Genera la informac&oacute;n del rol.
     * @return Informac&oacute;n del rol
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append("/***********************  ROL  **********************//").append(System.getProperty("line.separator"));
        sb.append("ID: ").append(this.id).append(System.getProperty("line.separator"));
        sb.append("Nombre: ").append(this.nombre).append(System.getProperty("line.separator"));
        sb.append(super.toString());
        sb.append("/****************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }
}
