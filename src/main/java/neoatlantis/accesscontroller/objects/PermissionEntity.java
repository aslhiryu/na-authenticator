package neoatlantis.accesscontroller.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Define una entidad con permisos
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.1
 */
public class PermissionEntity implements Serializable{
    protected ArrayList<Permission> permisos;

    /**
     * Genera una entidad con permisos
     */
    public PermissionEntity(){
        this.permisos = new ArrayList<Permission>();
    }

    
    
    /**
     * Agrega un permiso a la entidad
     * @param permiso Nombre del permiso
     */
    public void agregarPermiso(Permission permiso) {
        if( permiso==null ){
            return;
        }

        this.permisos.add(permiso);
    }

    /**
     * Verifica si tiene activo un permiso
     * @param permiso Permiso a validar
     * @return true si esta activo (para validar si esta activo revisa si el valor del permiso coincide con 'activo', 'ok', 'yes', 'si' o 'true')
     */
    public boolean validaPermiso(Permission permiso) {
        for(Permission p: this.permisos){
            if( p.getName().equals(permiso.getName()) ){
                if ( p!=null && (p.getValue().equalsIgnoreCase("ACTIVO") || p.getValue().equalsIgnoreCase("OK") ||
                        p.getValue().equalsIgnoreCase("YES") || p.getValue().equalsIgnoreCase("SI") ||
                        p.getValue().equalsIgnoreCase("TRUE") || p.getValue().equalsIgnoreCase("1") ||
                        p.getValue().equalsIgnoreCase("ACTIVE")) ) {
                    return true;
                }

                break;
            }
        }

        return false;
    }

    /**
     * Verifica si tiene activo un permiso
     * @param permiso Nombre del permiso
     * @return true si esta activo (para validar si esta activo revisa si el valor del permiso coincide con 'activo', 'ok', 'yes', 'si' o 'true')
     */
    public boolean validatePermission(String permiso) {
        if( permiso==null ){
            return false;
        }

        return this.validaPermiso(new Permission(permiso));
    }

    /**
     * Obtiene el valor dado a un permiso
     * @param permiso Permiso a obtener
     * @return valor del permiso
     */
    public String obtienePermiso(Permission permiso) {
        for(Permission p: this.permisos){
            if( p.getName().equals(permiso.getName()) ){
                return p.getValue();
            }
        }

        return null;
    }

    /**
     * Obtiene el valor dado a un permiso
     * @param permiso Nombre del permiso
     * @return valor del permiso
     */
    public String getPermission(String permiso) {
        return obtienePermiso(new Permission(permiso));
    }

    /**
     * Genera la informac&oacute;n de la entidad.
     * @return Informaci&oacute;n de la entidad
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append("//--------------- Permisos ---------------//").append(System.getProperty("line.separator"));
        for(Permission p: this.permisos){
            sb.append("\t").append(p.getName()).append(": ").append(p.getValue()).append(System.getProperty("line.separator"));
        }
        sb.append("//---------------------------------------//").append(System.getProperty("line.separator"));

        return sb.toString();
    }

    /**
     * Genera una cadena con un nombre limpio de caracteres no validos.
     * @param nombre Cadena con el nombre
     * @return Nombre sin caracteres validos
     */
    public static String limpiaNombre(String nombre){
        return nombre.toUpperCase().replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U').replace(' ', '_');
    }
}
