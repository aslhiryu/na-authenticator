package neoAtlantis.utilidades.accessController.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.allower.NullAllower;
import neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay;
import neoAtlantis.utilidades.accessController.audit.NullAudit;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.audit.interfaces.LevelAudit;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.blocker.MemoryBlocker;
import neoAtlantis.utilidades.accessController.blocker.NullBlocker;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockType;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.captcha.*;
import neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter;
import neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode;
import neoAtlantis.utilidades.accessController.cipher.*;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.objects.PeticionesCaptcha;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.NullProfiler;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utilidades.accessController.scheduler.GeneralScheduler;
import neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay;
import neoAtlantis.utilidades.configFiles.ClassGenerator;
import neoAtlantis.utilidades.statistics.StatictisMotor;
import neoAtlantis.utilidades.statistics.interfaces.Statistical;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Escuchador que realiza la carga y configuraci&oacute;n del {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * en un entorno web, para su posterior uso en el sistema. La configuración la realiza 
 * a partir de lo descrito en el archivo <i>WEB-INF/classes/config/configAccess.xml</i>.<br><br>
 * Se puede personalizar el nombre y ubicación del archivo a travez de un <i>context-param</i>
 * dentro del archivo <i>web.xml</i>, para lo cual se debe utilizar como <i>param-name</i>
 * la clave <b>configAccessNA</b> y como <i>param-value</i> la ubicación y nombre 
 * del archivo.<br><br>
 * Dentro del valor del parametro se pueden utilizar los siguientes comodines:<br>
 * <ul>
 * <li>%HOME_WEB%    = ruta del directorio ra&iacute;z de la aplicaci&oacute;n</li>
 * <li>%HOME_WEBINF% = ruta del directorio WEB-INF de la aplicaci&oacute;n</li>
 * <li>%HOME_CLASS%  = ruta del directorio classes de la aplicaci&oacute;n</li>
 * </ul>
 * <pre>
 *     Ej.
 *         &lt;context-param&gt;
 *           &lt;param-name&gt;<i>configAccessNA</i>&lt;/param-name&gt;
 *           &lt;param-value&gt;<i>%HOME_WEBINF%/autenticacion.xml</i>&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 * </pre>
 * La estructura del archivo debe ser la siguiente:
 * <pre>
 * &lt;accessController attempts="<i>numero_de_intentos</i>" multiple="<i>soporte_de_usuario_multiple</i>"&gt;
 *   &lt;authentication  login="<i>pagina_de_logeo</i>" sessionPing="<i>tiempo_de_validacion_de_sesion</i>"&gt;
 *     &lt;home&gt;<i>pagina_de_inicio</i>&lt;/home&gt;
 *     &lt;exit&gt;<i>mensaje_de_finalizacion</i>&lt;exit&gt;
 *     &lt;validation restrictive="<i>tipo_de_validacion</i>"&gt;
 *       &lt;validator class="<i>clase_del_validador_de_acceso_a_recursos</i>"&gt;
 *         &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" /&gt;
 *         &lt;exceptions&gt;
 *           &lt;page&gt;<i>pagina_a_omitir</i>&lt;/page&gt;
 *           &lt;directory&gt;<i>directorio_a_omitir</i>&lt;/directory&gt;
 *           &lt;resource&gt;<i>recurso_a_omitir</i>&lt;/resource&gt;
 *         &lt;/exceptions&gt;
 *       &lt;/validator&gt;
 *     &lt;/validation&gt;
 *   &lt;/authentication&gt;
 *   &lt;authenticationWay class="<i>medio_autenticador</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/authenticationWay&gt;
 *   &lt;profilerWay class="<i>medio_perfilador</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/profilerWay&gt;
 *   &lt;auditWay class="<i>medio_bitacorador</i>" level="<i>nivel_de_bitacora</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/auditWay&gt;
 *   &lt;allowerWay class="<i>medio_permisor</i>" level=""&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/allowerWay&gt;
 *   &lt;blockerWay class="<i>medio_de_bloqueo</i>" type="<i>tipo_de_bloqueo</i>" timeBlocked="<i>tiempo_de_bloqueo</i>" sessionLife="<i>tiempo_de_sesion</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/blockerWay&gt;
 *   &lt;schedulerWay class="<i>medio_calendarizador</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/schedulerWay&gt;
 *   &lt;cipher class="<i>cifrador_de_datos</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/cipher&gt;
 *   &lt;captcha class="<i>dibujador_de_captchas</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/captcha&gt;
 *   &lt;confirmationCode class="<i>generador_de_codigos</i>"&gt;
 *     &lt;param name="<i>nombre_del_parametro</i>" value="<i>valor_del_parametro</i>" type="<i>tipo_de_parametro</i>" /&gt;
 *   &lt;/confirmationCode&gt;
 *   &lt;messages/&gt;
 *     &lt;deniedOwner&gt;<i>mensaje_personalizado</i>&lt;/deniedOwner&gt;
 *     &lt;permission&gt;<i>mensaje_permiso_denegado</i>&lt;/permission&gt;
 *     &lt;blocked&gt;<i>mensaje_de_usuario_bloqueado</i>&lt;/blocked&gt;
 *     &lt;exceeded&gt;<i>mensjae_de_intentos_excedidos</i>&lt;/exceeded&gt;
 *     &lt;error&gt;<i>mensaje_de_error_en_autenticacion</i>&lt;/error&gt;
 *     &lt;session&gt;<i>mensaje_de_sesion_vencida</i>&lt;/session&gt;
 *     &lt;connected&gt;<i>mensaje_de_usuario_conectado</i>&lt;/connected&gt;
 *     &lt;denied&gt;<i>mensaje_de_credenciales_invalidas</i>&lt;/denied&gt;
 *     &lt;inactive&gt;<i>mensaje_de_usuario_inactivo</i>&lt;/inactive&gt;
 *     &lt;temporal&gt;<i>mensaje_de_acceso_temporal</i>&lt;/temporal&gt;
 *     &lt;noFound&gt;<i>mensaje_de_usuario_inexistente</i>&lt;/noFound&gt;
 *     &lt;outTime&gt;<i>mensaje_de_fuera_de_tiempo</i>&lt;/outTime&gt;
 *     &lt;captcha&gt;<i>mensaje_de_error_en_captcha</i>&lt;/captcha&gt;
 *     &lt;expires&gt;<i>mensaje_de_usuario_expirado</i>&lt;/expires&gt;
 *     &lt;methodGet&gt;<i>mensaje_de_metodo_get</i>&lt;/methodGet&gt;
 *   &lt;/messages&gt;
 * &lt;/accessController&gt;
 * </pre>
 * <i>numero_de_intentos= </i> N&uacute;mero que determina la cantidad de intentos
 * que se pueden realizar antes de que se bloquee la cuenta. El bloqueo se efectua 
 * con lo determinado en el parametro <i>type</i> del <i>blockerWay</i>. (Opcional.
 * En caso de no definirse por default se asigna el valor de '3')<br>
 * <br>
 * <i>soporte_de_usuario_multiple=</i> Permite los valores 'true' o 'false' y determina
 * la posibilidad de que un mismo usuario se conecte simultaneamente mas de 1 vez
 * al sistema. (Opcional. En caso de no definirse por default se asigna el valor 
 * de 'false')<br>
 * <br>
 * <i>pagina_de_logeo=</i> Recurso al que se redirecionar&acute; para solicitar las 
 * credenciales de acceso. Esto sucede cuando expira la sesi&oacute;n de un usuario.  
 * (Opcional. En caso de no definirse por default se asigna el valor de 'login.html')<br>
 * <br>
 * <i>tiempo_de_validacion_de_sesion=</i> N&uacute;mero que determina la cantidad 
 * de segundos que espera, para notificar que la sesi&oacute;n esta activa. Si se 
 * asigna el valor de '-1' se deshabilita esta opci&oacute;n. (Opcional. En caso 
 * de no definirse por default se asigna el valor de '-1')<br>
 * <br>
 * <i>pagina_de_inicio=</i> Recurso al que se redirecionar&aacute; una vez que se ha autenticado
 * corractamente un usuario. Adicionalemnte tambien se redirecciona a este recurso
 * cuando se intenta acceder a un recurso sin los suficientes privilegios. (Opcional. 
 * En caso de no definirse por default se asigna el valor de 'index.html')<br>
 * <br>
 * <i>mensaje_de_finalizacion=</i> Mensaje que se utiliza cuando se finaliza la sesi&oacute;n 
 * del usuario. (Opcional. En caso de no definirse por default se asigna el valor 
 * de 'Sesi&oacute;n finalizada.')<br>
 * <br>
 * <i>tipo_de_validacion=</i> Determina la forma de acceso a los recursos, permite los 
 * valores de 'true' o 'false', en caso de ser 'true' se impide al acceso a todos 
 * los recursos que no tengan definida una regla de acceso. Cuando este parametro se 
 * configura con 'false' permitira el acceso a todos los recursos de la aplicaci&oacute;n
 * a no ser que se haya definido una regla de acceso especifica al recurso. (Opcional. 
 * En caso de no definirse por default se asigna el valor de 'true')<br>
 * <br>
 * <i>clase_del_validador_de_acceso_a_recursos=</i> Clase que definir&aacute; 
 * el acceso a los recursos. Debe de extender de {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower}
 * (La definici&oacute;n del <i>validator</i> es opcional. En caso de no definirse por default se asigna el objeto
 * {@link neoAtlantis.utilidades.accessController.resourcesFilter.NullResourceAccess}).<br>
 * <br>
 * <i>medio_autenticador=</i> Clase que define el tipo de autenticaci&oacute;n a seguir.
 * Debe de extender de {@link neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay}.
 * (La definici&oacute;n del <i>authenticationWay</i> es obligatoria).<br>
 * <br>
 * <i>medio_perfilador=</i> Clase permite la carga de perfiles para los usuarios.
 * Debe de extender de {@link neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay}. 
 * (La definici&oacute;n del <i>profilerWay</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.profiler.NullProfiler}).<br>
 * <br>
 * <i>medio_bitacorador=</i> Clase que capta y procesa las solicitudes para bitacorar.
 * Debe de extender de {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay}. 
 * (La definici&oacute;n del <i>auditWay</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.audit.NullAudit}).<br>
 * <br>
 * <i>nivel_de_bitacora=</i> Determina el tipo de evento que procesara el {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay}. 
 * (Opcional. En caso de no definirse por default se asigna el valor de 'access')
 * Este se puede definir como:
 * <ul>
 * <li>null: No procesa ningun tipo de evento.</li>
 * <li>access: Procesa unicamente los eventos de login y logout.</li>
 * <li>basic: Procesa todos los eventos a excepci&oacute;n de los intentos de accesos
 * a los recursos y los propios del negocio.</li>
 * <li>bussiness: Procesa los mismos evento que basic y adicionalmente los que se 
 * hallan definido en el negocio.</li>
 * <li>all: Procesa todos los eventos.</li>
 * </ul>
 * <i>medio_permisor=</i> Clase permite la carga de permisos para los usuarios.
 * Debe de extender de {@link neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay}. 
 * (La definici&oacute;n del <i>allowerWay</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.allower.NullAllower}).<br>
 * <br>
 * <i>medio_de_bloqueo=</i> Clase permite controla las sesiones y bloqueos activos 
 * en el sistema. Debe de extender de {@link neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay}. 
 * (La definici&oacute;n del <i>blockerWay</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.blocker.MemoryBlocker}).<br>
 * <br>
 * <i>tipo_de_bloqueo=</i> Determina la manera que controlara los bloqueos de los 
 * usuarios. (Opcional. En caso de no definirse por default se asigna el valor de 
 * 'user'). Permite los valores:
 * <ul>
 * <li>user: Registra el nombre del usuario y ya no le permite el acceso
 * independientemente de la terminal desde donde lo intente.</li>
 * <li>ip: Registra la ip y ya no le permite el acceso.</li>
 * independientemente del usuario que intente ingresar desde dicho equipo.</li>
 * </ul>
 * <i>tiempo_de_bloqueo=</i> Determina el n&uacute;mero de minutos que ya no se le
 * permitira el acceso al sistema, cuando un usuario o ip son bloqueados. (Opcional. 
 * En caso de no definirse por default se asigna el valor de '1440', equivalente a un d&iacute;a).<br>
 * <br>
 * <i>tiempo_de_sesion=</i> Determina el n&uacute;mero de minutos que dura una sesi&oacute;n 
 * de un usuario conectado en el sistema. (Opcional. En caso de no definirse por 
 * default se asigna el valor de '10').<br>
 * <br>
 * <i>medio_calendarizador=</i> Clase determina el calendario y horarios para acceder 
 * al sistema. Debe de extender de {@link neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay}.
 * (La definici&oacute;n del <i>schedulerWay</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.scheduler.GeneralScheduler}).<br>
 * <br>
 * <i>cifrador_de_datos=</i> Clase que controla el cifrado de los datos utilizados 
 * para cuestiones como contrase&ntilde;as. Debe de extender de {@link neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher}.
 * (La definici&oacute;n del <i>cipher</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.cipher.CipherMd5Des}).<br>
 * <br>
 * <i>dibujador_de_captchas=</i> Clase que controla el la forma en que se generan 
 * los captchas. Debe de extender de {@link neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter}.
 * (La definici&oacute;n del <i>captcha</i> es opcional. En caso de no definirse 
 * por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.captcha.PointLineCaptcha}).<br>
 * <br>
 * <i>generador_de_codigos=</i> Clase que controla el la forma en que se generan 
 * los c&oacute;digos de confirmaci&oacute;n que despliega el sistema. Debe de extender 
 * de {@link neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode}.
 * (La definici&oacute;n del <i>confirmationCode</i> es opcional. En caso de no 
 * definirse por default se asigna el objeto {@link neoAtlantis.utilidades.accessController.captcha.BasicConfirmationCode}).<br>
 * <br>
 * <i>mensaje_personalizado=</i> Mensaje que se utiliza cuando una post-validaci&oacute;n 
 * personalizada falla.(Opcional. En caso de no definirse por default se asigna 
 * el valor de 'Acceso denegado.').<br>
 * <br>
 * <i>mensaje_permiso_denegado=</i> Mensaje que se utiliza cuando se intenta acceder 
 * a un recurso y no se tienen los privilegios suficientes expecificados por el 
 * {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower}.
 * (Opcional. En caso de no definirse por default se asigna el valor de 'Sin privilegios 
 * suficientes.').<br>
 * <br>
 * <i>mensaje_de_usuario_bloqueado=</i> Mensaje que se utiliza cuando el usuario 
 * que intenta acceder esta bloqueado. (Opcional. En caso de no definirse por default 
 * se asigna el valor de 'El usuario esta baneado.').<br>
 * <br>
 * <i>mensaje_de_intentos_excedidos=</i> Mensaje que se utiliza cuando se ha excedido 
 * el nu&uacute;mero de intentos espableciados en el parametro <i>attempts</i>. 
 * (Opcional. En caso de no definirse por default se asigna el valor de 'Se ha revasado 
 * el numero de intentos permitidos, la cuenta sera baneada.').<br>
 * <br>
 * <i>mensaje_de_error_en_autenticacion=</i> Mensaje que se utiliza cuando sucede 
 * un problema en el proceso de autenticaci&oacute;n. (Opcional. En caso de no definirse 
 * por default se asigna el valor de 'Error al autenticar.').<br>
 * <br>
 * <i>mensaje_de_sesion_vencida=</i> Mensaje que se utiliza cuando el tiempo de 
 * vida de la sesi&oacute;n del usuario se ha sobrepasado. (Opcional. En caso de 
 * no definirse por default se asigna el valor de 'Su sesión ha expirado.').<br>
 * <br>
 * <i>mensaje_de_usuario_conectado=</i> Mensaje que se utiliza cuando el usuario 
 * que esta intentando acceder, esta conectado y la opci&oacute; <i>multiple</i>
 * tiene el valor de 'false'. (Opcional. En caso de no definirse por default se 
 * asigna el valor de 'El usuario ya esta conectado.').<br>
 * <br>
 * <i>mensaje_de_credenciales_invalidas=</i> Mensaje que se utiliza cuando la contrase&ntilde;a 
 * del usuario no es valida. (Opcional. En caso de no definirse por default se asigna 
 * el valor de 'Acceso denegado.').<br>
 * <br>
 * <i>mensaje_de_usuario_inactivo=</i> Mensaje que se utiliza cuando el usuario 
 * que intenta acceder al sistema no esta activo en el sistema. (Opcional. En caso 
 * de no definirse por default se asigna el valor de 'El usuario esta deshabilitado.').<br>
 * <br>
 * <i>mensaje_de_acceso_temporal=</i> Mensaje que se utiliza cuando se permite el 
 * acceso al sistema, pero el usuario requiere modificar su contraseña. (Opcional. 
 * En caso de no definirse por default se asigna el valor de 'Acceso temporal, se 
 * requiere modificar la contraseña.').<br>
 * <br>
 * <i>mensaje_de_usuario_inexistente=</i> Mensaje que se utiliza cuando el usuario 
 * que intenta acceder no existe en el sistema. (Opcional. En caso de no definirse 
 * por default se asigna el valor de 'El usuario no es valido.').<br>
 * <br>
 * <i>mensaje_de_fuera_de_tiempo=</i> Mensaje que se utiliza cuando se esta intentado 
 * acceder en una fecha u horario no debidos. Esto esta definidor por el {@link neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay Medio Calendarizador}.
 * (Opcional. En caso de no definirse por default se asigna el valor de 'No esta 
 * permitido el acceso en este horario.').<br>
 * <br>
 * <i>mensaje_de_error_en_captcha=</i> Mensaje que se utiliza cuando se incorpora 
 * el captcha y no es correctamente proporcionado por el usuario. (Opcional. En 
 * caso de no definirse por default se asigna el valor de 'La clave de confirmaci&oacute;n 
 * no es correcta.').<br>
 * <br>
 * <i>mensaje_de_usuario_expirado=</i> Mensaje que se utiliza cuando al momento de 
 * autenticar el usuario se detecta la fecha de vigencia de la cuenta se ha sobrepasado.
 * (Opcional. En caso de no definirse por default se asigna el valor de 'El usuario 
 * esta caduco.').<br>
 * <br>
 * <i>mensaje_de_metodo_get=</i> Mensaje que se utiliza cuando se utiliza algun servlet 
 * en el proceso de autenticaci&oacute;n y se manda llamar a travez del metodo GET. 
 * (Opcional. En caso de no definirse por default se asigna el valor de 'No esta 
 * permitido el metodo 'get' para autenticar.').
 * <pre>
 *     Ej. 
 *         {@link neoAtlantis.utilidades.accessController.utils.SimpleAuthenticationServlet}<br>
 * </pre>
 * En el caso de la definici&oacute; de las clases se pueden proporcionar parametros 
 * de configuraci&oacute;n los cuales se utilizan para generar la instancia de la 
 * clase, estos parametros deben de ir en el order que los recibe el constructor.<br>
 * <br>
 * <i>nombre_del_parametro=</i> Nombre para el parametro, este nombre es simboliuco 
 * y solo sirve de apoyo para ubicar el parametro. (Obligatorio).<br>
 * <br>
 * <i>valor_del_parametro=</i> Valor que se le asigna al parametro. (Obligatorio).<br>
 * <br>
 * <i>tipo_de_parametro=</i> Define el como se manejar&aacute; el parametro. 
 * (Opcional. En caso de no definirse por default se asigna el valor de 'string').
 * Se pueden trabajar con los siguientes tipos:
 * <ol>
 * <li>string: Trabaja con el valor del parametro como si se tratara de una cadena.</li>
 * <li>int: Trabaja con el valor del parametro como si se tratara de un entero.</li>
 * <li>appContext: En este caso no importa lo que se haya colocado en '<i>value</i>'
 * el parametro tomar&aacute; como valor el contexto de la aplicaci&oacute;n.</li>
 * <li>props: Cuando se define este tipo, se asume que el contenido de '<i>value</i>'
 * es la ubicación de un archivo de {@link java.util.Properties}.</li>
 * </ol>
 * Dentro del valor del parametro se pueden utilizar los siguientes comodines:<br>
 * <ul>
 * <li>%HOME_WEB%    = ruta del directorio ra&iacute;z de la aplicaci&oacute;n</li>
 * <li>%HOME_WEBINF% = ruta del directorio WEB-INF de la aplicaci&oacute;n</li>
 * <li>%HOME_CLASS%  = ruta del directorio classes de la aplicaci&oacute;n</li>
 * </ul>
 * En caso de que el constructor no reciba nada, no se debe de especificar ningun 
 * parametro.
 * <pre>
 *     Ej.
 *         &lt;cipher class="<i>cifrador_de_datos</i>"/&gt;
 * </pre>
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.6
 */
public class AccessControllerPublisher implements HttpSessionListener{//ServletContextListener{
    /**
     * Logeador del objeto
     */
    private static final Logger logger = Logger.getLogger(AccessControllerPublisher.class);

    /**
     * Clave del objeto en sesi&oacute;n que almacena al {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
     */
//    public static String CLAVE_CTRL="na.util.access.AccessCtrl";
    /**
     * Clave del objeto en sesi&oacute;n que almacena al {@link neoAtlantis.utilidades.accessController.objects.User Usuario Autenticado}
     */
    public static String CLAVE_USER="na.util.access.User";
    /**
     * Clave del objeto en sesi&oacute;n que almacena al {@link neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter Dibujador de Captchas}
     */
    public static String CLAVE_CAPTCHA="na.util.access.Captcha";
    /**
     * Clave del objeto en sesi&oacute;n que almacena al {@link neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode Generador de C&oacute;digos}
     */
    public static String CLAVE_CODE="na.util.access.CodeConfimation";
    /**
     * Clave del objeto en request que almacena los mensajes que envia el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
     */
    public static String CLAVE_MENSAJE="na.util.access.MessageText";
    /**
     * 
     */
    public static String CLAVE_PETITIONS="na.util.access.Captchas";
    /**
     * Clave del objeto en el contexto de la aplicaci&oacute;n que almacena las conexiones activas de los usuarios
     */
    public static String CLAVE_CONECTIONS="na.util.access.Conections";
    /**
     * Clave del objeto en el contexto de la aplicaci&oacute;n que almacena las bloqueos activos de los usuarios
     */
    public static String CLAVE_BLOCKS="na.util.access.Blocks";

    private static String homeWeb;
    private static String homeWebInf;
    private static HashMap<String, Object> entorno=new HashMap<String, Object>();

    private AccessController access;
    //private List<User> conexiones;
    //private List<User> bloqueos;

    /**
     * Metodo llamado al momneto de generar la sesi&oacute;n, en este se realiza 
     * toda la carga de la configuraci&oacute;n.
     * @param hse  Sesi&oacute;n web
     */
    @Override
    public void sessionCreated(HttpSessionEvent hse) {
    //public void contextInitialized(ServletContextEvent hse) {      
        List<User> conexiones;
        List<User> bloqueos;
        
        //valida si ya existe el control configurado
        if( AccessController.getInstance()==null ){
            logger.debug("Configuro control de autenticacion");
            
            //configuro los home
            homeWeb=hse.getSession().getServletContext().getRealPath("/").replace('\\', '/')+"/";
            homeWebInf=homeWeb+"WEB-INF/";
            Properties com=new Properties();
            com.setProperty("homeWeb", homeWeb);
            com.setProperty("homeWebInf", homeWebInf);
            com.setProperty("homeClass", homeWebInf+"classes/");

            String home=homeWebInf+"config/configAccess.xml";

            //genero la coleccion de peticiones
            if( hse.getSession().getServletContext().getAttribute(CLAVE_PETITIONS)==null ){
                logger.debug("Inicializo las peticiones de Catpcha");
                hse.getSession().getServletContext().setAttribute(CLAVE_PETITIONS, new PeticionesCaptcha());
            }
            //genero los elemento del MemoryBlocker
            if( hse.getSession().getServletContext().getAttribute(CLAVE_CONECTIONS)==null ){
                logger.debug("Inicializo las sesiones del MemoryBlocker");
                conexiones=Collections.synchronizedList(new ArrayList<User>());
                hse.getSession().getServletContext().setAttribute(CLAVE_CONECTIONS, conexiones);
            }
            else{
                conexiones=(List<User>)hse.getSession().getServletContext().getAttribute(CLAVE_CONECTIONS);
            }
            if( hse.getSession().getServletContext().getAttribute(CLAVE_BLOCKS)==null ){
                logger.debug("Inicializo los bloqueos del MemoryBlocker");
                bloqueos=Collections.synchronizedList(new ArrayList<User>());
                hse.getSession().getServletContext().setAttribute(CLAVE_BLOCKS, bloqueos);
            }
            else{
                bloqueos=(List<User>)hse.getSession().getServletContext().getAttribute(CLAVE_BLOCKS);
            }

            //reviso si existe una configuracion personalizada
            if( hse.getSession().getServletContext().getInitParameter("configAccessNA")!=null && hse.getSession().getServletContext().getInitParameter("configAccessNA").length()>2 ){
                home=ClassGenerator.parseaComodinesConfig(hse.getSession().getServletContext().getInitParameter("configAccessNA"), com);
            }

            //genero el entorno
            this.entorno.put("appContext", hse.getSession().getServletContext());
            this.entorno.put("sessionContext", hse.getSession());
            this.entorno.put("homeWeb", this.homeWeb);

            logger.info("Configuracion cargada de :"+home);
            //validamos la existencia del archivo de configuracion
            File fTmp=new File(home);
            if( fTmp.exists() ){
                try{
                    //parseo el archivo XML
                    Document doc=(new SAXBuilder(false)).build(home);
                    AuthenticationWay aut=creaAutenticador(doc);
                    DataCipher cip=creaCifrador(doc);
                    ProfilerWay per=creaPerfilador(doc);
                    AllowerWay perm=creaPermisor(doc);
                    BlockerWay blo=creaBloqueador(doc, conexiones, bloqueos, hse.getSession());
                    AuditWay bit=creaBitacora(doc);
                    SchedulerWay cal=creaCalendario(doc);
                    boolean multiple=obtieneMultiple(doc);
                    int intentos=obtieneIntentos(doc);

    //                this.access=new AccessController(aut, cip, per, perm,  blo, bit, cal);
                    this.access=AccessController.getInstance(aut, cip, per, perm,  blo, bit, cal);
                    this.access.setUsuarioMultiple(multiple);
                    this.access.setMaxIntentos(intentos);

                    //cargo las variables de aplicacion
    //                hse.setAttribute(CLAVE_CTRL, this.access);
                    if( hse.getSession().getServletContext().getAttribute(CLAVE_CAPTCHA)==null ){
                        hse.getSession().getServletContext().setAttribute(CLAVE_CAPTCHA, creaCaptcha(doc));
                    }
                    if( hse.getSession().getServletContext().getAttribute(CLAVE_CODE)==null ){
                        hse.getSession().getServletContext().setAttribute(CLAVE_CODE, creaCodigo(doc));
                    }
                    if( hse.getSession().getServletContext().getAttribute(CLAVE_MENSAJE)==null ){
                        hse.getSession().getServletContext().setAttribute(CLAVE_MENSAJE, this.obtieneMensajesError(doc));
                    }

                    //valido si existen estadisticas
                    if( hse.getSession().getServletContext().getAttribute(StatictisMotor.APP_STATISTICS)==null ){
                        this.iniciaEstaditicas(doc, hse.getSession().getServletContext());
                    }
                }
                catch(Exception ex){
                    logger.error("Error al cargar el archivo de configuración de access '"+home+"'.", ex);
                }
            }
            else{
                throw new RuntimeException("No se ha definido la configuración para el Control de Acceso.");
            }
        }
    }

    /**
     * Metodo llamado al momneto de destruir la sesi&oacute;n, en este se realiza 
     * la destrucción de los valores almacenados en <b>CLAVE_USER</b>, <b>CLAVE_MENSAJE</b>
     * y <b>CLAVE_CTRL</b>
     * @param hse Sesi&oacute;n web
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
    //public void contextDestroyed(ServletContextEvent hse) {
        StatictisMotor sm=(StatictisMotor)hse.getSession().getServletContext().getAttribute(StatictisMotor.APP_STATISTICS);
        
        if( sm!=null ){
            sm.detiene();
            sm=null;
        }
        
        User u=(User)hse.getSession().getAttribute(CLAVE_USER);
        logger.info("Intenta finalizar el usuario '"+u+"'.");
        hse.getSession().removeAttribute(CLAVE_USER);

        if( u!=null ){
            try{
                this.access.finaliza(u);                
            }
            catch(Exception ex){
                logger.error("Error al finalizar al usuario '"+u+"'.", ex);
            }
        }
        
    }

    //--------------------------------------------------------------------------

    private AuthenticationWay creaAutenticador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("authenticationWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        if(obj!=null){
            return (AuthenticationWay)obj;
        }
        else{
            throw new RuntimeException("No se logro definir la clase del autenticador.");
        }
    }

    private ProfilerWay creaPerfilador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("profilerWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new NullProfiler(): (ProfilerWay)obj);
    }

    private AllowerWay creaPermisor(Document config){
        return new NullAllower();
    }

    private BlockerWay creaBloqueador(Document config, List<User> conexiones, List<User> bloqueos, HttpSession sesion){
        Element e, raiz = config.getRootElement();
        Object obj=null;
        BlockerWay blo;

        e=raiz.getChild("blockerWay");

        //revisa si utiliza el MemoryBlocker
        if( e==null || e.getAttribute("class")==null ){
            logger.debug("Se genera el MemoryBlocker");
            blo=new MemoryBlocker(conexiones, bloqueos);
            blo.setModoBloqueo(BlockType.USUARIO);
            blo.setTiempoBloqueo(30*BlockerWay.MINUTO_EN_MILIS);

            return blo;
        }
        //valida si se genera MemoryBlocker desde config
        else if( e.getAttributeValue("class").equals("neoAtlantis.utilidades.accessController.blocker.MemoryBlocker") ){
            logger.debug("Se genera el MemoryBlocker con configuracion");
            obj=new MemoryBlocker(conexiones, bloqueos);
        }
        else{
            //si no utiliza el MemoryBlocker genero el especificado
            obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);
        }

        if(obj==null){
            blo=new NullBlocker();
        }
        else{
            blo=(BlockerWay)obj;

            //recupero el tipo
            if( e.getAttribute("type")!=null ){
                if( e.getAttributeValue("type").equalsIgnoreCase("ip") ){                    
                    blo.setModoBloqueo(BlockType.IP);
                }
                else if( e.getAttributeValue("type").equalsIgnoreCase("user") ){
                    blo.setModoBloqueo(BlockType.USUARIO);
                }
                else{
                    throw new RuntimeException("El valor de 'type' en 'blockerWay' solo puede ser 'ip' o 'user'.");
                }
                logger.debug("Genero bloqueador con tipo de bloqueo="+e.getAttributeValue("type"));
            }

            //recupero el tiempo de bloqueo
            if( e.getAttribute("timeBlocked")!=null ){
                try{
                    blo.setTiempoBloqueo(Math.abs(Integer.parseInt(e.getAttributeValue("timeBlocked"))*BlockerWay.MINUTO_EN_MILIS));
                    logger.debug("Genero bloqueador con tiempo de bloqueo="+e.getAttributeValue("timeBlocked")+" mins");
                }
                catch(Exception ex1){
                    throw new RuntimeException("El valor de 'timeBlocked' en 'blockerWay' solo entero positivo.");
                }
            }

            //ya no se toma en cuenta el atributo, siempre va a tomar por default el valor de la sesion del contexto
            //recupero el tiempo de sesion
            /*if( e.getAttribute("sessionLife")!=null ){
                try{
                    blo.setTiempoSesion(Math.abs(Integer.parseInt(e.getAttributeValue("sessionLife"))*BlockerWay.MINUTO_EN_MILIS));
                    logger.debug("Genero bloqueador con vida de sesion="+e.getAttributeValue("sessionLife")+" mins");
                }
                catch(Exception ex1){
                    throw new RuntimeException("El valor de 'sessionLife' en 'blockerWay' solo entero positivo.");
                }
            }*/
            blo.setTiempoSesion( sesion.getMaxInactiveInterval()*1000 );
            
        }

        return blo;
    }

    private AuditWay creaBitacora(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;
        AuditWay bit;

        e=raiz.getChild("auditWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        if(obj==null){
            bit=new NullAudit();
        }
        else{
            bit=(AuditWay)obj;

            //recupero el tipo
            if( e.getAttribute("level")!=null ){
                if( e.getAttributeValue("level").equalsIgnoreCase("null") ){                    
                    bit.setNivelBitacora(LevelAudit.NULA);
                }
                else if( e.getAttributeValue("level").equalsIgnoreCase("access") ){
                    bit.setNivelBitacora(LevelAudit.ACCESO);
                }
                else if( e.getAttributeValue("level").equalsIgnoreCase("basic") ){
                    bit.setNivelBitacora(LevelAudit.BASICA);
                }
                else if( e.getAttributeValue("level").equalsIgnoreCase("bussiness") ){
                    bit.setNivelBitacora(LevelAudit.NEGOCIO);
                }
                else if( e.getAttributeValue("level").equalsIgnoreCase("all") ){
                    bit.setNivelBitacora(LevelAudit.COMPLETA);
                }
                else{
                    throw new RuntimeException("El valor de 'level' en 'auditWay' solo puede ser 'access',  'null', 'basic', 'bussiness' o 'all'.");
                }
                logger.debug("Genero bitacora con nivel="+e.getAttributeValue("level"));
            }
        }
            
        return bit;
    }

    private SchedulerWay creaCalendario(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("schedulerWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new GeneralScheduler(): (SchedulerWay)obj);
    }

    private ConfirmationCode creaCodigo(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("confirmationCode");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new BasicConfirmationCode(): (ConfirmationCode)obj);
}

    private CaptchaPainter creaCaptcha(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("captcha");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new PointLineCaptcha(): (CaptchaPainter)obj);
    }

    private DataCipher creaCifrador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("cipher");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new CipherMd5Des("default"): (DataCipher)obj);
    }

    private boolean obtieneMultiple(Document config){
        boolean b=false;
        Element raiz = config.getRootElement();

        if(raiz.getAttribute("multiple")==null){
            return b;
        }

        try{
            b=(new Boolean(raiz.getAttributeValue("multiple"))).booleanValue();
        }
        catch(Exception ex){
            throw new RuntimeException("El valor de 'multiple' en 'accessController' solo puede ser 'true' o 'false'.");
        }

        return b;
    }

    private int obtieneIntentos(Document config){
        int i=3;
        Element raiz = config.getRootElement();

        if(raiz.getAttribute("attempts")==null){
            return i;
        }

        try{
            i=Integer.parseInt(raiz.getAttributeValue("attempts"));
            i=Math.abs(i);
        }
        catch(Exception ex){
            throw new RuntimeException("El valor de 'attempts' en 'accessController' solo puede ser entero positivo.");
        }

        return i;
    }

    private void iniciaEstaditicas(Document config, ServletContext context){
        Element e, raiz = config.getRootElement();
        Object obj;
        Statistical s;
        int t=15;
        
        e=raiz.getChild("statistics");
        
        if( e!=null ){
            logger.debug("Si existen estadisticas, las inicia.");
            StatictisMotor sm=StatictisMotor.getInstance();
            context.setAttribute(StatictisMotor.APP_STATISTICS, sm);
            
            try{
                t=Integer.parseInt( e.getAttributeValue("interval") );
            }catch(Exception ex){}
            
            obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);
            if(obj!=null){
                s=(Statistical)obj;
                s.setTime(t);
                sm.addStatistical(s);
                logger.debug("Genera Estadisticas tipo '"+s.getClass().getName()+"' con intervalo de "+s.getTime()+".");
            }
        }
    }

    private Map<String, String> obtieneMensajesError(Document config){
        Element e, e2,raiz = config.getRootElement();
        HashMap<String,String> m=new HashMap<String,String>();

        //defino los mensajes
        e=raiz.getChild("messages");
        
        if(e==null){
                m.put("methodGet", "No esta permitido el metodo 'get' para autenticar.");
                m.put("error", "Error al autenticar.");
                m.put("blocked", "El usuario esta baneado.");
                m.put("expires", "El usuario esta caduco.");
                m.put("denied", "Acceso denegado.");
                m.put("deniedOwner", "Acceso denegado.");
                m.put("connected", "El usuario ya esta conectado.");
                m.put("captcha", "La clave de confirmación no es correcta.");
                m.put("outTime", "No esta permitido el acceso en este horario.");
                m.put("inactive", "El usuario esta deshabilitado.");
                m.put("exceeded", "Se ha revasado el numero de intentos permitidos, la cuenta sera baneada.");
                m.put("noFound", "El usuario no es valido.");
                m.put("temporal", "Acceso temporal, se requiere modificar la contraseña.");
                m.put("noUser", "Se requiere el usuario.");
        }
        else{
            e2=e.getChild("methodGet");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("methodGet", e2.getText());
            }
            else{
                m.put("methodGet", "No esta permitido el metodo 'get' para autenticar.");
            }
            e2=e.getChild("error");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("error", e2.getText());
            }
            else{
                m.put("error", "Error al autenticar.");
            }
            e2=e.getChild("blocked");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("blocked", e2.getText());
            }
            else{
                m.put("blocked", "El usuario esta baneado.");
            }
            e2=e.getChild("expires");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("expires", e2.getText());
            }
            else{
                m.put("expires", "El usuario esta caduco.");
            }
            e2=e.getChild("denied");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("denied", e2.getText());
            }
            else{
                m.put("denied", "Acceso denegado.");
            }
            e2=e.getChild("deniedOwner");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("deniedOwner", e2.getText());
            }
            else{
                m.put("deniedOwner", "Acceso denegado.");
            }
            e2=e.getChild("connected");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("connected", e2.getText());
            }
            else{
                m.put("connected", "El usuario ya esta conectado.");
            }
            e2=e.getChild("captcha");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("captcha", e2.getText());
            }
            else{
                m.put("captcha", "La clave de confirmación no es correcta.");
            }
            e2=e.getChild("outTime");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("outTime", e2.getText());
            }
            else{
                m.put("outTime", "No esta permitido el acceso en este horario.");
            }
            e2=e.getChild("inactive");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("inactive", e2.getText());
            }
            else{
                m.put("inactive", "El usuario esta inhabilitado.");
            }
            e2=e.getChild("exceeded");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("exceeded", e2.getText());
            }
            else{
                m.put("exceeded", "Se ha revasado el numero de intentos permitidos, la cuenta sera baneada.");
            }
            e2=e.getChild("noFound");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("noFound", e2.getText());
            }
            else{
                m.put("noFound", "El usuario no es valido.");
            }
            e2=e.getChild("temporal");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("temporal", e2.getText());
            }
            else{
                m.put("temporal", "Acceso temporal, se requiere modificar la contraseña.");
            }
            e2=e.getChild("noUser");
            if( e2!=null && e2.getText().length()>0 ){
                m.put("noUser", e2.getText());
            }
            else{
                m.put("noUser", "Se requiere el usuario.");
            }
        }

        return m;
    }
}
