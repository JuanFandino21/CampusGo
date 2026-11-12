# CampusGo

Aplicación web para el registro y consulta de objetos perdidos y encontrados en el entorno universitario. CampusGo está construido con Spring Boot y dividido en **dos microservicios independientes**, protegidos mediante autenticación con JSON Web Tokens (JWT). Se puede ejecutar de forma local, en contenedores Docker o en un clúster de Kubernetes.

## Proyecto

**Proyecto 01 – Web APIs y Microservicios**

## Integrantes

- **Juan David Fandiño Hernández** — 2220221087
- **Anthony Sebastián Vanegas Aguirre** — 2220251055

## Descripción

CampusGo permite registrar, consultar, actualizar, entregar y eliminar objetos perdidos y encontrados, y ofrece al funcionario que opera la plataforma un resumen del inventario y la consulta de objetos antiguos.

El sistema está dividido en dos microservicios:

- **`campusgo-auth`** — autentica usuarios y emite tokens JWT.
- **`campusgo-objetos`** — gestiona el recurso `Objeto` y sirve la interfaz web.

Cada microservicio es un proyecto Maven independiente, con su propio `pom.xml`, su propio `Dockerfile` y **su propia base de datos**. Ninguno accede a las tablas del otro.

El acceso a la API está protegido: el usuario se autentica contra el servicio de autenticación, recibe un token JWT y lo envía en cada petición posterior al servicio de objetos. Ninguno de los dos mantiene sesiones en el servidor, por lo que cada petición es independiente.

La configuración se externaliza mediante variables de entorno, de modo que las mismas imágenes funcionan en los tres entornos sin modificar el código.

## Tecnologías

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.8 |
| Arquitectura | Microservicios |
| Web | Spring Web MVC |
| Persistencia | Spring Data JPA (Hibernate) |
| Base de datos | H2 en memoria (local) y PostgreSQL 16 (Docker y Kubernetes) |
| Seguridad | Spring Security, OAuth2 Resource Server, Nimbus JOSE (JWT con **RS256 / RSA 2048**), BCrypt, CORS |
| Validación | Jakarta Validation |
| Interfaz web | HTML, CSS y JavaScript sin frameworks |
| Construcción | Maven Wrapper |
| Contenedores | Docker y Docker Compose |
| Orquestación | Kubernetes |
| Pruebas manuales | Postman y curl |
| Control de versiones | Git y GitHub |

## Arquitectura

### Vista general

Los dos microservicios **no se llaman entre sí**. El servicio de objetos valida los tokens comprobando su firma con la llave pública RSA, sin consultar al servicio de autenticación. Esa es la ventaja de usar JWT en una arquitectura distribuida: el token es autocontenido, no hay una llamada de red adicional por cada petición y no existe un punto único de falla en tiempo de ejecución.

```text
                  Navegador / Postman / curl
                       |              |
         login (8081)  |              |  objetos (8083)
                       v              v
        ┌──────────────────────┐   ┌──────────────────────────┐
        │   campusgo-auth      │   │   campusgo-objetos       │
        │                      │   │                          │
        │  AuthController      │   │  Interfaz web            │
        │  UsuarioService      │   │  ObjetoController        │
        │  JwtService          │   │  verifica la firma       │
        │  FIRMA con la        │   │  con la LLAVE PÚBLICA    │
        │  LLAVE PRIVADA       │   │  (no puede firmar)       │
        └──────────┬───────────┘   └────────────┬─────────────┘
                   |                            |
                   v                            v
            Base de datos              Base de datos
             de usuarios                 de objetos
```

### Arquitectura interna de cada microservicio

Ambos conservan la arquitectura por capas. Toda petición a una ruta protegida pasa primero por el filtro de seguridad, que valida el token JWT antes de permitir el acceso al controlador.

```text
Interfaz Web / Cliente HTTP (Postman, curl)
                |
                | HTTP / JSON
                v
     Filtro de seguridad (valida el JWT)
                |
                v
           Controller
                |
                v
            Service
                |
                v
          Repository
                |
                v
   H2 en memoria  /  PostgreSQL
```

Responsabilidad de cada capa:

- **Controller:** recibe las peticiones HTTP, valida el cuerpo con `@Valid` y devuelve la respuesta con su código de estado.
- **Service:** contiene la lógica de negocio y coordina las operaciones.
- **Repository:** accede a la base de datos mediante Spring Data JPA, que genera las consultas a partir del nombre de los métodos.

