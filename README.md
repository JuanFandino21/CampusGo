# CampusGo

Aplicación web para el registro y consulta de objetos perdidos y encontrados en el entorno universitario. CampusGo expone una API REST construida con Spring Boot, protegida mediante autenticación con JSON Web Tokens (JWT), y se puede ejecutar de forma local, en contenedores Docker o en un clúster de Kubernetes.

## Proyecto

**Proyecto 01 – Web APIs y Microservicios**

## Integrantes

- **Juan David Fandiño Hernández** — 2220221087
- **Anthony Sebastián Vanegas Aguirre** — 2220251055

## Descripción

CampusGo permite registrar, consultar, actualizar y eliminar objetos perdidos y encontrados. El recurso principal es `Objeto`, que se administra a través de una API REST siguiendo una arquitectura por capas.

El acceso a la API está protegido: el usuario se autentica con usuario y contraseña, recibe un token JWT y lo envía en cada petición posterior. La aplicación no mantiene sesiones en el servidor, por lo que cada petición es independiente.

La configuración se externaliza mediante variables de entorno, de modo que la misma imagen de la aplicación funciona en los tres entornos sin modificar el código.

## Tecnologías

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.8 |
| Web | Spring Web MVC |
| Persistencia | Spring Data JPA (Hibernate) |
| Base de datos | H2 en memoria (local) y PostgreSQL 16 (Docker y Kubernetes) |
| Seguridad | Spring Security, OAuth2 Resource Server, Nimbus JOSE (JWT con HS256), BCrypt |
| Validación | Jakarta Validation |
| Interfaz web | HTML, CSS y JavaScript sin frameworks |
| Construcción | Maven Wrapper |
| Contenedores | Docker y Docker Compose |
| Orquestación | Kubernetes |
| Pruebas manuales | Postman |
| Control de versiones | Git y GitHub |

## Arquitectura

La aplicación sigue una arquitectura por capas. Toda petición a una ruta protegida pasa primero por el filtro de seguridad, que valida el token JWT antes de permitir el acceso al controlador.

```text
Interfaz Web / Cliente HTTP (Postman)
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

## Estructura del proyecto

```text
CampusGo/
├── src/
│   ├── main/
│   │   ├── java/CampusGo/
│   │   │   ├── CampusgoApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java    Crea el usuario inicial
│   │   │   │   └── SecurityConfig.java     Reglas de seguridad y JWT
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java     Inicio de sesión
│   │   │   │   └── ObjetoController.java   CRUD de objetos
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── LoginResponse.java
│   │   │   ├── model/
│   │   │   │   ├── Objeto.java
│   │   │   │   └── Usuario.java
│   │   │   ├── repository/
│   │   │   │   ├── ObjetoRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   └── service/
│   │   │       ├── JwtService.java         Genera los tokens
│   │   │       ├── ObjetoService.java
│   │   │       └── UsuarioService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html              Interfaz web
│   │       └── application.properties
│   └── test/
│       └── java/CampusGo/
│           └── CampusgoApplicationTests.java
├── k8s/
│   ├── namespace.yaml
│   ├── app-configmap.yaml
│   ├── app-deployment.yaml
│   ├── app-service.yaml
│   ├── db-deployment.yaml
│   ├── db-pvc.yaml
│   └── db-service.yaml
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── .dockerignore
├── .gitignore
└── README.md
```

## Variables de entorno

La configuración se externaliza para no incluir credenciales en el código fuente. El archivo `application.properties` define valores por defecto con la sintaxis `${VARIABLE:valor_por_defecto}`.

| Variable | Obligatoria | Valor por defecto | Descripción |
|---|---|---|---|
| `JWT_SECRET` | **Sí** | Ninguno | Clave para firmar los tokens. **Mínimo 32 caracteres.** |
| `JWT_EXPIRATION_MINUTES` | No | `60` | Minutos de validez del token |
| `SERVER_PORT` | No | `8081` | Puerto de la aplicación |
| `SPRING_DATASOURCE_URL` | No | `jdbc:h2:mem:campusgo` | URL de la base de datos |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | No | `org.h2.Driver` | Driver JDBC |
| `SPRING_DATASOURCE_USERNAME` | No | `sa` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | No | (vacío) | Contraseña de la base de datos |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | No | `update` | Estrategia de creación del esquema |
| `SPRING_JPA_SHOW_SQL` | No | `true` | Muestra las consultas SQL en consola |
| `SPRING_H2_CONSOLE_ENABLED` | No | `true` | Habilita la consola de H2 |
| `POSTGRES_PASSWORD` | Solo Docker y Kubernetes | Ninguno | Contraseña de PostgreSQL |

> **`JWT_SECRET` es obligatoria.** No tiene valor por defecto, así que la aplicación **no arranca** sin ella. Además, `SecurityConfig` lanza una excepción si la clave tiene menos de 32 caracteres, porque es el mínimo que exige el algoritmo HS256.

El archivo `.env` se usa para entregarle los valores a Docker Compose y está excluido en el `.gitignore`. No se deben publicar contraseñas ni claves en el repositorio.

## Formas de ejecución

| Forma | Base de datos | Persistencia | URL de acceso |
|---|---|---|---|
| Maven Wrapper (local) | H2 en memoria | Se pierde al detener la aplicación | `http://localhost:8081` |
| Docker Compose | PostgreSQL 16 | Volumen de Docker | `http://localhost:8082` |
| Kubernetes | PostgreSQL 16 | PersistentVolumeClaim | `http://localhost:30082` |

