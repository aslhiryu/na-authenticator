package neoatlantis.accesscontroller.resourceAccessAllower.interfaces;

/**
 * Enumeraci&oacute;n que define los tipos de acceso a los recursos.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum DenialType {
    NOT_CONNECTED,
    NOT_ALLOWED,
    RESTRICTIVE,
    ALLOWED,
    LOGIN_ALLOWED
}