### Reparto de responsabilidades

| | `campusgo-auth` | `campusgo-objetos` |
|---|---|---|
| Puerto | 8081 | 8083 |
| Emite tokens (`JwtEncoder`) | Sí | No |
| Valida tokens (`JwtDecoder`) | No | Sí |
| Llave privada RSA | Sí | **No** |
| Llave pública RSA | Sí | Sí |
| Conoce usuarios y contraseñas | Sí | No |
| Cifrado BCrypt | Sí | No |
| Sirve la interfaz web | No | Sí |
| Base de datos | `campusgo_auth` | `campusgo_objetos` |

## Estructura del proyecto

```text
CampusGo/
├── campusgo-auth/                          MICROSERVICIO 1
│   ├── src/main/java/CampusGo/
│   │   ├── CampusgoAuthApplication.java
│   │   ├── config/
│   │   │   ├── DataInitializer.java        Crea el usuario inicial
│   │   │   └── SecurityConfig.java         Emisión de JWT, BCrypt y CORS
│   │   ├── controller/
│   │   │   └── AuthController.java         Inicio de sesión
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   └── LoginResponse.java
│   │   ├── model/
│   │   │   └── Usuario.java
│   │   ├── repository/
│   │   │   └── UsuarioRepository.java
│   │   └── service/
│   │       ├── JwtService.java             Genera los tokens
│   │       └── UsuarioService.java
│   ├── src/main/resources/
│   │   ├── keys/
│   │   │   ├── private.pem                 Firma los tokens
│   │   │   └── public.pem
│   │   └── application.properties
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── campusgo-objetos/                       MICROSERVICIO 2
│   ├── src/main/java/CampusGo/
│   │   ├── CampusgoObjetosApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java         Validación de JWT
│   │   ├── controller/
│   │   │   └── ObjetoController.java       CRUD, búsquedas, resumen y entrega
│   │   ├── model/
│   │   │   └── Objeto.java
│   │   ├── repository/
│   │   │   └── ObjetoRepository.java
│   │   └── service/
│   │       └── ObjetoService.java
│   ├── src/main/resources/
│   │   ├── keys/
│   │   │   └── public.pem                  Solo verifica firmas
│   │   ├── static/
│   │   │   └── index.html                  Interfaz web
│   │   └── application.properties
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── k8s/                                    13 manifiestos
│   ├── namespace.yaml
│   ├── auth-configmap.yaml
│   ├── auth-deployment.yaml
│   ├── auth-service.yaml
│   ├── db-auth-deployment.yaml
│   ├── db-auth-pvc.yaml
│   ├── db-auth-service.yaml
│   ├── objetos-configmap.yaml
│   ├── objetos-deployment.yaml
│   ├── objetos-service.yaml
│   ├── db-objetos-deployment.yaml
│   ├── db-objetos-pvc.yaml
│   └── db-objetos-service.yaml
│
├── docker-compose.yml                      4 contenedores
├── .dockerignore
├── .gitignore
└── README.md
```

## Variables de entorno

La configuración se externaliza para no incluir credenciales en el código fuente. Cada `application.properties` define valores por defecto con la sintaxis `${VARIABLE:valor_por_defecto}`.

| Variable | Servicio | Obligatoria | Valor por defecto | Descripción |
|---|---|---|---|---|
| `JWT_EXPIRATION_MINUTES` | auth | No | `60` | Minutos de validez del token |
| `SERVER_PORT` | Ambos | No | `8081` / `8083` | Puerto del microservicio |
| `SPRING_DATASOURCE_URL` | Ambos | No | `jdbc:h2:mem:campusgo_auth` / `..._objetos` | URL de la base de datos |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | Ambos | No | `org.h2.Driver` | Driver JDBC |
| `SPRING_DATASOURCE_USERNAME` | Ambos | No | `sa` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Ambos | No | (vacío) | Contraseña de la base de datos |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Ambos | No | `update` | Estrategia de creación del esquema |
| `SPRING_JPA_SHOW_SQL` | Ambos | No | `true` | Muestra las consultas SQL en consola |
| `SPRING_H2_CONSOLE_ENABLED` | Ambos | No | `true` | Habilita la consola de H2 |
| `POSTGRES_PASSWORD` | Ambos | Solo Docker y Kubernetes | Ninguno | Contraseña de PostgreSQL |

