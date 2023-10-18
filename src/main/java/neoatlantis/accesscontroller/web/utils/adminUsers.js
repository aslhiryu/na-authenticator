/* 
 * Libreria con metodos para la administración de usuarios
 */

/**
 * Función para inactivar a un usuario
 * @param {String} id Identificador del usuario a inactivar
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersInactiveUser(id, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=inactiveUser&NA_Session="+session+"&NA_IdUser="+id, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoActive=document.getElementById("NA:ActiveUserButton:"+id);
                if( icoActive!==null ){
                    icoActive.className="NA_UserList_activeButton";
                    icoActive.title="Habilita al usuario";
                    icoActive.onclick=function(){NAAdminUsersActiveUser(""+id, ""+session);};
                }
                var icoModify=document.getElementById("NA:ModifyUserButton:"+id);
                if( icoModify!==null ){
                    icoModify.className="NA_null";
                }
                var icoPass=document.getElementById("NA:ChangePassButton:"+id);
                if( icoPass!==null ){
                    icoPass.className="NA_null";
                }
                var icoRoles=document.getElementById("NA:ChangeRolesButton:"+id);
                if( icoRoles!==null ){
                    icoRoles.className="NA_null";
                }
            }
        }
    };
    ajax.send();
}

/**
 * Función para activar a un usuario
 * @param {String} id Identificador del usuario a activar
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersActiveUser(id, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=activeUser&NA_Session="+session+"&NA_IdUser="+id, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoActive=document.getElementById("NA:ActiveUserButton:"+id);
                if( icoActive!==null ){
                    icoActive.className="NA_UserList_inactiveButton";
                    icoActive.title="Inhabilita al usuario";
                    icoActive.onclick=function(){NAAdminUsersInactiveUser(""+id, ""+session);};
                }
                var icoModify=document.getElementById("NA:ModifyUserButton:"+id);
                if( icoModify!==null ){
                    icoModify.className="NA_UserList_editButton";
                }
                var icoPass=document.getElementById("NA:ChangePassButton:"+id);
                if( icoPass!==null ){
                    icoPass.className="NA_UserList_passwordButton";
                }
                var icoRoles=document.getElementById("NA:ChangeRolesButton:"+id);
                if( icoRoles!==null ){
                    icoRoles.className="NA_UserList_rolesButton";
                }
            }
        }
    };
    ajax.send();
}

/**
 * Función que agrega un rol a un usuario
 * @param {String} idUser Identificador del usuario
 * @param {String} idRole Identificador del rol
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersAddRole(idUser, idRole, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=addRole&NA_Session="+session+"&NA_IdUser="+idUser+"&NA_IdRole="+idRole, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoRedio=document.getElementById("NA:AssigRoleButton:"+idRole);
                if( icoRedio!==null ){
                    icoRedio.className="NA_EditRole_selectedRole";
                    icoRedio.title="Remueve Rol";
                    icoRedio.onclick=function(){NAAdminUsersRemoveRole(""+idUser, ""+idRole,  ""+session);};
                }
            }
        }
    };
    ajax.send();
}

/**
 * Función que remueve un rol de un usuario
 * @param {String} idUser Identificador del usuario
 * @param {String} idRole Identificador del rol
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersRemoveRole(idUser, idRole, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=removeRole&NA_Session="+session+"&NA_IdUser="+idUser+"&NA_IdRole="+idRole, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoRedio=document.getElementById("NA:AssigRoleButton:"+idRole);
                if( icoRedio!==null ){
                    icoRedio.className="NA_EditRole_unselectedRole";
                    icoRedio.title="Asigna Rol";
                    icoRedio.onclick=function(){NAAdminUsersAddRole(""+idUser, ""+idRole,  ""+session);};
                }
            }
        }
    };
    ajax.send();
}

/**
 * Elimina la conexión de un usuario
 * @param {String} id Identificador del usuario del que se desea terminar la conexión
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersRemoveConnection(id, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=removeSession&NA_Session="+session+"&NA_IdUser="+id, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoStatus=document.getElementById("NA:StatusUserButton:"+id);
                if( icoStatus!==null ){
                    icoStatus.className="NA_UserList_offlineButton";
                    icoStatus.title="Fuera de Linea";
                    icoStatus.onclick=null;
                }
            }
        }
    };
    ajax.send();
}

/**
 * Elimina el bloqueo de un usuario
 * @param {String} id Identificador del usuario del que se desea terminarel bloqueo
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersRemoveBlock(id, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=removeBlock&NA_Session="+session+"&NA_IdUser="+id, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoStatus=document.getElementById("NA:StatusUserButton:"+id);
                if( icoStatus!==null ){
                    icoStatus.className="NA_UserList_offlineButton";
                    icoStatus.title="Fuera de Linea";
                    icoStatus.onclick=null;
                }
            }
        }
    };
    ajax.send();
}

/**
 * Función que agrega un permiso a un rol
 * @param {String} idRole Identificador del rol
 * @param {String} idPermission Identificador del permiso
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersAddPermissionRole(idRole, idPermission, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=addPermissionRole&NA_Session="+session+"&NA_IdRole="+idRole+"&NA_IdPermission="+idPermission, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoRedio=document.getElementById("NA:AssigPermissionButton:"+idPermission);
                if( icoRedio!==null ){
                    icoRedio.className="NA_EditPermission_selectedPermission";
                    icoRedio.title="Remueve Permiso";
                    icoRedio.onclick=function(){NAAdminUsersRemoveRole(""+idRole, ""+idPermission,  ""+session);};
                }
            }
        }
    };
    ajax.send();
}

/**
 * Función que remueve un permiso de un rol
 * @param {String} idRole Identificador del rol
 * @param {String} idPermission Identificador del permiso
 * @param {String} session Id de la sesión del usuario que realiza la operación
 * @returns {boolean} true si tuvo exito
 */
function NAAdminUsersRemovePermissionRole(idRole, idPermission, session){
    var ajax = createAjaxObject();
    ajax.open("GET", "##SERVLET_CONTEXT##/neoAtlantis/resources/web/adminUser.service?NA_Operation=removePermissionRole&NA_Session="+session+"&NA_IdRole="+idRole+"&NA_IdPermission="+idPermission, true);
    ajax.onreadystatechange = function() {
        if( ajax.readyState===4 && ajax.status===200 ){
            //reviso si es un error
            if( ajax.responseText!==null && ajax.responseText.indexOf("ERROR:")!==-1 ){
                alert("Se encontro un error: "+ajax.responseText.substring(6));
            }
            //reviso si es la respuesta
            if( ajax.responseText!==null && ajax.responseText==="DATA:true" ){
                var icoRedio=document.getElementById("NA:AssigPermissionButton:"+idPermission);
                if( icoRedio!==null ){
                    icoRedio.className="NA_EditPermission_unselectedPermission";
                    icoRedio.title="Asigna Permiso";
                    icoRedio.onclick=function(){NAAdminUsersAddRole(""+idRole, ""+idPermission,  ""+session);};
                }
            }
        }
    };
    ajax.send();
}


function NAAdminUsersExecuteOperation(operation, id){
    var form=document.getElementById("NA:ChangedDataList");
    
    if( form!==null ){
        form.NA_Operation.value=operation;
        form.NA_id.value=id;
        form.submit();
    }
    else{
        alert("No existe la forma de control de paginacion");
    }
}