### Credenciales de acceso

Al arrancar, la clase `DataInitializer` crea un usuario si todavía no existe:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `CampusGo123` | `USER` |

La contraseña se almacena cifrada con BCrypt. Es un usuario de desarrollo para poder probar la aplicación.

### Requisitos

- JDK 21. Si `javac -version` no responde, solo tienes el entorno de ejecución (JRE) y falta el JDK.
- Git.
- Docker Desktop, únicamente para las formas de ejecución con contenedores.

No hace falta instalar Maven: el proyecto incluye el Maven Wrapper.

### 1. Clonar el repositorio

```bash
git clone https://github.com/JuanFandino21/CampusGo.git
cd CampusGo
```

### 2. Ejecución local con Maven Wrapper

La base de datos por defecto es **H2 en memoria**, así que no hay que instalar nada más. Los datos se borran cada vez que se detiene la aplicación; el usuario `admin` se vuelve a crear solo en el siguiente arranque.

En Git Bash:

```bash
JWT_SECRET="clave-secreta-de-al-menos-32-caracteres" ./mvnw spring-boot:run
```

En PowerShell:

```powershell
$env:JWT_SECRET="clave-secreta-de-al-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8081`. La consola de H2 queda en `http://localhost:8081/h2-console`, con la URL `jdbc:h2:mem:campusgo` y el usuario `sa`.

Para compilar y ejecutar las pruebas:

```bash
JWT_SECRET="clave-secreta-de-al-menos-32-caracteres" ./mvnw clean package
```

> La variable también es necesaria al compilar, porque la prueba `contextLoads()` levanta el contexto completo de Spring.

### 3. Ejecución con Docker Compose

Crea un archivo `.env` en la raíz del proyecto con tus propios valores:

```properties
POSTGRES_PASSWORD=tu_contraseña
JWT_SECRET=clave-secreta-de-al-menos-32-caracteres
JWT_EXPIRATION_MINUTES=60
```

Levanta los servicios:

```bash
docker compose up --build -d
```

Esto construye la imagen de la aplicación con el `Dockerfile` de dos etapas (compila con Maven y ejecuta sobre una imagen ligera con solo el entorno de ejecución) y levanta dos contenedores: `campusgo-db` con PostgreSQL y `campusgo-app` con la aplicación. El contenedor de la aplicación espera a que PostgreSQL responda a su verificación de estado antes de arrancar.

