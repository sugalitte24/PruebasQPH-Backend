# Cambios y mejoras

Cambios hechos sobre el proyecto base. El README original sigue igual.

## Base de datos

Se agregaron tres tablas nuevas (el script está en `product_db.sql`):

- `users`: usuarios con rol `ADMIN` o `USER`. La contraseña se guarda con BCrypt.
- `sales`: cabecera de la venta (quién vendió, fecha y total).
- `sale_items`: detalle de la venta (producto, cantidad, precio unitario y subtotal).

Relación: un usuario tiene muchas ventas, y cada venta tiene uno o varios productos.
El precio se guarda en el detalle para que el histórico no cambie si después se edita el producto.

Usuario inicial: `admin` / `admin123`.

## Ventas y stock

- `POST /api/sales` registra una venta y `GET /api/sales` devuelve el historial (el admin ve todas, el usuario solo las suyas).
- El stock se descuenta con un `UPDATE ... WHERE stock >= cantidad`, así no se puede vender más de lo que hay aunque lleguen dos ventas al mismo tiempo.
- Si un producto no tiene stock, se cancela toda la venta y responde 409.
- No se puede eliminar un producto que ya tiene ventas.

## Usuarios

- `GET /api/users` y `POST /api/users`, solo para administradores.
- El login ahora valida contra la base de datos y el token incluye el rol.
- Se valida que el usuario y el email no estén repetidos.

## Seguridad y errores

- Sin token o con token inválido/expirado ahora responde 401 (antes daba 403 sin mensaje).
- Todos los errores devuelven el mismo formato: `{ "error": "..." }`.
- JSON mal formado o parámetros inválidos responden 400 en lugar de 500.
- Los errores no controlados se registran en el log.
- Credenciales y secreto JWT se pueden configurar por variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`), con valores por defecto para desarrollo.
- Se quitó el `System.out` que imprimía un hash al arrancar.

## Swagger

Documentación de la API en `http://localhost:8080/swagger-ui.html`.
Para probar los endpoints protegidos, hacer login y pegar el token en el botón Authorize.

## Tests

Tests unitarios con JUnit y Mockito para `SaleServiceImpl` y `UserServiceImpl` (ventas, stock insuficiente, usuarios duplicados, etc.). Se ejecutan con:

```bash
./mvnw test
```

## Frontend

- Menú superior con Productos, Ventas y Usuarios.
- Botón para vender desde la lista de productos.
- Pantalla de historial de ventas.
- Pantalla para crear usuarios (solo admin).
- URL de la API en `environments`.
