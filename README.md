# EvanMasterMartiniApp · Backend Spring Boot

Backend de Java 21 y Maven para la aplicación Android de Master Martini. Incluye tres microservicios REST, un API Gateway reactivo y se conecta a un servidor MySQL 8.4 externo con tres bases. El módulo Maven `common` es una biblioteca de clases compartidas.

```text
Android / cliente HTTP
  └─ Gateway reactivo (8090)
       ├─ Identidad y perfiles (8081) ─ martini_identity
       ├─ Catálogo (8082)             ─ martini_catalog
       └─ Contacto (8083)             ─ martini_contact
```

## Identidad y perfiles

Módulo `identity`. Registra cuentas de usuario, verifica credenciales, mantiene sesiones de ocho horas y administra el perfil de persona y negocio. Utiliza PBKDF2 para las contraseñas y almacena únicamente el hash del token de sesión. Cerrar sesión revoca el token. El registro público asigna el rol `user` y el administrador inicial se configura mediante variables de entorno.

| Método y ruta | Acceso y contrato |
| --- | --- |
| `POST /api/v1/auth/register` | Público. Recibe `nombre`, `correo`, `usuario`, `password`, `privacidad: true`, `terminos: true` y campos opcionales del perfil. Devuelve 201 con el perfil. |
| `POST /api/v1/auth/login` | Público. Recibe `correo` y `password`. Devuelve `accessToken`, `expiresIn` en segundos y `user`. |
| `POST /api/v1/auth/logout` | Bearer. Revoca la sesión y devuelve 204. |
| `GET /api/v1/profile/me` | Bearer. Devuelve el perfil de la cuenta actual. |
| `PATCH /api/v1/profile/me` | Bearer. Edita `nombre`, `correo`, `usuario`, `nombreNegocio`, `tipoNegocio`, `empleados`, `telefono`, `direccion`, `rutEmpresa` e `intereses` (lista de textos). |

Las tablas `users` y `login_sessions` se crean con Liquibase. La ruta interna `GET /internal/session` devuelve el ID y rol asociados al token para Catálogo y Contacto. El Gateway no publica esa ruta.

## Catálogo

Módulo `catalog`. Consulta recetas, videos y guías de formación. La búsqueda ignora mayúsculas y tildes. El administrador crea, edita y elimina fichas, y usa `published` para controlar la visibilidad pública.

| Método y ruta | Acceso y contrato |
| --- | --- |
| `GET /api/v1/recipes` y `GET /api/v1/recipes/{id}` | Público. La lista acepta `q`. |
| `GET /api/v1/learning` y `GET /api/v1/learning/{id}` | Público. La lista acepta `q` y `type=video` o `type=course`. |
| `GET /api/v1/admin/{recipes o learning}` y `GET /api/v1/admin/{recipes o learning}/{id}` | Administrador. Incluye borradores. |
| `POST /api/v1/admin/{recipes o learning}` | Administrador. Crea una ficha con ID único y devuelve 201. |
| `PATCH /api/v1/admin/{recipes o learning}/{id}` | Administrador. Actualiza parcialmente la ficha. Ejemplo de publicación: `{"published":true}`. |
| `DELETE /api/v1/admin/{recipes o learning}/{id}` | Administrador. Devuelve 204. |

Las recetas reciben `id`, `title`, `description`, `duration`, `servings`, `ingredients`, `steps`, `videoTitle`, `videoUrl`, `category` y `published`. El contenido formativo usa `id`, `title`, `specialty`, `summary`, `isVideo`, `lessons`, `videoUrl` y `published`. Los videos requieren una URL HTTPS de YouTube. Liquibase crea `catalog_entries` y carga las tres recetas, los dos videos y las tres guías iniciales de Android.

## Contacto

Módulo `contact`. Guarda las consultas y su estado. Cada usuario ve su historial y el administrador puede revisar la bandeja completa. El flujo funciona dentro del backend y no envía correos.

| Método y ruta | Acceso y contrato |
| --- | --- |
| `GET /api/v1/contact` | Bearer. Devuelve las consultas de la cuenta actual. |
| `POST /api/v1/contact` | Bearer. Recibe `subject` y `body`. Devuelve 201 con número, fecha y estado inicial `recibida`. |
| `GET /api/v1/admin/contact` | Administrador. Devuelve todas las consultas. |
| `PATCH /api/v1/admin/contact/{number}` | Administrador. Recibe `status` con valor `recibida`, `revisada` o `cerrada`. |

Liquibase crea `contact_messages` con un índice por usuario. Contacto usa el identificador que verifica Identidad y no necesita consultar su base de datos.

## API Gateway reactivo

Módulo `gateway`. Utiliza Spring Cloud Gateway Server WebFlux sobre Reactor Netty. Reenvía la API y el encabezado `Authorization: Bearer <accessToken>` al microservicio correspondiente. Los filtros de seguridad de los microservicios verifican el token y el rol. `GET /health` verifica en paralelo los endpoints Actuator de los tres servicios y devuelve 503 si alguno no está disponible.

`CorsConfig` admite orígenes web explícitos en `CORS_ORIGINS`, separados por comas. Por ejemplo, `http://localhost:5173`. Dejar el valor vacío deshabilita las peticiones CORS entre orígenes. Los clientes Android nativos utilizan HTTP directamente.

## Estructura y dependencias

En cada microservicio, `controller` expone los endpoints, `dto` define entradas y salidas, `service` contiene las reglas, `model` contiene las entidades JPA y `repository` implementa el acceso a datos con Spring Data JPA. `config/SecurityConfig.java` y `config/CorsConfig.java` definen seguridad y CORS. Los microservicios incluyen Spring Web, Spring Validation, Spring Data JPA, Spring Security, Lombok, Liquibase y MySQL Connector/J. El Gateway utiliza el starter reactivo y tiene sus propias clases de seguridad y CORS.