> **Ninguna variable es obligatoria.** Los tokens se firman con un par de llaves RSA que viaja en el propio proyecto, así que los servicios arrancan sin configurar nada. Solo Docker y Kubernetes necesitan `POSTGRES_PASSWORD`.

El archivo `.env` se usa para entregarle los valores a Docker Compose y está excluido en el `.gitignore`. No se deben publicar contraseñas ni claves en el repositorio.

## Formas de ejecución

| Forma | Base de datos | Persistencia | Auth | Objetos (interfaz web) |
|---|---|---|---|---|
| Maven Wrapper (local) | H2 en memoria | Se pierde al detener los servicios | `http://localhost:8081` | `http://localhost:8083` |
| Docker Compose | PostgreSQL 16 | Volúmenes de Docker | `http://localhost:8081` | `http://localhost:8083` |
| Kubernetes | PostgreSQL 16 | PersistentVolumeClaim | `http://localhost:30081` | `http://localhost:30083` |

### Credenciales de acceso

Al arrancar, la clase `DataInitializer` del servicio de autenticación crea un usuario si todavía no existe:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `CampusGo123` | `USER` |

La contraseña se almacena cifrada con BCrypt. Es un usuario de desarrollo para poder probar la aplicación.

### Requisitos

- JDK 21. Si `javac -version` no responde, solo tienes el entorno de ejecución (JRE) y falta el JDK.
- Git.
- Docker Desktop, únicamente para las formas de ejecución con contenedores.

No hace falta instalar Maven: cada microservicio incluye el Maven Wrapper.

### 1. Clonar el repositorio

```bash
git clone https://github.com/JuanFandino21/CampusGo.git
cd CampusGo
```

### 2. Ejecución local con Maven Wrapper

Cada microservicio se levanta por separado, en su propia terminal. La base de datos por defecto es **H2 en memoria**, así que no hay que instalar nada más.

Terminal 1, servicio de autenticación:

```bash
cd campusgo-auth
./mvnw spring-boot:run
```

Terminal 2, servicio de objetos:

```bash
cd campusgo-objetos
./mvnw spring-boot:run
```

En Windows, con PowerShell o CMD, el comando es `.\mvnw.cmd spring-boot:run`.

La interfaz web queda disponible en `http://localhost:8083`. La consola de H2 de cada servicio queda en su propio puerto, bajo la ruta `/h2-console`.

> **Los datos se pierden al detener los servicios**, porque H2 en memoria vive dentro del proceso. Para conservarlos sin usar Docker, basta con sobrescribir la variable y usar H2 en modo archivo:
>
> ```bash
> SPRING_DATASOURCE_URL="jdbc:h2:file:./datos/objetos" ./mvnw spring-boot:run
> ```
>
> No hace falta tocar el código: la base de datos se elige por configuración.

### 3. Ejecución con Docker Compose

Crea un archivo `.env` en la raíz del proyecto con tus propios valores:

```properties
POSTGRES_PASSWORD=tu_contraseña
JWT_EXPIRATION_MINUTES=60
```

Levanta todo:

```bash
docker compose up --build -d
```

Este comando construye **dos imágenes**, una por microservicio, y levanta **cuatro contenedores**:

| Contenedor | Imagen | Función |
|---|---|---|
| `campusgo-db-auth` | `postgres:16-alpine` | Base de datos de usuarios |
| `campusgo-db-objetos` | `postgres:16-alpine` | Base de datos de objetos |
| `campusgo-auth` | `campusgo-auth:1.0` | Microservicio de autenticación |
| `campusgo-objetos` | `campusgo-objetos:1.0` | Microservicio de objetos |

Cada `Dockerfile` usa **dos etapas**: la primera compila el proyecto con Maven y genera el JAR; la segunda copia solo ese JAR a una imagen que únicamente tiene el entorno de ejecución de Java. Así la imagen final es mucho más liviana, porque no carga con Maven ni con el código fuente.

Cada servicio espera, mediante `depends_on` con la condición `service_healthy`, a que su base de datos responda a la verificación de estado antes de arrancar.

Comandos útiles:

```bash
docker compose ps        # Estado de los contenedores
docker compose logs -f   # Registros en vivo
docker compose down      # Detener los servicios
```

