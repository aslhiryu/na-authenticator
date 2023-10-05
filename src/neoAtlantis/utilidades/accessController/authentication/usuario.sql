CREATE TABLE usuario_na(
    id_usuario  INT NOT NULL,
    nombre      VARCHAR(50),
    login       VARCHAR(15),
    pass        VARCHAR(30),
    mail        VARCHAR(50),
    estado      CHAR(1),
    expira      DATE,
    CONSTRAINT id_usuario_na PRIMARY KEY (id_usuario)
);