La aplicación queda en `http://localhost:8082`, porque Compose asigna el puerto 8082 del equipo al 8081 del contenedor.

Comandos útiles:

```bash
docker compose ps        # Estado de los contenedores
docker compose logs -f   # Registros en vivo
docker compose down      # Detener los servicios
```

El volumen `campusgo-postgres-data` conserva los datos entre reinicios. `docker compose down` no lo elimina; para borrarlo también habría que usar `docker compose down -v`.

## API REST

### Autenticación

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

### Objetos

**Todos estos endpoints requieren autenticación.** Hay que enviar la cabecera `Authorization: Bearer <token>`. Sin ella, la respuesta es `401 Unauthorized`.

| Método | Endpoint | Descripción | Respuesta |
|---|---|---|---|
| POST | `/api/objetos` | Registrar un objeto | `201 Created` |
| GET | `/api/objetos` | Listar todos los objetos | `200 OK` |
| GET | `/api/objetos/{id}` | Consultar un objeto por ID | `200 OK` o `404 Not Found` |
| GET | `/api/objetos/estado?valor=PERDIDO` | Filtrar por estado | `200 OK` |
| GET | `/api/objetos/buscar?nombre=calculadora` | Buscar por nombre, sin importar mayúsculas | `200 OK` |
| PUT | `/api/objetos/{id}` | Actualizar un objeto | `200 OK` o `404 Not Found` |
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
| `estado` | No puede estar vacío y solo acepta `PERDIDO` o `ENCONTRADO` |

### Rutas públicas

Solo estas rutas no requieren token:

- `/` y `/index.html`, la interfaz web
- `/api/auth/login`, para poder obtener el token
- `/h2-console/**`, la consola de H2 en desarrollo

Cualquier otra ruta exige autenticación.

## Autenticación JWT

El flujo es el siguiente:

1. El cliente envía usuario y contraseña a `/api/auth/login`.
2. `AuthController` delega en el `AuthenticationManager`, que compara la contraseña con la versión cifrada en BCrypt que está en la base de datos.
3. Si coincide, `JwtService` genera un token firmado con HS256, con el emisor `campusgo`, el nombre de usuario como asunto, la fecha de expiración y los roles.
4. El cliente guarda el token y lo envía en la cabecera `Authorization` de las siguientes peticiones.
5. El filtro de seguridad valida la firma y la expiración del token en cada petición.

La API es **stateless**: no guarda sesiones en el servidor, y por eso se desactivó la protección CSRF. Esta es también la razón por la que la aplicación se puede escalar a varias réplicas en Kubernetes sin configuración adicional.

### Probar la API en Postman

1. `POST http://localhost:8081/api/auth/login` con el cuerpo en formato JSON y las credenciales de arriba.
2. Copiar el valor de `token` de la respuesta.
3. En las demás peticiones, abrir la pestaña **Authorization**, elegir el tipo **Bearer Token** y pegar el token.

## Despliegue en Kubernetes

La carpeta `k8s/` contiene siete manifiestos:

| Archivo | Recurso | Función |
|---|---|---|
| `namespace.yaml` | Namespace | Aísla los recursos en el espacio `campusgo` |
| `app-configmap.yaml` | ConfigMap | Configuración no confidencial de la aplicación |
| `app-deployment.yaml` | Deployment | Ejecuta la aplicación, con sondas de disponibilidad |
| `app-service.yaml` | Service (NodePort) | Expone la aplicación en el puerto `30082` |
| `db-deployment.yaml` | Deployment | Ejecuta PostgreSQL, con sondas `pg_isready` |
| `db-pvc.yaml` | PersistentVolumeClaim | Reserva 1 GiB de almacenamiento persistente |
| `db-service.yaml` | Service (ClusterIP) | Comunica la aplicación con la base de datos dentro del clúster |

