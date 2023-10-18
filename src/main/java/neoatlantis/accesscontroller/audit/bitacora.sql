CREATE TABLE bitacora_na(
    id_evento       VARCHAR(50),
    id_usuario         VARCHAR(50),
    terminal        VARCHAR(60),
    origen          VARCHAR(30),
    fecha           DATETIME,
    evento          VARCHAR(1),
    detalle         VARCHAR(255),
    CONSTRAINT bitacora_na_pk PRIMARY KEY (id_evento)
);