Cada microservicio tiene su changelog de Liquibase en `src/main/resources/db/changelog.sql` y su propia base y cuenta. Hibernate valida las tablas al iniciar. Los datos se guardan en el servidor MySQL externo. El proyecto usa un esquema nuevo y no importa automáticamente los datos de las versiones anteriores.

## Demostración local en Windows con VS Code

1. Instala o verifica JDK 21, las extensiones de Java para VS Code, MySQL Server 8 y el cliente mysql.exe. Inicia MySQL en localhost:3306.
2. Abre este repositorio en VS Code. En la terminal de la carpeta, ejecuta .\configurar-mysql-local.ps1 e ingresa la contraseña del administrador de MySQL cuando la pida. El script crea las bases y deja las contraseñas en archivos locales ignorados por Git.
3. En Run and Debug, selecciona **Backend completo (MySQL local)** y pulsa F5. Espera que http://localhost:8090/health indique status ok.
4. Abre el repositorio Android en Android Studio y ejecuta la variante debug en el emulador. Para demostrar el registro, crea un usuario con un correo ficticio nuevo. Para administrar recetas con una base recién configurada, ingresa con admin.demo@example.test y la contraseña ADMIN_PASSWORD que puedes leer localmente en .identity-local.properties.
5. En MySQL Workbench, consulta martini_identity.users para ver el registro y martini_catalog.catalog_entries para ver la receta.

El script prepara un entorno de demostración nuevo. Liquibase crea las tablas y carga el catálogo inicial. Cada computador guarda sus propios registros en MySQL.

## Compilar y arrancar

Para compilar localmente instala JDK 21 y Maven 3.9, abre una terminal en esta carpeta y ejecuta:

```powershell
mvn clean verify
```

### Preparar MySQL externo

1. Inicia un servidor MySQL 8.4 instalado en Windows o disponible en otro equipo. Debe aceptar conexiones TCP desde Docker.
2. Abre [`mysql-setup.sql`](mysql-setup.sql) en MySQL Workbench o en tu cliente SQL. Reemplaza `ORIGEN_CLIENTE` por el host o la red de origen que MySQL reconoce para las conexiones de los contenedores y ejecútalo con una cuenta administradora. El script crea las tres bases y sus cuentas. Anota las contraseñas generadas para las cuentas nuevas.
3. Copia `.env.example` a `.env` e ingresa las contraseñas en `IDENTITY_DB_PASSWORD`, `CATALOG_DB_PASSWORD` y `CONTACT_DB_PASSWORD`. Si las cuentas ya existían, usa sus contraseñas actuales. El backend no necesita la contraseña de `root`.
4. Configura `DB_HOST` y `DB_PORT`. En Docker Desktop, usa `host.docker.internal` si MySQL corre en el mismo equipo. Para un servidor remoto, usa su nombre DNS o IP y el puerto correspondiente. La dirección de destino en `DB_HOST` y el origen de la cuenta MySQL cumplen funciones distintas. [Conexión desde Docker Desktop al host](https://docs.docker.com/desktop/features/networking/networking-how-tos/#connect-a-container-to-a-service-on-the-host).

### Arrancar los cuatro componentes Java

`ADMIN_EMAIL` y `ADMIN_PASSWORD` son opcionales y se configuran juntos para probar las rutas de administrador. La contraseña de la cuenta administradora requiere de 8 a 128 caracteres.

```powershell
docker compose up --build -d
Invoke-RestMethod http://127.0.0.1:8090/health
```

Compose inicia el Gateway y los tres microservicios. Cada microservicio aplica sus cambios de Liquibase cuando se conecta a MySQL. Repite la consulta de salud hasta que devuelva `status: ok`. Compose publica sólo el Gateway en el puerto de `GATEWAY_PORT`. Para el emulador Android, la URL del ejemplo es `http://10.0.2.2:8090/api/v1`. La app Android del proyecto debe configurar ese cliente para consumir la API.

```powershell
$api = 'http://127.0.0.1:8090/api/v1'
Invoke-RestMethod "$api/recipes?q=flan"
Invoke-RestMethod "$api/learning?type=video"
$registro = @{ nombre='Demo'; correo='demo@example.test'; usuario='demo'; password='DemoLocal2026'; privacidad=$true; terminos=$true } | ConvertTo-Json
Invoke-RestMethod "$api/auth/register" -Method Post -ContentType 'application/json' -Body $registro
$credenciales = @{ correo='demo@example.test'; password='DemoLocal2026' } | ConvertTo-Json
$sesion = Invoke-RestMethod "$api/auth/login" -Method Post -ContentType 'application/json' -Body $credenciales
Invoke-RestMethod "$api/profile/me" -Headers @{ Authorization="Bearer $($sesion.accessToken)" }
```

Para ejecutar JAR individuales con un MySQL ya inicializado, configura `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` para cada microservicio. Usa `java -jar <módulo>/target/<módulo>-2.0.0.jar` en una terminal por servicio. Con los puertos predeterminados el Gateway local escucha en 8090. Usa `PORT` para cambiar el puerto HTTP de un proceso y las variables `IDENTITY_URL`, `CATALOG_URL` y `CONTACT_URL` para redirigir el Gateway. Catálogo y Contacto también usan `IDENTITY_URL`.

Referencias de implementación: [Spring Cloud Gateway WebFlux](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/starter.html) y [gestión de bases con Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html).
