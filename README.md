# ModoGuardian

ModoGuardian es una aplicación Android para el monitoreo de seguridad, pensada para guardias y equipos de vigilancia. Permite iniciar sesión con distintos roles, ver cámaras, eventos y alertas, y activar o desactivar el "Modo Guardián" del sistema.

Proyecto desarrollado para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (sección 002D).

## Autores

- José Cuevas
- Salvador Briceño

## Funcionalidades

- **Inicio de sesión** con correo y contraseña, o con una cuenta de Google, usando Firebase Authentication.
- **Roles de usuario** guardados en Firestore. Cada rol ve y puede hacer cosas distintas.
- **Pantalla de inicio** adaptada al rol de la persona.
- **Cámaras**: lista y detalle de cámaras obtenidas desde una API REST.
- **Eventos y alertas** de seguridad, con pantalla de detalle para cada alerta.
- **Historial** de acciones realizadas, guardado en el teléfono.
- **Modo Guardián**: se puede activar o desactivar y la app recuerda el estado.
- **Publicaciones (Posts)**: crear, ver, editar y eliminar, conectadas a la API.
- **Perfil** de usuario, con acceso a la cámara del teléfono (pide permiso).
- **Tema claro/oscuro** configurable.
- **Funciona sin conexión**: si la API falla, la app muestra la última copia guardada en el teléfono.
- **Diseño adaptable** a celular, tablet y pantalla horizontal.

## Roles

| Rol | Qué puede hacer |
|---|---|
| Admin | Acceso total: gestión de usuarios, eventos y Modo Guardián |
| Supervisor | Ver eventos y activar o desactivar el Modo Guardián |
| Operador | Solo ver los eventos de seguridad (solo lectura) |

Si alguien entra con Google por primera vez, se crea automáticamente con el rol **Operador**, que es el de menos permisos.

## Cuentas de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin@guardian.test | 123456 | Admin |
| supervisor@guardian.test | 123456 | Supervisor |
| operador@guardian.test | 123456 | Operador |

También se puede entrar con **cualquier cuenta de Google** (ver la sección siguiente).

## Cómo ejecutar el proyecto

### Requisitos

- Android Studio actualizado.
- Un emulador **con Google Play** (en el Device Manager, los que tienen el ícono de Play Store) o un teléfono físico.
- Una cuenta de Google agregada en el emulador o teléfono, para probar el login con Google.
- Conexión a internet (la app usa Firebase y una API en la nube).

### Pasos

1. Clonar el repositorio:
   ```
   git clone -b guardian_final https://github.com/jose212019/DSY1105-002D-JCUEVAS-JBRICENO.git
   ```
   (La versión final del proyecto está en la rama `guardian_final`.)
2. Abrir la carpeta con Android Studio y esperar a que termine el Gradle Sync.
3. Elegir un emulador (con Google Play) y presionar **Run**.
4. Iniciar sesión con una cuenta de prueba o con el botón de Google.

Para ver el mensaje de error de conexión, activar "Simular sin conexión (demo)" en el login e intentar entrar.

### Sobre el login con Google en cualquier computador

Google valida que la app esté firmada con una llave conocida (huella SHA-1). Para que el login funcione en cualquier PC, el proyecto incluye una llave de depuración compartida en `app/debug.keystore`, que ya está configurada en `app/build.gradle.kts` y registrada en Firebase. Es una llave solo para pruebas, no se usa para publicar la app.

Si el login con Google falla:

- Revisar que el emulador tenga Google Play y una cuenta de Google agregada.
- Desinstalar la app del emulador y volver a ejecutarla.
- Confirmar que existe el archivo `app/google-services.json`.

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM con StateFlow y SharedFlow
- Navigation Compose
- Firebase Authentication y Cloud Firestore
- Google Sign-In con Credential Manager
- Retrofit y Gson para consumir la API REST (MockAPI)
- Room para la base de datos local
- DataStore Preferences (estado del Modo Guardián y tema)
- Coil para cargar imágenes
- Window Size Classes (diseño adaptable)
- Pruebas: JUnit, Kotest y MockK (unitarias) y Compose UI Test (de interfaz)

## Organización del código

```
modoguardian/
├── MainActivity.kt   (punto de entrada y navegación)
├── data/
│   ├── local/        (Room: base de datos, entidades y DAOs)
│   ├── remote/       (Retrofit: API y endpoints)
│   ├── model/        (modelos de la API y conversores)
│   └── ...           (DataStore y ayudante de Google Sign-In)
├── model/            (roles, estados, errores y modelos de la app)
├── navigation/       (pantallas y eventos de navegación)
├── repository/       (acceso a datos: Firebase, API y Room)
├── viewmodel/        (lógica de cada pantalla)
└── ui/               (pantallas, componentes, tema y utilidades)
```

## Cómo funcionan los datos

- **Usuarios y roles:** Firebase Authentication identifica a la persona y Firestore guarda su rol.
- **Cámaras, eventos y posts:** se piden a la API REST con Retrofit.
- **Copia local:** los eventos se guardan en Room. Si no hay conexión o la API falla, se muestra esa copia y la app avisa que son datos guardados.
- **Historial de acciones:** cada acción importante queda registrada en Room.
- **Preferencias:** el estado del Modo Guardián y el tema se guardan con DataStore.

## Pruebas

Resultado de las pruebas unitarias (17-11-2026): **63 pruebas, 0 fallas, 100% exitosas.**

El reporte completo está en `reportes/testDebugUnitTest/index.html` (se abre con cualquier navegador).

Las pruebas unitarias están en `app/src/test` y cubren repositorios, ViewModels y roles. Las pruebas de interfaz están en `app/src/androidTest`.

Para correrlas desde la terminal, en la carpeta del proyecto:

```
./gradlew testDebugUnitTest
```

(En Windows: `gradlew.bat testDebugUnitTest`.)

También se pueden correr en Android Studio con clic derecho sobre la carpeta `test` y **Run Tests**.

Al correrlas, el reporte nuevo se genera en `app/build/reports/tests/testDebugUnitTest/index.html`.
