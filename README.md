# Induztek – Registro de Terreno

Aplicación Android para registrar en terreno los **protocolos de mantenimiento** y las **pruebas eléctricas** de equipos eléctricos (transformadores, tableros, celdas, etc.) instalados en las instalaciones de los clientes de la empresa.

El técnico elige el cliente, la instalación y el equipo desde listas definidas por un administrador, verifica con GPS que está en la instalación, completa el protocolo (con foto opcional) y lo guarda en el teléfono. El administrador gestiona clientes, usuarios y registros, y puede revisar el historial de cambios de cada registro.

Desarrollada en **Kotlin** con **Jetpack Compose**, arquitectura **MVVM** y base de datos local **SQLite (Room)**.

## Estudiantes

- César Saavedra
- Emilio Rojo

## Funcionalidades implementadas

### Inicio de sesión y roles
- Login con usuario y contraseña. Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
- La sesión queda abierta al cerrar la app, hasta que el usuario cierra sesión.
- Dos roles: **Administrador** y **Técnico**. Cada uno entra a su propia pantalla de inicio.

### Técnico
- **Nuevo registro de terreno:**
  - Cliente, instalación y tipo de equipo se eligen desde listas dependientes: cada lista muestra solo las opciones de lo elegido en la anterior.
  - El campo "Técnico a cargo" se completa automáticamente con el usuario que inició sesión.
- **Verificación GPS:**
  - Para registrar, el técnico debe estar a 100 m o menos de la instalación.
  - Con **Auto-detectar**, la app elige sola la instalación más cercana.
- **Protocolo de mantenimiento:**
  - Estado (Bueno / Regular / Malo / N/A) y observaciones de 7 componentes.
  - **Foto** tomada con la cámara o elegida desde la galería.
- **Protocolo de pruebas eléctricas:** mediciones de aislamiento, contacto, voltaje y corriente, y un veredicto (Aprobado / Observado / Rechazado).
- **Comparación con el registro anterior** del mismo equipo mientras se completa el formulario.
- **Registros anteriores:** listado con filtros por tipo y detalle completo de cada registro.
- **Edición de registros guardados:** cada cambio queda en el historial con fecha, hora, quién lo hizo y qué campos se modificaron.
- Animación de carga al guardar y mensaje de confirmación "Registro guardado".

### Administrador
- **Clientes:** crear, editar y eliminar clientes, sus instalaciones (con coordenadas GPS) y los tipos de equipo de cada instalación.
- **Usuarios:** crear, editar y eliminar técnicos y administradores. Un administrador no puede eliminarse ni quitarse el rol a sí mismo.
- **Registros anteriores:** revisar y editar los registros de todos los técnicos.
- **Archivar en vez de eliminar:**
  - Los registros "eliminados" pasan a la sección **Archivados**, que solo ven los administradores.
  - Desde ahí se pueden **restaurar**.

### Datos
- Todo se guarda en el teléfono en una base de datos **SQLite con Room**, con 8 tablas relacionadas.
- Las **migraciones** de la base de datos conservan los datos al actualizar la app.
- Los formularios no pierden lo escrito al **girar el teléfono**, porque el estado vive en los ViewModel.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje |
| Jetpack Compose + Material 3 | Interfaz de usuario |
| MVVM (ViewModel + StateFlow) | Arquitectura |
| Navigation Compose | Navegación entre pantallas |
| Room (SQLite) + KSP | Base de datos local |
| Corrutinas y Flow | Tareas en segundo plano y datos reactivos |
| Activity Result API + FileProvider | Cámara y galería |
| LocationManager | Ubicación GPS |
| SharedPreferences | Sesión iniciada |

## Estructura del proyecto

```
app/src/main/java/com/example/appnombre_grupo1/
├── MainActivity.kt          Punto de entrada de la app
├── InduztekApp.kt           Clase Application (crea el AppContainer)
├── di/                      AppContainer: base de datos y repositorios compartidos
├── model/                   MODEL: datos, validación, repositorios y Room
│   ├── database/            Entidades (tablas), DAOs, base de datos y migraciones
│   ├── repository/          Repositorios (usuarios, clientes, registros, fotos)
│   └── validation/          Validación de formularios
├── viewmodels/              VIEWMODEL: estado y lógica de cada pantalla
├── navigation/              Rutas y NavHost
└── ui/                      VIEW: pantallas y componentes en Compose
    ├── screens/             Login, formularios, administración y registros
    ├── components/          Componentes reutilizables (selectores, foto, permisos)
    └── utils/               Ubicación GPS y formato de fechas
```

## Pasos para ejecutar

### Requisitos
- **Android Studio** en una versión reciente, compatible con Android Gradle Plugin 9.3. Si al abrir el proyecto pide actualizar, hay que aceptar.
- **Android SDK 37**. Android Studio ofrece instalarlo al sincronizar el proyecto.
- Un teléfono o emulador con **Android 8.1 (API 27) o superior**.

### Ejecutar
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Cesar-Saavedra/KotlinPruebas.git
   ```
2. En Android Studio: **File → Open** y seleccionar la carpeta `KotlinPruebas`.
3. Esperar a que termine la sincronización de Gradle, o ejecutar **File → Sync Project with Gradle Files**.
4. Elegir un emulador o conectar un teléfono con la depuración USB activada.
5. Presionar **Run ▶** (`Shift + F10`).

### Usuarios de prueba
La primera vez que se abre la app se crean estos usuarios y los clientes de prueba **Jumbo** y **Falabella**, cada uno con dos instalaciones con coordenadas GPS:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin` | Administrador |
| `user` | `user` | Técnico |

### Probar la cámara y el GPS en el emulador
- **Cámara:**
  - En el **Device Manager**, el emulador debe tener la cámara configurada como *Emulated* o *VirtualScene*.
  - Al tocar "Tomar foto", hay que aceptar el permiso de cámara.
- **GPS:**
  1. En el emulador, abrir **Extended controls (···) → Location**.
  2. Fijar una ubicación cercana a una instalación de prueba, por ejemplo Jumbo Costanera Center: `-33.4173, -70.6062`.
  3. En el formulario, presionar **Auto-detectar** y aceptar el permiso de ubicación.

### Solución de problemas
- **La app se cierra al abrirla con un error de migración (`A migration from X to Y was required`):**
  - La base de datos del emulador quedó de una versión de prueba.
  - Hay que desinstalar la app del emulador, o ir a *Settings → Apps → la app → Storage → Clear data*, y volver a ejecutar.
- **Error de compilación de Room o KSP:** ejecutar **Build → Clean Project** y luego **Build → Rebuild Project**.
