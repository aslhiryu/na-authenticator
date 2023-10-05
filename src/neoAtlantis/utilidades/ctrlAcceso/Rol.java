package neoAtlantis.utilidades.ctrlAcceso;

import neoAtlantis.utilidades.ctrlAcceso.interfaces.EntidadConPermisos;

/**
 * Definici&oacute;n de un rol de sistema.
 * @version 2.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class Rol extends EntidadConPermisos {

    /**
     * Versi&oacute;n de la clase
     */
    public static final String version = "2.1";
    private String id;
    private String nombre;

    /**
     * Genera un Rol.
     * @param rol Nombre del rol
     */
    public Rol(String rol){
        this(rol, rol);
    }

    public Rol(String id, String rol){
        this.nombre=this.limpiaNombre(rol);
        this.id=id.replace(' ', '_');
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
     * Genera la informac&oacute;n del rol.
     * @return Informac&oacute;n del rol
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("");

        sb.append("/***********************  ROL  **********************//\n");
        sb.append("ID: ").append(this.id).append("\n");
        sb.append("Nombre: ").append(this.nombre).append("\n");
        sb.append(super.toString());
        sb.append("/****************************************************//\n");

        return sb.toString();
    }
}