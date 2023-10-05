package test;

import java.util.List;
import neoAtlantis.utilidades.accessController.cipher.CipherSha1;
import javax.naming.Context;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import neoAtlantis.utilidades.accessController.objects.User;
import java.util.HashMap;
import java.util.Properties;
import neoAtlantis.utilidades.accessController.authentication.LoginLDAPAutentication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class ProbadorLDAP {
    User uTmp;
    
    public ProbadorLDAP() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void pruebaLDAP() throws Exception{
        Properties config=new Properties();        
        config.setProperty("url", "ldap://delfos.cisen.gob:389");
        config.setProperty("base", "dc=cisen,dc=gob");
        config.setProperty("bindType", "activeDirectory");
        config.setProperty("dataUser", "sAMAccountName");
        config.setProperty("dataMail", "mail");
        config.setProperty("dataName", "displayName");
        
        HashMap data=new HashMap();
        data.put("user", "u11451");
        data.put("pass", "MercurY.1359");
        
        LoginLDAPAutentication aut=new LoginLDAPAutentication(config);
        
        User u=aut.autenticaUsuario(data);
        System.out.println(u);
    }
    
    @Test
    public void busquedaLDAP() throws Exception{
        Properties config=new Properties();        
        config.setProperty("url", "ldap://delfos.cisen.gob:389");
        config.setProperty("base", "dc=cisen,dc=gob");
        config.setProperty("bindType", "activeDirectory");
        config.setProperty("dataUser", "sAMAccountName");
        config.setProperty("dataMail", "mail");
        config.setProperty("dataName", "displayName");
        
        HashMap data=new HashMap();
        data.put("user", "u11451");
        data.put("pass", "MercurY.1359");
        
        LoginLDAPAutentication aut=new LoginLDAPAutentication(config);
        
        User u=aut.autenticaUsuario(data);
        
        data=new HashMap();
        data.put("cadenaBusqueda", "intell");
        List<User> l=aut.buscaUsuarios(data);
        System.out.println(l.size());
        for(User uTmp: l){
            System.out.println("- "+uTmp.getUser()+" "+uTmp.getNombre());
        }
    }

    /*@Test
    public void pruebaLDAPAdmin() throws Exception{
        Properties config=new Properties();        
        config.setProperty("url", "ldap://delfos.cisen.gob:389");
        config.setProperty("base", "dc=cisen,dc=gob");
        config.setProperty("bindType", "activeDirectory");
        config.setProperty("dataUser", "sAMAccountName");
        config.setProperty("dataMail", "mail");
        config.setProperty("dataName", "displayName");
        config.setProperty("filePass", "passwd");
        
        CipherSha1 cif=new CipherSha1();
        HashMap data=new HashMap();
        data.put("user", "admin");
        data.put("pass", cif.cifra("system"));
                
        LoginLDAPAutentication aut=new LoginLDAPAutentication(config);        
        User u=aut.autenticaUsuario(data);
        System.out.println(u);
        uTmp=u;
    }
    
    @Test
    public void pruebaLDAPAdminCambio() throws Exception{
        Properties config=new Properties();        
        config.setProperty("url", "ldap://delfos.cisen.gob:389");
        config.setProperty("base", "dc=cisen,dc=gob");
        config.setProperty("bindType", "activeDirectory");
        config.setProperty("dataUser", "sAMAccountName");
        config.setProperty("dataMail", "mail");
        config.setProperty("dataName", "displayName");
        config.setProperty("filePass", "passwd");
        
        CipherSha1 cif=new CipherSha1();
        HashMap data=new HashMap();
        data.put("user", "admin");
        data.put("pass", cif.cifra("system"));
                
        LoginLDAPAutentication aut=new LoginLDAPAutentication(config);                
        User u=aut.autenticaUsuario(data);
        System.out.println(aut.modificaContrasena(u, cif.cifra("system2")));
    }*/

    /*@Test
    public void pruebaBusqueda() throws Exception{
        Hashtable auth = new Hashtable();
        auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        auth.put(Context.PROVIDER_URL, "ldap://delfos.cisen.gob:389");
        auth.put(Context.SECURITY_AUTHENTICATION, "simple");
        auth.put(Context.SECURITY_PRINCIPAL, "u11451@cisen.gob");
        auth.put(Context.SECURITY_CREDENTIALS, "MercurY.1356");
        auth.put(Context.REFERRAL, "follow");
        
        String login="u11432";
        String base="dc=cisen,dc=gob";
        SearchResult sr=null;
        Attributes att=null;
        
        DirContext ctx = new InitialDirContext(auth);
        String filter = "sAMAccountName=u11451";//cn=*";
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results = ctx.search(base, filter, constraints);

        while (results.hasMore()){
            sr=(SearchResult) results.next();
            System.out.println("P: "+sr);
            att = (Attributes) sr.getAttributes();
            System.out.println("A: "+att);
        }
        
        ctx.close();
    }*/
}
