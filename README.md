# Vankoo IAM Service

Este microservicio es el núcleo de seguridad de **Vankoo**. Se encarga de la autenticación (JWT), registro de usuarios y gestión de roles. Utiliza **Spring Boot 4**, **PostgreSQL** y se comunica con **Kafka** para eventos asíncronos.

## Requisitos Previos

* **Java:** 25 (Temurin de preferencia)
* **Maven:** 3.9+
* **Infraestructura:** Tener corriendo el repositorio `vankoo-infra` (Docker Desktop).

---

## Modos de Ejecución

Hemos configurado el servicio para que sea flexible según lo que necesites hacer:

### 1. Desarrollo Local (IntelliJ / VS Code)
Ideal para programar y ver cambios al instante.
* **Perfil activo:** `dev`
* **Eureka:** El servicio buscará el Discovery en `localhost:8761`.
* **Base de Datos/Kafka:** Debes tener los contenedores de `vankoo-infra` encendidos.

**Pasos:**
1. Ten configurada tu variable de entorno: `SPRING_PROFILES_ACTIVE=dev`.
2. Asegúrate de tener las variables `JWT_SECRET` y `JWT_EXPIRATION_DAYS` en tu entorno local.
3. Ejecuta la clase `VankooIamServiceApplication` (o usa el comando `mvn spring-boot:run`, o bien el botón de ejecución en tu IDE).

### 2. Contenerizado (Docker)
Ideal para pruebas de integración y despliegue final.
* **Perfil activo:** `docker`
* **Eureka:** El servicio buscará el hostname `discovery-server`.
* **Red:** Se integra automáticamente en la `main-network`.

**Pasos:**
1. Compila el proyecto:
   ```bash
   mvn clean package -DskipTests
    ```
   (O usa la interfaz de tu IDE, como `Maven` > `Lifecycle` > `clean` y luego `package` con la opción de omitir tests).
2. Ve al repositorio de infraestructura y levanta el servicio:
    ```bash
    docker compose up -d --build iam-service
    ```
    (Si es la primera vez que levantas `vankoo-infra`, simplemente corre `docker compose up -d --build` para levantar todo).

### 3. Azure Container Apps
Despliegue en la nube (Sprint 2). Los scripts y el paso a paso viven en `vankoo-infra/azure/`.
* **Perfil activo:** `azure` (la imagen arranca con `docker` por defecto; la Container App lo sobrescribe con `SPRING_PROFILES_ACTIVE=azure`).
* **Eureka:** apagado (`eureka.client.enabled=false`). El gateway resuelve por el DNS interno del entorno.
* **Eventos:** apagados (`vankoo.events.enabled=false`) mientras no haya broker en Azure. Entra `LoggingEventService`, que registra el evento en el log y lo descarta. **Parche temporal**: retirar cuando exista Event Hubs.
* **Esquema:** `ddl-auto: update` hasta que haya migraciones versionadas. **Parche temporal.**
* **Correo:** variables `MAIL_*` definidas pero vacías hasta elegir proveedor SMTP; recuperar contraseña responde igual pero el correo no sale. El indicador de salud de correo está apagado por el mismo motivo.
* **Actuator:** solo `health` e `info` expuestos; el resto no debe salir a internet.

Variables propias de este perfil, además de las de la tabla de abajo: `IAM_DB_NAME`, `IAM_DB_SSLMODE` (`require` por defecto), `IAM_PUBLIC_HOST`, `IAM_GATEWAY_HOST`, `PASSWORD_RESET_WEB_URL`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

---

## Arquitectura de Red (Eureka)

El servicio se registra en Eureka con el ID: `iam-service:iam-service:8081`.
* **Dashboard de Eureka:** `http://localhost:8761/`
* **Health Check (Público):** `http://localhost:8081/actuator/health`
> Nota: El endpoint de salud es público para permitir que el Healthcheck de Docker valide el estado del contenedor sin necesidad de tokens.

---

## Variables de Entorno Clave

Si necesitas personalizar la conexión, estas son las variables que maneja el `application.yaml`:

| Variable               | Descripción                               | Valor por Defecto (dev)         | Valor por Defecto (docker)             |
|------------------------|-------------------------------------------|---------------------------------|----------------------------------------|
| `IAM_DB_PORT`          | Puerto de PostgreSQL                      | `5432`                          | `5432`                                 |
| `IAM_DB_HOST`          | Host de PostgreSQL                        | `localhost`                     | `iam-db-postgres`                      |
| `JWT_SECRET`           | Clave secreta para JWT                    | *(Requerido)*                   | *(Requerido)*                          |
| `JWT_EXPIRATION_DAYS`  | Días de validez del token JWT             | `7`                             | `7`                                    |
| `IAM_DB_USERNAME`      | Usuario de la DB                          | `admin`                         | `admin`                                |
| `IAM_DB_PASSWORD`      | Contraseña de la DB                       | `password`                      | `password`                             |
| `KAFKA_HOST`           | Puerto de Kafka                           | `localhost`                     | `kafka-broker`                         |
| `KAFKA_EXTERNAL_PORT`  | Puerto externo de Kafka (para desarrollo) | `9092`                          | `9092`                                 |
| `KAFKA_INTERNAL_PORT`  | Puerto interno de Kafka (para Docker)     | `9094`                          | `9094`                                 |
| `DISCOVERY_SERVER_URL` | URL del Discovery Server                  | `http://localhost:8761/eureka/` | `http://discovery-server:8761/eureka/` |

## Troubleshooting (Solución de problemas)

1. **Error de Kafka (Rebootstrapping):** Asegúrate de que el contenedor de Kafka esté corriendo. Si estás en Local, verifica que el puerto `9092` esté mapeado.
2. **Status Unhealthy en Docker:** El servicio tarda unos 40 segundos en pasar a `healthy` debido a las validaciones de conexión con la DB y Kafka.
3. **Error 401 en Actuator:** Si agregaste nuevas reglas de seguridad, asegúrate de que `/actuator/**` siga teniendo `permitAll()`.

## Información Adicional

Puedes encontrar más detalles sobre el proyecto en el directorio de [Documentación](/docs), donde se agregaron guías en markdown y demás documentación relevante para el desarrollo y mantenimiento del servicio.