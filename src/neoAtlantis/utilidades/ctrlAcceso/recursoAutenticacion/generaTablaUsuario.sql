CREATE TABLE usuario_na_data(
    id_usuario  INT, NOT NULL, PRIMARY KEY,
    nombre      VARCHAR(50),
    login       VARCHAR(15),
    pass        VARCHAR(30),
    activo      NUMERIC(1,0),
    expira      DATE,
    acceso      DATETIME,
    tipo        VARCHAR(1)
);