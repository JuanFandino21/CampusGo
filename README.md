# CampusGo

Aplicación web para el registro y consulta de objetos perdidos y encontrados en el entorno universitario.

## Proyecto

**Proyecto 01 – Web APIs y Microservicios**

## Integrantes

- **Juan David Fandiño Hernández** — 2220221087
- **Anthony Sebastián Vanegas Aguirre** — 2220251055

## Descripción

CampusGo permite registrar, consultar, actualizar y eliminar objetos perdidos y encontrados. La aplicación cuenta con una interfaz web sencilla y una API REST para gestionar el recurso `Objeto`.

## Tecnologías

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Jakarta Validation
- HTML y JavaScript
- Maven
- Postman
- Git y GitHub

## Arquitectura

```text
Interfaz Web
     |
     | HTTP / JSON
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
H2 Database
```

## Estructura principal

```text
src/
└── main/
    ├── java/
    │   └── CampusGo/
    │       ├── controller/
    │       │   └── ObjetoController.java
    │       ├── model/
    │       │   └── Objeto.java
    │       ├── repository/
    │       │   └── ObjetoRepository.java
    │       └── service/
    │           └── ObjetoService.java
    └── resources/
        ├── static/
        │   └── index.html
        └── application.properties
```

## API REST

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/objetos` | Registrar un objeto |
| GET | `/api/objetos` | Listar objetos |
| GET | `/api/objetos/{id}` | Consultar un objeto por ID |
| GET | `/api/objetos/estado?valor=PERDIDO` | Consultar objetos por estado |
| GET | `/api/objetos/buscar?nombre=calculadora` | Buscar objetos por nombre |
| PUT | `/api/objetos/{id}` | Actualizar un objeto |
| DELETE | `/api/objetos/{id}` | Eliminar un objeto |

## Ejecución

Desde la carpeta raíz del proyecto, en Windows:

```bash
mvnw.cmd spring-boot:run
```

Después abrir:

```text
http://localhost:8080
```

## Pruebas

La API fue probada mediante Postman:

- POST — `201 Created`
- GET — `200 OK`
- PUT — `200 OK`
- DELETE — `204 No Content`

## Repositorio

https://github.com/JuanFandino21/CampusGo