Los volúmenes `campusgo-auth-data` y `campusgo-objetos-data` conservan los datos entre reinicios. `docker compose down` no los elimina; para borrarlos también habría que usar `docker compose down -v`.

## API REST

### Autenticación — `campusgo-auth` (puerto 8081)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Autentica al usuario y devuelve un token JWT |

Cuerpo de la petición:

```json
{
  "username": "admin",
  "password": "CampusGo123"
}
```

Respuesta correcta, con código `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin"
}
```

Si las credenciales son incorrectas, responde `401 Unauthorized`.

### Objetos — `campusgo-objetos` (puerto 8083)

**Todos estos endpoints requieren autenticación.** Hay que enviar la cabecera `Authorization: Bearer <token>`. Sin ella, la respuesta es `401 Unauthorized`.

| Método | Endpoint | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/objetos` | Registrar un objeto | `201 Created` |
| GET | `/api/objetos` | Listar todos los objetos | `200 OK` |
| GET | `/api/objetos/{id}` | Consultar un objeto por ID | `200 OK` o `404 Not Found` |
| GET | `/api/objetos/estado?valor=PERDIDO` | Filtrar por estado | `200 OK` |
| GET | `/api/objetos/buscar?nombre=calculadora` | Buscar por nombre, sin importar mayúsculas | `200 OK` |
| GET | `/api/objetos/resumen` | Resumen del inventario | `200 OK` |
| GET | `/api/objetos/antiguos?dias=30` | Objetos registrados hace más de N días | `200 OK` |
| PUT | `/api/objetos/{id}` | Actualizar un objeto | `200 OK` o `404 Not Found` |
| PUT | `/api/objetos/{id}/entregar?persona=Juan Perez` | Registrar la entrega al dueño | `200 OK` o `404 Not Found` |
| DELETE | `/api/objetos/{id}` | Eliminar un objeto | `204 No Content` o `404 Not Found` |

Cuerpo para crear o actualizar un objeto:

```json
{
  "nombre": "Calculadora Casio",
  "descripcion": "Calculadora científica color negro",
  "categoria": "Electrónica",
  "ubicacion": "Bloque 3, salón 204",
  "estado": "PERDIDO"
}
```

### Validaciones

El modelo `Objeto` valida los datos antes de guardarlos. Si alguna regla no se cumple, la API responde `400 Bad Request` y no toca la base de datos.

| Campo | Regla |
|---|---|
| `nombre` | No puede estar vacío |
| `descripcion` | No puede estar vacío |
| `categoria` | No puede estar vacío |
| `ubicacion` | No puede estar vacío |
| `estado` | No puede estar vacío y solo acepta `PERDIDO`, `ENCONTRADO` o `ENTREGADO` |

Los campos `fechaRegistro`, `fechaEntrega` y `entregadoA` no se envían en la petición: los llena el sistema.

### Rutas públicas

En `campusgo-auth`:

- `/api/auth/**`, para poder obtener el token
- `/h2-console/**`, la consola de H2 en desarrollo

En `campusgo-objetos`:

- `/` y `/index.html`, la interfaz web
- `/h2-console/**`, la consola de H2 en desarrollo

Cualquier otra ruta exige autenticación.

## Funcionalidades de gestión del inventario

Además del CRUD básico, la plataforma incorpora tres funcionalidades pensadas para el funcionario que opera la oficina de objetos perdidos. Las tres viven en `campusgo-objetos` y están disponibles tanto en la API como en la interfaz web.

### 1. Resumen del inventario

`GET /api/objetos/resumen` devuelve los totales por estado y el conteo por categoría:

```json
{
  "total": 12,
  "perdidos": 7,
  "encontrados": 4,
  "entregados": 1,
  "porCategoria": {
    "Documentos": 6,
    "Electrónica": 4,
    "Ropa": 2
  }
}
```

Las cuentas las hace la base de datos, no Java. Para los totales por estado se usa el método derivado `countByEstadoIgnoreCase`, cuyo nombre Spring Data JPA traduce a una consulta de conteo. La agrupación por categoría no se puede expresar con el nombre de un método, así que se escribió explícitamente en JPQL con la anotación `@Query` y una cláusula `GROUP BY`.

En la interfaz web corresponde al botón **Ver resumen**.

### 2. Consulta de objetos antiguos

`GET /api/objetos/antiguos?dias=30` devuelve los objetos registrados hace más de N días, ordenados del más antiguo al más reciente. El parámetro `dias` tiene `30` como valor por defecto.

Esto es posible porque el modelo incorpora el campo `fechaRegistro`, anotado con `@CreationTimestamp`: Hibernate lo llena automáticamente en el momento de guardar, sin que el cliente tenga que enviarlo. La consulta usa el método derivado `findByFechaRegistroBeforeOrderByFechaRegistroAsc`.

Sirve para decidir qué objetos donar o descartar tras un periodo sin reclamar. En la interfaz web corresponde al botón **Ver antiguos**, con un campo para indicar los días.

### 3. Registro de entrega

`PUT /api/objetos/{id}/entregar?persona=Juan Perez` cierra el ciclo de vida del objeto: cambia su estado a `ENTREGADO` y guarda a quién se le entregó y en qué momento, en los campos `entregadoA` y `fechaEntrega`.

Antes de esta funcionalidad, la única forma de sacar un objeto del inventario era eliminarlo, con lo que se perdía la constancia de que efectivamente se había devuelto a su dueño.

El estado `ENTREGADO` **no aparece en el formulario de registro**, y es deliberado: un objeto no nace entregado, llega a ese estado únicamente a través de este endpoint. En la interfaz web, cada tarjeta muestra un botón **Entregar**, que desaparece una vez el objeto ha sido entregado, y la tarjeta se marca con una franja azul.

## Autenticación JWT

El flujo completo entre los dos microservicios es el siguiente:

1. El cliente envía usuario y contraseña a `/api/auth/login`, en **campusgo-auth**.
2. `AuthController` delega en el `AuthenticationManager`, que compara la contraseña con la versión cifrada en BCrypt almacenada en la base de datos de usuarios.
3. Si coincide, `JwtService` genera un token y lo firma con **RS256**, usando la llave privada RSA. El token lleva el emisor `campusgo`, el nombre de usuario como asunto, la fecha de expiración y los roles.
4. El cliente guarda el token y lo envía en la cabecera `Authorization` de las siguientes peticiones, dirigidas a **campusgo-objetos**.
5. El filtro de seguridad de ese servicio verifica la firma con la **llave pública** y revisa la expiración, **sin realizar ninguna llamada al servicio de autenticación**.

La API es **stateless**: no guarda sesiones en el servidor, y por eso se desactivó la protección CSRF. Esta es también la razón por la que cualquiera de los dos servicios se puede escalar a varias réplicas en Kubernetes sin configuración adicional.

### Firma asimétrica con RSA

Los tokens se firman con **RS256**, es decir, RSA de 2048 bits con SHA-256. Eso implica un **par de llaves** en vez de una clave compartida:

| | `campusgo-auth` | `campusgo-objetos` |
|---|---|---|
| Llave privada (`private.pem`) | La tiene | **No la tiene** |
| Llave pública (`public.pem`) | La tiene | La tiene |
| Puede firmar tokens | Sí | **No** |
| Puede verificar tokens | Sí | Sí |

La diferencia frente a una clave simétrica es de seguridad real. Con HS256 ambos servicios comparten la misma clave, de modo que el servicio de objetos también podría **emitir** tokens falsos. Con RSA no: la llave privada solo existe en el servicio de autenticación, así que aunque alguien vulnerara el servicio de objetos no tendría con qué firmar un token.

Las llaves se generaron con OpenSSL:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

`SecurityConfig` las lee del classpath, les quita las líneas de encabezado, decodifica el Base64 y las reconstruye con `KeyFactory`. El par se envuelve en un **JWK** (JSON Web Key), que es el formato estándar que usa la librería Nimbus.

> En este proyecto académico las llaves viajan dentro del repositorio para que cualquiera pueda clonarlo y ejecutarlo sin configuración previa. En un entorno productivo la llave privada nunca se versionaría: se inyectaría desde un gestor de secretos o desde una variable de entorno, igual que se hace aquí con la contraseña de PostgreSQL.

### CORS

La interfaz web se sirve desde `campusgo-objetos`, en el puerto 8083, pero el formulario de inicio de sesión llama a `campusgo-auth`, en el 8081. Para el navegador son **dos orígenes distintos**, así que el servicio de autenticación declara un bean `corsConfigurationSource` que autoriza esas llamadas. Es una consecuencia directa de haber dividido la aplicación en microservicios.

En `index.html`, la constante `URL_AUTH` define la dirección del servicio de autenticación. Las peticiones de objetos no la usan, porque se dirigen al mismo servicio que sirve la página.

### Probar la API en Postman

1. `POST http://localhost:8081/api/auth/login` con el cuerpo en formato JSON y las credenciales de arriba.
2. Copiar el valor de `token` de la respuesta.
3. En las peticiones a `http://localhost:8083/api/objetos`, abrir la pestaña **Authorization**, elegir el tipo **Bearer Token** y pegar el token.

### Probar la API con curl

Obtener el token y guardarlo en una variable:

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"CampusGo123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)
```

Consultar sin token, para comprobar que la API está protegida:

```bash
curl -i http://localhost:8083/api/objetos
```

Consultas con el token:

```bash
curl -s http://localhost:8083/api/objetos -H "Authorization: Bearer $TOKEN"

curl -s "http://localhost:8083/api/objetos/buscar?nombre=calcu" \
  -H "Authorization: Bearer $TOKEN"

curl -s http://localhost:8083/api/objetos/resumen \
  -H "Authorization: Bearer $TOKEN"

curl -s -X PUT "http://localhost:8083/api/objetos/1/entregar?persona=Juan%20Perez" \
  -H "Authorization: Bearer $TOKEN"
```

## Despliegue en Kubernetes

La carpeta `k8s/` contiene trece manifiestos, seis por microservicio más el namespace compartido:

| Archivo | Recurso | Función |
|---|---|---|
| `namespace.yaml` | Namespace | Aísla los recursos en el espacio `campusgo` |
| `auth-configmap.yaml` | ConfigMap | Configuración no confidencial de auth |
| `auth-deployment.yaml` | Deployment | Ejecuta el microservicio de autenticación |
| `auth-service.yaml` | Service (NodePort) | Expone auth en el puerto `30081` |
| `db-auth-deployment.yaml` | Deployment | PostgreSQL de usuarios, con sondas `pg_isready` |
| `db-auth-pvc.yaml` | PersistentVolumeClaim | Reserva 1 GiB para la base de usuarios |
| `db-auth-service.yaml` | Service (ClusterIP) | Comunica auth con su base de datos |
| `objetos-configmap.yaml` | ConfigMap | Configuración no confidencial de objetos |
| `objetos-deployment.yaml` | Deployment | Ejecuta el microservicio de objetos |
| `objetos-service.yaml` | Service (NodePort) | Expone objetos en el puerto `30083` |
| `db-objetos-deployment.yaml` | Deployment | PostgreSQL de objetos, con sondas `pg_isready` |
| `db-objetos-pvc.yaml` | PersistentVolumeClaim | Reserva 1 GiB para la base de objetos |
| `db-objetos-service.yaml` | Service (ClusterIP) | Comunica objetos con su base de datos |

Los Services de las bases de datos son de tipo **ClusterIP**, visibles únicamente dentro del clúster; los de los microservicios son de tipo **NodePort**, expuestos hacia el exterior.

> Los manifiestos están preparados para el Kubernetes que incluye **Docker Desktop**: los PersistentVolumeClaim usan la clase de almacenamiento `hostpath`, que es la de ese entorno. En Minikube la clase se llama `standard` y habría que ajustarla.

### 1. Habilitar Kubernetes

En Docker Desktop, entrar a **Settings → Kubernetes → Enable Kubernetes**. Después, verificar:

```bash
kubectl config use-context docker-desktop
kubectl get nodes
```

### 2. Construir las imágenes

Los Deployment usan `imagePullPolicy: IfNotPresent`, por lo que buscan las imágenes en el equipo y no en un registro remoto. Hay que construir las dos antes:

```bash
docker build -t campusgo-auth:1.0 ./campusgo-auth
docker build -t campusgo-objetos:1.0 ./campusgo-objetos
```

### 3. Crear el namespace y el Secret

El Secret **no está en el repositorio**, porque contiene credenciales. Se crea a mano con `kubectl` y lo comparten ambos microservicios:

```bash
kubectl apply -f k8s/namespace.yaml

kubectl create secret generic campusgo-secret \
  --namespace campusgo \
  --from-literal=POSTGRES_PASSWORD='tu_contraseña'
```

### 4. Desplegar

```bash
kubectl apply -f k8s/
kubectl get pods -n campusgo -w
```

Es normal que los pods de los microservicios se reinicien una o dos veces mientras PostgreSQL termina de iniciar. Cuando los cuatro aparezcan como `Running` y `1/1`, la interfaz web queda disponible en `http://localhost:30083`.

> En Kubernetes los puertos externos son `30081` y `30083`, distintos de los de Docker Compose. Para que el inicio de sesión funcione desde el navegador, hay que ajustar la constante `URL_AUTH` de `index.html` a `http://localhost:30081`. En un entorno productivo esto lo resolvería una puerta de enlace (API Gateway) que expusiera ambos servicios bajo un mismo dominio.

### Comandos de inspección

```bash
kubectl get all -n campusgo                              # Todos los recursos
kubectl get pvc -n campusgo                              # Almacenamiento persistente
kubectl logs -f deployment/campusgo-objetos -n campusgo  # Registros de un servicio
kubectl describe pod <nombre-del-pod> -n campusgo        # Detalle de un pod
```

### Escalar un microservicio

```bash
kubectl scale deployment campusgo-objetos --replicas=3 -n campusgo
```

Esto funciona sin configuración adicional porque la API es stateless: no guarda sesiones, cada petición lleva su token y los datos viven en PostgreSQL, no en la memoria del servicio. Además, al estar dividida la aplicación, se puede escalar únicamente el servicio que lo necesite.

### Eliminar el despliegue

```bash
kubectl delete namespace campusgo
```

## Pruebas

El comportamiento de la API se verificó con **Postman** y con **curl**, cubriendo:

- Inicio de sesión con credenciales correctas (`200 OK`) e incorrectas (`401 Unauthorized`).
- Acceso a `/api/objetos` sin token (`401 Unauthorized`) y con token válido (`200 OK`), lo que demuestra que un servicio valida los tokens emitidos por el otro.
- Creación de objetos (`201 Created`) y rechazo de un estado inválido (`400 Bad Request`).
- Consulta por ID, por estado y por nombre.
- Resumen del inventario antes y después de registrar una entrega, comprobando que los conteos cambian.
- Consulta de objetos antiguos con distintos valores de `dias`.
- Registro de entrega (`200 OK`) y verificación de que el objeto queda en estado `ENTREGADO`.
- Actualización (`200 OK`) y eliminación (`204 No Content`), incluyendo el caso de un ID inexistente (`404 Not Found`).

## Solución de problemas

| Síntoma | Causa |
|---|---|
| `No compiler is provided in this environment` | Falta el JDK 21; solo está instalado el JRE |
| `401 Unauthorized` al consultar objetos con un token recién obtenido | El `public.pem` de objetos no corresponde al `private.pem` de auth |
| `FileNotFoundException` con `keys/public.pem` o `keys/private.pem` | Falta la carpeta `keys/` dentro de `src/main/resources/` |
| `401 Unauthorized` en Postman | Falta la cabecera `Authorization: Bearer <token>` o el token expiró |
| El inicio de sesión falla desde el navegador | `URL_AUTH` en `index.html` no apunta al puerto correcto del servicio de autenticación |
| La lista de objetos aparece vacía al reiniciar | Es el comportamiento de H2 en memoria; use H2 en modo archivo, Docker o Kubernetes |
| Los cambios en `index.html` no se ven | `spring-boot:run` sirve los recursos desde `target/classes`; hay que reiniciar el servicio |
| `Port 8081 was already in use` | Hay otra instancia corriendo, en Maven o en un contenedor |
| Pod en `ErrImagePull` | Faltan los `docker build` de las dos imágenes |
| Pod en `CreateContainerConfigError` | Falta crear el Secret `campusgo-secret` |

## Control de versiones

El código se administra con Git y GitHub.

Repositorio: [CampusGo en GitHub](https://github.com/JuanFandino21/CampusGo)

Antes de enviar cambios, conviene revisar el estado del repositorio:

```bash
git status
git add .
git commit -m "Descripción del cambio"
git push
```

El archivo `.env` está excluido en el `.gitignore` y no debe subirse. Tampoco se deben incluir contraseñas, claves JWT ni archivos de Secret en los commits. Coordina los cambios con el otro integrante para evitar conflictos.
