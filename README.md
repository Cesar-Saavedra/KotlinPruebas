# Induztek – Registro de Terreno

Aplicación Android para registrar en terreno los **protocolos de mantenimiento** y las **pruebas eléctricas** de equipos eléctricos (transformadores, tableros, celdas, etc.) instalados en las instalaciones de los clientes de la empresa.

El técnico elige el cliente, la instalación y el equipo desde listas definidas por un administrador, verifica con GPS que está en la instalación, completa el protocolo (con foto opcional) y lo guarda en el teléfono. El administrador gestiona clientes, usuarios y registros, y puede revisar el historial de cambios de cada registro.

Desarrollada en **Kotlin** con **Jetpack Compose**, arquitectura **MVVM**, base de datos local **SQLite (Room)** e integración con servicio REST vía **Retrofit2**.

## Estudiantes

- César Saavedra
- Emilio Rojo

## Funcionalidades implementadas

### Inicio de sesión y roles
- Login con usuario y contraseña (validación remota mediante API REST y fallback local en Room). Las contraseñas se guardan como hash SHA-256.
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
- **Edición de registros guardados:** cada cambio queda en el historial con fecha, hora, quién lo hizo y qué campos se modificaron (sincronizado con backend mediante REST API).
- Animación de carga al guardar y mensaje de confirmación "Registro guardado".

### Administrador
- **Clientes:** crear, editar y eliminar clientes, sus instalaciones (con coordenadas GPS) y los tipos de equipo de cada instalación (sincronizado vía Retrofit y Room).
- **Usuarios:** crear, editar y eliminar técnicos y administradores vía API REST y Room. Un administrador no puede eliminarse ni quitarse el rol a sí mismo.
- **Registros anteriores:** revisar y editar los registros de todos los técnicos.
- **Archivar en vez de eliminar (Eliminado lógico):**
  - Los registros "eliminados" pasan a la sección **Archivados** mediante endpoint de red (`PUT /api/registros/archive/{id}`), visibles solo para administradores.
  - Desde ahí se pueden **restaurar** (`PUT /api/registros/restore/{id}`).

### Datos y Red
- **Persistencia local:** base de datos **SQLite con Room** (8 tablas relacionadas) con soporte de migraciones.
- **Integración de Red REST API:** cliente **Retrofit2** configurado para entorno `localhost` (`http://10.0.2.2:8080/`) con convertidor Gson para la sincronización de credenciales, clientes, mantenimientos, pruebas eléctricas y archivado de registros.
- Tráfico HTTP sin cifrado (`usesCleartextTraffic`) habilitado en el manifiesto para entornos de desarrollo local.
- Los formularios no pierden lo escrito al **girar el teléfono**, porque el estado vive en los ViewModel.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje |
| Jetpack Compose + Material 3 | Interfaz de usuario |
| MVVM (ViewModel + StateFlow) | Arquitectura |
| Navigation Compose | Navegación entre pantallas |
| Room (SQLite) + KSP | Base de datos local |
| Retrofit 2 + Gson | Cliente HTTP / API REST |
| Corrutinas y Flow | Tareas en segundo plano y datos reactivos |
| Activity Result API + FileProvider | Cámara y galería |
| LocationManager | Ubicación GPS |
| SharedPreferences | Sesión iniciada |

## Estructura del proyecto

```
app/src/main/java/com/example/appnombre_grupo1/
├── MainActivity.kt          Punto de entrada de la app
├── InduztekApp.kt           Clase Application (crea el AppContainer)
├── di/                      AppContainer: base de datos, servicio Retrofit y repositorios compartidos
├── data/                    DATOS: modelos, base de datos local (Room) y cliente API (Retrofit)
│   ├── model/               Modelos de dominio, validaciones y repositorios
│   │   ├── database/        Entidades (tablas), DAOs, base de datos SQLite y migraciones
│   │   ├── repository/      Repositorios (usuarios, clientes, registros, fotos)
│   │   └── validation/      Validación de formularios
│   └── remote/              Cliente de red (Retrofit2, ApiService, DTOs)
├── viewmodels/              VIEWMODEL: estado y lógica de cada pantalla
├── navigation/              Rutas y NavHost (Screen, AppNavigation)
└── ui/                      VIEW: pantallas y componentes en Compose
    ├── screens/             Login, formularios, administración y registros
    ├── components/          Componentes reutilizables (selectores, foto, permisos)
    └── utils/               Ubicación GPS y formato de fechas
```

## Pasos para ejecutar

### Requisitos
- **Android Studio** en una versión reciente, compatible con Android Gradle Plugin 9.3.
- **Android SDK 37**.
- Un teléfono o emulador con **Android 8.1 (API 27) o superior**.
- **Servidor Backend Local (opcional para pruebas en red):** corriendo en `localhost:8080` (en emulador Android se accede mediante `http://10.0.2.2:8080/`).

### Ejecutar
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Cesar-Saavedra/KotlinPruebas.git
   ```
2. En Android Studio: **File → Open** y seleccionar la carpeta `KotlinPruebas`.
3. Esperar a que termine la sincronización de Gradle (`File → Sync Project with Gradle Files`).
4. Elegir un emulador o conectar un teléfono con depuración USB.
5. Presionar **Run ▶** (`Shift + F10`).

### Usuarios de prueba
La primera vez que se abre la app se crean estos usuarios y los clientes de prueba **Jumbo** y **Falabella**, cada uno con dos instalaciones con coordenadas GPS:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin` | Administrador |
| `user` | `user` | Técnico |

### Probar la cámara y el GPS en el emulador
- **Cámara:** en el Device Manager, configurar la cámara como *Emulated* o *VirtualScene*.
- **GPS:** fijar una ubicación cercana a una instalación en *Extended controls (···) → Location* (ej. Jumbo Costanera Center: `-33.4173, -70.6062`) y presionar **Auto-detectar**.
