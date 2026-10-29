\# FitTrack



Sistema de gestión de gimnasio desarrollado en Java con JavaFX y Maven.



\## Requisitos del entorno



\- JDK 21.

\- Apache Maven compatible con el proyecto.

\- JavaFX 21, gestionado mediante Maven.

\- MySQL Server 8.

\- MySQL Workbench para administrar la base de datos.

\- Git para colaborar mediante GitHub.



\## Abrir el proyecto



1\. Clonar el repositorio de GitHub.

2\. Abrir el proyecto desde NetBeans como proyecto Maven.

3\. Verificar que NetBeans utilice JDK 21.

4\. Esperar a que Maven descargue las dependencias.

5\. Ejecutar Clean and Build.

6\. Ejecutar el proyecto desde NetBeans.



También se puede iniciar desde la terminal, en la raíz del proyecto, con:



```powershell

mvn javafx:run

```



\## Estructura del código



\- `com.fittrack.model`: entidades y enumeraciones del modelo.

\- `com.fittrack.controller`: controladores de JavaFX.

\- `com.fittrack.service`: reglas de negocio.

\- `com.fittrack.dao`: operaciones de acceso a datos.

\- `com.fittrack.database`: conexión con MySQL.

\- `com.fittrack.util`: utilidades compartidas.

\- `src/main/resources`: vistas y estilos.

\- `database`: scripts SQL compartidos por el equipo.



\## Base de datos



Los scripts oficiales para crear y actualizar la base de datos se almacenarán en `database/`.



Cada integrante utilizará su propia instancia local de MySQL, salvo que el equipo acuerde utilizar un servidor compartido.



No subir contraseñas, credenciales ni datos personales reales.



\## Colaboración



\- `main`: versión estable.

\- `develop`: integración del trabajo del equipo.

\- `feature/\*`: ramas para desarrollar funcionalidades.



Las funcionalidades se integran en `develop` mediante Pull Requests y, después de probarlas, se incorporan a `main`.

