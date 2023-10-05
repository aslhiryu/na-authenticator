CREATE TABLE usuario_na(
    id_usuario  VARCHAR(50) NOT NULL,
    nombre      VARCHAR(50) NOT NULL,
    login       VARCHAR(15) NOT NULL,
    pass        VARCHAR(30),
    mail        VARCHAR(50),
    estado      SMALLINT(1) DEFAULT 1,
    expira      DATE,
    ult_acceso  DATE,
    CONSTRAINT usuario_na_pk PRIMARY KEY (id_usuario)
);