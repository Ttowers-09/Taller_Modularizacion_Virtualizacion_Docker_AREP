# Taller: Aplicación Web, Docker y Despliegue en AWS

- El taller consiste en crear una aplicación web pequeña usando Spring. Una vez    tengamos esta aplicación, construiremos un contenedor Docker para la app, lo desplegaremos y configuraremos en nuestra máquina local, subiremos la imagen a DockerHub y finalmente la desplegaremos en una máquina virtual de AWS.

## Arquitectura

El servidor está implementado en la clase principal `HttpServer.java` y funciona de la siguiente manera:

1. **Escucha en un puerto TCP (8080)** usando `ServerSocket` y acepta conexiones de clientes de forma secuencial (no concurrente).
2. **Procesa cada solicitud HTTP** leyendo la primera línea para identificar el método y la ruta solicitada.
3. **Manejo de archivos estáticos:**
    - Si la ruta corresponde a un archivo existente en `target/classes/webroot`, el servidor lee el archivo del disco y lo retorna con el tipo MIME adecuado (HTML, CSS, JS, imágenes, etc.).
4. **Manejo de servicios REST:**
    - Si la ruta solicitada corresponde a un endpoint especial (por ejemplo, `/hello`, `/hellopost`, `/app/hello`), el servidor ejecuta un método Java que genera la respuesta (texto o JSON), permitiendo la comunicación asíncrona desde el frontend.
5. **Frontend de prueba:**
    - La carpeta `public` contiene una aplicación web con HTML, CSS, JS e imágenes que permite probar tanto la carga de archivos estáticos como la invocación de servicios REST mediante AJAX/fetch.

El servidor no utiliza ningún framework web externo, solo clases estándar de Java (`ServerSocket`, `Files`, `PrintWriter`, etc.).

---

# Servidor web no concurrente
Este proyecto implementa un servidor web sencillo en **Java** que permite:
- Servir archivos estáticos como HTML, CSS, JS e imágenes.  
- Manejar solicitudes HTTP con los métodos **GET** y **POST**.  
- Responder a formularios desde el navegador. 

## Para comenzar
Estas instrucciones te permitirán obtener una copia del proyecto y ejecutarlo en tu máquina local para desarrollo y pruebas.  

### Prerequisites
Debes tener instalado en tu equipo:  