> Los manifiestos están preparados para el Kubernetes que incluye **Docker Desktop**: el `db-pvc.yaml` usa la clase de almacenamiento `hostpath`, que es la de ese entorno. En Minikube la clase se llama `standard` y habría que ajustarla.

### 1. Habilitar Kubernetes

En Docker Desktop, entrar a **Settings → Kubernetes → Enable Kubernetes**. Después, verificar:

```bash
kubectl config use-context docker-desktop
kubectl get nodes
```

### 2. Construir la imagen

El `app-deployment.yaml` usa `imagePullPolicy: IfNotPresent`, por lo que busca la imagen en el equipo y no en un registro remoto. Hay que construirla antes:

```bash
docker build -t campusgo:1.0 .
```

### 3. Crear el namespace y el Secret

El Secret **no está en el repositorio**, porque contiene credenciales. Se crea a mano con `kubectl`:

```bash
kubectl apply -f k8s/namespace.yaml

kubectl create secret generic campusgo-secret \
  --namespace campusgo \
  --from-literal=POSTGRES_PASSWORD='tu_contraseña' \
  --from-literal=JWT_SECRET='clave-secreta-de-al-menos-32-caracteres'
```

### 4. Desplegar

```bash
kubectl apply -f k8s/
kubectl get pods -n campusgo -w
```

Es normal que el pod de la aplicación se reinicie una o dos veces mientras PostgreSQL termina de iniciar. Cuando ambos pods aparezcan como `Running` y `1/1`, la aplicación queda disponible en `http://localhost:30082`.

### Comandos de inspección

```bash
kubectl get all -n campusgo                           # Todos los recursos
kubectl get pvc -n campusgo                           # Almacenamiento persistente
kubectl logs -f deployment/campusgo-app -n campusgo   # Registros de la aplicación
kubectl describe pod <nombre-del-pod> -n campusgo     # Detalle de un pod
```

### Escalar la aplicación

```bash
kubectl scale deployment campusgo-app --replicas=3 -n campusgo
```

Esto funciona sin configuración adicional porque la API es stateless: no guarda sesiones, cada petición lleva su token y los datos viven en PostgreSQL, no en la memoria de la aplicación.

### Eliminar el despliegue

```bash
kubectl delete namespace campusgo
```

## Pruebas

**Pruebas automatizadas.** El proyecto incluye la prueba `contextLoads()`, que verifica que el contexto de Spring se levante correctamente con toda la configuración de seguridad, persistencia y validación. Se ejecuta con:

```bash
JWT_SECRET="clave-secreta-de-al-menos-32-caracteres" ./mvnw test
```

**Pruebas manuales.** El comportamiento de la API se verificó con Postman, cubriendo:

- Inicio de sesión con credenciales correctas (`200 OK`) e incorrectas (`401 Unauthorized`).
- Acceso a `/api/objetos` sin token (`401 Unauthorized`) y con token válido (`200 OK`).
- Creación de objetos (`201 Created`) y rechazo de un estado inválido (`400 Bad Request`).
- Consulta por ID, por estado y por nombre.
- Actualización (`200 OK`) y eliminación (`204 No Content`), incluyendo el caso de un ID inexistente (`404 Not Found`).

## Solución de problemas

| Síntoma | Causa |
|---|---|
| `No compiler is provided in this environment` | Falta el JDK 21; solo está instalado el JRE |
| La aplicación no arranca y menciona `app.jwt.secret` | No se definió `JWT_SECRET` |
| `La clave JWT debe tener al menos 32 bytes` | El valor de `JWT_SECRET` es demasiado corto |
| `401 Unauthorized` en Postman | Falta la cabecera `Authorization: Bearer <token>` o el token expiró |
| La lista de objetos aparece vacía al reiniciar | Es el comportamiento de H2 en memoria; use Docker o Kubernetes para conservar los datos |
| Pod en `ErrImagePull` | Falta ejecutar `docker build -t campusgo:1.0 .` |
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