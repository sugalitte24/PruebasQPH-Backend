/******************************
-  Nombre:      1.- Creación de base de datos
-  Descripción: Definición y creación del contenedor principal
               para el sistema de gestión de productos.
-  Tipo:        Estructura (Database)
-  Versión:     1.0
-  Autor:       Stalin Ladino
-  Fecha:       13-04-2026
*******************************/
CREATE DATABASE products_db;


/******************************
-  Nombre:      2.- Creación de tabla de productos
-  Descripción: Definición y creación de la tabla principal
               que almacena el catálogo de productos con
               control de precios y stock disponible.
-  Tipo:        Estructura (Table)
-  Versión:     1.0
-  Autor:       Stalin Ladino
-  Fecha:       13-04-2026
*******************************/
CREATE TABLE public.products (
    id      serial         NOT NULL,
    nombre  varchar(255)   NOT NULL,
    precio  numeric(38, 2) NOT NULL,
    stock   int4           NOT NULL,
    CONSTRAINT products_pkey       PRIMARY KEY (id),
    CONSTRAINT products_stock_check CHECK ((stock >= 0))
);

/******************************
-  Nombre:      3.- Creación de tabla de usuarios
-  Descripción: Usuarios del sistema. La contraseña se guarda
               como hash BCrypt. El rol define permisos
               (ADMIN: gestiona usuarios; USER: opera ventas).
-  Tipo:        Estructura (Table)
-  Versión:     1.1
*******************************/
CREATE TABLE public.users (
    id          serial        NOT NULL,
    username    varchar(50)   NOT NULL,
    password    varchar(100)  NOT NULL,
    nombre      varchar(150)  NOT NULL,
    email       varchar(150)  NOT NULL,
    role        varchar(20)   NOT NULL DEFAULT 'USER',
    created_at  timestamp     NOT NULL DEFAULT now(),
    CONSTRAINT users_pkey         PRIMARY KEY (id),
    CONSTRAINT users_username_uk  UNIQUE (username),
    CONSTRAINT users_email_uk     UNIQUE (email),
    CONSTRAINT users_role_check   CHECK (role IN ('ADMIN', 'USER'))
);


/******************************
-  Nombre:      4.- Creación de tabla de ventas (cabecera)
-  Descripción: Una venta pertenece a un usuario (quien la
               registró) y agrupa uno o varios ítems.
-  Tipo:        Estructura (Table)
-  Versión:     1.1
*******************************/
CREATE TABLE public.sales (
    id              serial         NOT NULL,
    invoice_number  varchar(36)    NOT NULL,
    user_id         int4           NOT NULL,
    total           numeric(38, 2) NOT NULL,
    created_at      timestamp      NOT NULL DEFAULT now(),
    CONSTRAINT sales_pkey          PRIMARY KEY (id),
    CONSTRAINT sales_invoice_uk    UNIQUE (invoice_number),
    CONSTRAINT sales_user_fk       FOREIGN KEY (user_id) REFERENCES public.users (id),
    CONSTRAINT sales_total_check   CHECK (total >= 0)
);
CREATE INDEX sales_user_idx       ON public.sales (user_id);
CREATE INDEX sales_created_at_idx ON public.sales (created_at);


/******************************
-  Nombre:      5.- Creación de tabla de detalle de ventas
-  Descripción: Relación N:M entre ventas y productos. Guarda
               el precio unitario vigente al momento de la
               venta para que el histórico no cambie si el
               precio del producto se actualiza después.
-  Tipo:        Estructura (Table)
-  Versión:     1.1
*******************************/
CREATE TABLE public.sale_items (
    id          serial         NOT NULL,
    sale_id     int4           NOT NULL,
    product_id  int4           NOT NULL,
    quantity    int4           NOT NULL,
    unit_price  numeric(38, 2) NOT NULL,
    subtotal    numeric(38, 2) NOT NULL,
    CONSTRAINT sale_items_pkey            PRIMARY KEY (id),
    CONSTRAINT sale_items_sale_fk         FOREIGN KEY (sale_id)    REFERENCES public.sales (id) ON DELETE CASCADE,
    CONSTRAINT sale_items_product_fk      FOREIGN KEY (product_id) REFERENCES public.products (id),
    CONSTRAINT sale_items_quantity_check  CHECK (quantity > 0)
);
CREATE INDEX sale_items_sale_idx    ON public.sale_items (sale_id);
CREATE INDEX sale_items_product_idx ON public.sale_items (product_id);


/******************************
-  Nombre:      6.- Usuario administrador inicial
-  Descripción: admin / admin123 (hash BCrypt)
-  Tipo:        Datos (Insert)
-  Versión:     1.1
*******************************/
INSERT INTO public.users (username, password, nombre, email, role)
VALUES ('admin', '$2a$10$eMSPwJfxqXRuTXFmPRKy2.CvXAMpOQ1q3Wgvc4cOkli9RuOBK5Cge',
        'Administrador', 'admin@producthub.local', 'ADMIN');