- [Java 17 o superior](https://adoptium.net/)  
- [Apache Maven](https://maven.apache.org/) 

Verifica las versiones:
-  java -version
-  mvn -version



## Estructura y puertos de tu proyecto
- **Puerto de la app en el contenedor:** `8080`  
- **Puerto expuesto en tu máquina (docker-compose):** `8087`  
- **Archivos estáticos:** en `webroot` (por ejemplo, `index.html`)  
- **Clase principal:** `com.arep.taller1.talle1arep.MicroSpringBoot` (o `HttpServer`)  

### Endpoints principales
- `GET /hello?name=TuNombre` → **Hello, TuNombre!**  
- `GET /App/pi` → **valor de pi**  
- `POST /App/hellopost` → **respuesta a formulario POST**  
- `GET /index.html` o `/` → **muestra el formulario HTML**  

**Visibilidad de archivos estáticos:**
- http://localhost:8080/index.html: veremos el archivo index.html el cual contendrá los métodos Get, Post, visibilidad de código style.css e imagen.

- http://localhost:8080/style.css: Veremos el codigo Css de la página.

- http://localhost:8080/IMAGEN.jpg: Veremos una imagen estática igual a la que se encuentra en el index.html.

## Compilación y dependencias

Compila con **Java 17+**  
En tu `pom.xml` agrega:

```
    xml
    <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    </properties>
```
## Dependencias de Spring
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <version>3.3.3</version>
</dependency>
```
## Utilizamos el plugin para copiar las dependencias
```
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>3.0.1</version>
      <executions>
        <execution>
          <id>copy-dependencies</id>
          <phase>package</phase>
          <goals>
            <goal>copy-dependencies</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Compilar proyecto
```
    mvn clean install
```
## Creamos el archivo dockerFile
```
FROM eclipse-temurin:17-jre

WORKDIR /usrapp/bin

ENV PORT 8080

COPY target/classes ./classes
COPY target/lib ./dependency

CMD ["java","-cp","./classes:./dependency/*","com.arep.taller1.talle1arep.MicroSpringBoot"]
```

## Creamos el archivo docker-compose.yml
```
version: '2'

services:
  web:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: web
    ports:
      - "8087:8080"
    depends_on:
      - db

  db:
    image: mongo:3.6.1
    container_name: db
    volumes:
      - mongodb:/data/db
      - mongodb_config:/data/configdb
    ports:
      - "27017:27017"
    command: mongod

volumes:
  mongodb:
  mongodb_config:
```

# Ejecucion local y en Docker:
## Construimos la imagen de Docker a parrtir del docker file
![Evidencia](src/main/resources/imgReadme/1.2.png)
## Verificamos la creacion de la imagen
![Evidencia](src/main/resources/imgReadme/1.3.png)

## Accedemos a http://localhost:8087/index.html, http://localhost:8087/ y http://localhost:8087/hello

## Creamos los contenedores indiciduales
```
docker run -d -p 34000:8080 --name firstdockercontainer dockersparkprimer
docker run -d -p 34001:8080 --name firstdockercontainer2 dockersparkprimer
docker run -d -p 34002:8080 --name firstdockercontainer3 dockersparkprimer
```
![Evidencia](src/main/resources/imgReadme/1.4.png)

## Los colocamos en funcionamiento
![Evidencia](src/main/resources/imgReadme/1.5.png)

## En nuestro visual lo listamos para ver que efectivamente estan corriendo
![Evidencia](src/main/resources/imgReadme/1.6.png)

## alternativa 2 para no ejecutarlo desde la interfaz grafica
![Evidencia](src/main/resources/imgReadme/1.7.png)

## nos dirigimos a nuestro navegador y lo comprobamos con el RestController
![Evidencia](src/main/resources/imgReadme/1.8.png)
![Evidencia](src/main/resources/imgReadme/1.9.png)

## Construimos los servicios dataBase y Web
```
docker-compose build
docker-compose up
```
![Evidencia](src/main/resources/imgReadme/1.10.png)

## Verificamos que se hallan creado correctamente
![Evidencia](src/main/resources/imgReadme/1.11.png)
```
- verificamos el mapeo de puertos
-Comprobamos el servicio web que expone el puerto 8080 del contenedor al 8087 de la maquina
- Contenedor de DataBase
```

## Comprobamos en Docker Desktop
![Evidencia](src/main/resources/imgReadme/1.12.png)

## Visualizamos en el navegador
![Evidencia](src/main/resources/imgReadme/1.13.png)

# Subir la imagen a Docker Hub
## creamos el repositorio en este caso areptallerdocker
![Evidencia](src/main/resources/imgReadme/1.14.png)

## Confirmamos que subimos dockersparkprimer
![Evidencia](src/main/resources/imgReadme/1.15.png)

## Subimos la imagen a DockerHub
```
Le damos a la imagen el nombre requerido por Docker Hub: usuario/repositorio:tag
```
![Evidencia](src/main/resources/imgReadme/1.16.png)

## realizamos login
![Evidencia](src/main/resources/imgReadme/1.17.png)

## subimos la imagen a DockerHub
![Evidencia](src/main/resources/imgReadme/1.18.png)

## Comprobamos en la UI de DockerHub
![Evidencia](src/main/resources/imgReadme/1.19.png)

## Ejecutamos la imagen anteriormente creada
![Evidencia](src/main/resources/imgReadme/1.20.png)

# Despliegue AWS
## Creamos la instancia de EC2 con los siguientes parametros
![Evidencia](src/main/resources/imgReadme/1.211.png)
![Evidencia](src/main/resources/imgReadme/1.21.png)
![Evidencia](src/main/resources/imgReadme/1.22.png)
![Evidencia](src/main/resources/imgReadme/1.23.png)
![Evidencia](src/main/resources/imgReadme/1.24.png)
![Evidencia](src/main/resources/imgReadme/1.25.png)
![Evidencia](src/main/resources/imgReadme/1.26.png)

## Ahora en la carpeta de nuestro proyecto guardamos la llave privada y nos conectamos mediante ssh
![Evidencia](src/main/resources/imgReadme/1.27.png)

## Actualizamos paquetes y librerias de nuestro sistema operativo en EC2
![Evidencia](src/main/resources/imgReadme/1.28.png)

## Instalamos Docker
![Evidencia](src/main/resources/imgReadme/1.29.png)

## Arrancamos el servicio
![Evidencia](src/main/resources/imgReadme/1.30.png)

## agregamos el usuario de ec2 para no usar siempre sudo
![Evidencia](src/main/resources/imgReadme/1.31.png)

## Clonamos nuestro repositorio
![Evidencia](src/main/resources/imgReadme/1.32.png)

## nos coinectamos mediante la URL
```´
http://ec2-34-230-35-71.compute-1.amazonaws.com:8080/hello
```
## Probamos la URl
![Evidencia](src/main/resources/imgReadme/1.34.png)
## Construido con:

- **Java Standard Library** - El proyecto está construido con clases de I/O y java.net de la librería estándar de Java.

- **Maven** - Se utiliza como herramienta de gestión de dependencias y compilación.

## Contribuir

En caso de querer contribuir envia una pull request especificando los cambios, en que afecta el código, el comportamiento del proyecto y su beneficio.

## Versionamiento

Usamos Git y GitHub para realizar el versionamiento del proyecto.

## Authors

* **Ivan Santiago Forero Torres** - *Trabajo inicial* - gitHub User: [Ttowers-09]


## Licencia

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details