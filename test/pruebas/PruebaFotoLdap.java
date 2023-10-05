/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import neoAtlantis.utils.accessController.authentication.LDAPAuthentication;

/**
 *
 * @author desarrollo.alberto
 */
public class PruebaFotoLdap {
    public static String base="DC=economia,DC=gob,DC=mx";
    public static String user="desarrollo.alberto";
    public static String url="ldap://172.18.53.100:389";
    
    
    public static void main(String[] args) throws Exception {
        //personaLogeada();
        personaBuscada();   
    }
    
    public static void personaLogeada() throws Exception {
        Hashtable auth = new Hashtable();
        String dn=LDAPAuthentication.getDNString("activeDirectory", base,user);
        String filtro="sAMAccountName="+user;
        SearchResult sr;
        Attributes attrs;
        Object foto;

        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, "BLUEMARY");

            auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            auth.put(Context.PROVIDER_URL, url);
            auth.put(Context.SECURITY_AUTHENTICATION, "simple");
            auth.put(Context.REFERRAL, "follow");
            auth.put("com.sun.jndi.ldap.read.timeout", ""+30000);
        
        DirContext ctx = new InitialDirContext(auth);   

            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(base, filtro, searchCtls);
            if( res.hasMore() ){
                System.out.println("Todo ok");
                
                sr = (SearchResult)res.next();
                attrs = sr.getAttributes();
                
                if (attrs != null) {
                    System.out.println("Tiene atributos");
                    foto=attrs.get("displayName");
                    System.out.println("Nombre: "+foto);
                    foto=attrs.get("photo");
                    System.out.println("Foto: "+foto);
                }
            }
            
            ctx.close();
    }
    
    public static void personaBuscada() throws Exception {
        Hashtable auth = new Hashtable();
        String dn=LDAPAuthentication.getDNString("activeDirectory", base,user);
        String filtro="sAMAccountName=miguel.jimenez";
        SearchResult sr;
        Attributes attrs;
        Object foto;
        Attribute att;
        InputStream photo = null;
        FileOutputStream fos;
        int b;

        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, "BLUEMARY");

            auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            auth.put(Context.PROVIDER_URL, url);
            auth.put(Context.SECURITY_AUTHENTICATION, "simple");
            auth.put(Context.REFERRAL, "follow");
            auth.put("com.sun.jndi.ldap.read.timeout", ""+30000);
        
        DirContext ctx = new InitialDirContext(auth);   

            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(base, filtro, searchCtls);
            if( res.hasMore() ){
                System.out.println("Todo ok");
                
                sr = (SearchResult)res.next();
                attrs = sr.getAttributes();
                
                if (attrs != null) {
                    System.out.println("Tiene atributos");
                    foto=attrs.get("displayName");
                    System.out.println("Nombre: "+foto);
                    foto=attrs.get("thumbnailPhoto");                    
                    System.out.println("Foto: "+foto);
                    if( foto!=null){
                        System.out.println("Foto: "+foto.getClass());
                        att=(Attribute)foto;
                        System.out.println("Foto: "+att.size());
                        System.out.println("Foto: "+att.get());
                        System.out.println("Foto: "+att.get().getClass());
                        photo = new ByteArrayInputStream((byte[]) att.get());
                        fos = new FileOutputStream("d:/img.jpg");
                        while( photo!=null&& (b=photo.read())!=-1 ){
                            fos.write(b);
                        }
                        fos.close();
                    }
                }
            }
            
            ctx.close();
    }
}
