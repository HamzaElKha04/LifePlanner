# LifePlanner

LifePlanner es una aplicación móvil Android orientada a la organización personal del usuario.
La app permite gestionar tareas, consultar un calendario semanal, controlar hábitos y editar un perfil personal desde una interfaz sencilla, visual y práctica.

El objetivo principal del proyecto es reunir en una sola aplicación varias herramientas que normalmente se utilizan por separado: una lista de tareas, un calendario y un seguimiento de hábitos.

---

## Descripción del proyecto

LifePlanner nace como una aplicación para ayudar al usuario a organizar mejor su día a día.

Desde la app, el usuario puede crear tareas, asignarles fecha, hora opcional y prioridad, consultar sus tareas por día, marcar tareas como completadas, gestionar hábitos semanales y ver un resumen general desde la pantalla principal.

La aplicación utiliza Firebase para gestionar los usuarios y almacenar los datos en la nube, permitiendo que cada cuenta tenga sus propias tareas, hábitos y perfil.

---

## Funcionalidades principales

### Autenticación de usuarios

La app permite:

* Registrar nuevos usuarios.
* Iniciar sesión con email y contraseña.
* Mantener la sesión iniciada.
* Cerrar sesión desde el perfil.
* Asociar los datos de la app al usuario autenticado.

La autenticación se gestiona mediante Firebase Authentication.

---

### Pantalla principal

La pantalla de inicio muestra un resumen general del estado del usuario dentro de la aplicación.

Desde esta pantalla se puede consultar rápidamente:

* Tareas del día.
* Hábitos pendientes o completados.
* Progreso semanal.
* Acceso rápido a la creación de tareas.
* Resumen visual de la actividad del usuario.

Esta pantalla funciona como un panel inicial para que el usuario vea de forma rápida cómo va su planificación.

---

### Gestión de tareas

LifePlanner incluye una gestión completa de tareas personales.

Cada tarea puede tener:

* Título.
* Fecha.
* Hora opcional.
* Prioridad.
* Estado de completada o pendiente.
* Usuario propietario mediante `userId`.

El usuario puede:

* Crear nuevas tareas.
* Editar tareas existentes.
* Marcar tareas como completadas.
* Volver a marcar tareas como pendientes.
* Filtrar tareas por estado.
* Eliminar tareas con confirmación.

Las tareas se muestran mediante tarjetas visuales para que la información sea clara y fácil de consultar.

---

### Calendario semanal

La app incluye una sección de calendario semanal que permite consultar tareas según el día seleccionado.

Desde el calendario, el usuario puede:

* Ver los días de la semana.
* Seleccionar una fecha concreta.
* Consultar las tareas asociadas a ese día.
* Crear una tarea directamente desde una fecha seleccionada.

Esta funcionalidad permite que la app no sea solo una lista de tareas, sino también una herramienta de planificación semanal.

---

### Gestión de hábitos

LifePlanner también permite crear y controlar hábitos semanales.

Cada hábito puede incluir:

* Nombre.
* Descripción.
* Icono o emoji.
* Color personalizado.
* Objetivo semanal.
* Fechas completadas.
* Usuario propietario mediante `userId`.

El usuario puede:

* Crear hábitos.
* Editar hábitos.
* Marcar hábitos como realizados.
* Eliminar hábitos.
* Consultar el progreso semanal.

El progreso se calcula usando las fechas en las que el usuario ha marcado el hábito como completado.

---

### Perfil de usuario

La aplicación incluye una pantalla de perfil donde el usuario puede consultar y editar información básica de su cuenta.

Desde esta sección se puede:

* Ver el email del usuario.
* Editar el nombre visible.
* Consultar estadísticas generales.
* Ver el estado de sincronización.
* Cerrar sesión.

---

## Tecnologías utilizadas

El proyecto ha sido desarrollado utilizando tecnologías actuales del entorno Android.

### Kotlin

Kotlin es el lenguaje principal utilizado para desarrollar la aplicación.
Permite crear código claro, moderno y seguro dentro del entorno Android.

### Jetpack Compose

Jetpack Compose se ha utilizado para crear la interfaz visual de la app.
Es una tecnología moderna de Android que permite diseñar las pantallas directamente con código Kotlin, utilizando componentes como botones, tarjetas, formularios, textos y barras de navegación.

### Firebase Authentication

Firebase Authentication se utiliza para gestionar:

* Registro de usuarios.
* Inicio de sesión.
* Usuario actual.
* Cierre de sesión.

Gracias a esta tecnología, cada usuario puede acceder a su propia cuenta dentro de la aplicación.

### Firebase Firestore

Firebase Firestore se utiliza como base de datos en la nube.
En Firestore se almacenan los datos principales de la aplicación:

* Tareas.
* Hábitos.
* Perfil de usuario.

Cada documento está asociado al `userId` del usuario autenticado, por lo que cada cuenta solo accede a sus propios datos.

### Android Studio

Android Studio ha sido el entorno de desarrollo utilizado para crear, probar y compilar la aplicación.

Desde Android Studio se ha realizado:

* Desarrollo del código.
* Diseño de pantallas con Jetpack Compose.
* Pruebas en emulador.
* Generación del APK.
* Revisión de errores y compilación final.

### Gradle

Gradle se utiliza para la configuración y construcción del proyecto Android, incluyendo dependencias, plugins y configuración general de la app.

---

## Estructura del repositorio

El repositorio está organizado para separar el README del proyecto Android.

```text
LifePlanner/
│
├── README.md
├── .gitignore
│
└── LifePlanner/
    ├── app/
    ├── gradle/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── gradlew
    └── gradlew.bat
```

La carpeta interior `LifePlanner` contiene el proyecto Android completo.

---

## Organización del código

Dentro del proyecto Android, el código está organizado en varios paquetes para mantener una estructura clara y fácil de mantener.

```text
app/src/main/java/com/hamza/lifeplanner/
│
├── data/
├── navigation/
├── ui/
│   ├── screens/
│   └── theme/
└── MainActivity.kt
```

---

### `data`

El paquete `data` contiene los modelos de datos y los repositorios.

Aquí se encuentran las clases que representan la información principal de la app y las clases que se encargan de comunicarse con Firebase.

Ejemplos:

* `Task`
* `Habit`
* `UserProfile`
* `AuthRepository`
* `TaskRepository`
* `HabitRepository`
* `UserProfileRepository`

Los modelos representan los datos.
Los repositorios gestionan las operaciones de lectura, creación, edición y eliminación.

---

### `navigation`

El paquete `navigation` contiene la navegación principal de la aplicación.

Aquí se define qué pantalla se muestra según el estado del usuario.

Por ejemplo:

* Si el usuario no ha iniciado sesión, se muestra Login.
* Si el usuario quiere crear una cuenta, se muestra Registro.
* Si el usuario ya ha iniciado sesión, se muestra la parte principal de la app.

---

### `ui/screens`

El paquete `ui/screens` contiene las pantallas visibles de la aplicación.

Algunas de las pantallas principales son:

* `LoginScreen`
* `RegisterScreen`
* `HomeScreen`
* `TasksScreen`
* `CalendarScreen`
* `HabitsScreen`
* `ProfileScreen`
* `MainTabsScreen`
* `ProgressScreen`

Estas pantallas están creadas con Jetpack Compose.

---

### `ui/theme`

El paquete `ui/theme` contiene la configuración visual general de la aplicación.

Incluye:

* Colores.
* Tema general.
* Tipografía.

Esto permite mantener una apariencia más coherente en toda la app.

---

## Firebase y almacenamiento de datos

LifePlanner utiliza Firebase para gestionar usuarios y almacenar los datos de la aplicación.

La app utiliza dos servicios principales:

### Firebase Authentication

Se encarga de la autenticación del usuario:

* Registrar usuario.
* Iniciar sesión.
* Obtener usuario actual.
* Cerrar sesión.

### Firebase Firestore

Se encarga de almacenar los datos principales de la app.

Las colecciones principales son:

```text
tasks
habits
user_profiles
```

---

### Colección `tasks`

Guarda las tareas creadas por los usuarios.

Cada tarea puede tener campos como:

```text
id
title
completed
userId
priority
dateText
timeText
```

Gracias al campo `userId`, cada tarea queda asociada al usuario que la creó.

---

### Colección `habits`

Guarda los hábitos creados por los usuarios.

Cada hábito puede tener campos como:

```text
id
title
subtitle
icon
colorHex
targetValue
completedDates
userId
```

El campo `completedDates` guarda las fechas en las que el hábito se ha marcado como realizado.
Con esa información se calcula el progreso semanal.

---

### Colección `user_profiles`

Guarda la información básica del perfil del usuario.

Puede incluir campos como:

```text
userId
email
displayName
```

---

## Arquitectura general

La aplicación sigue una organización sencilla basada en la separación de responsabilidades.

Las pantallas no trabajan directamente con Firebase.
En su lugar, las pantallas llaman a los repositorios, y los repositorios se encargan de comunicarse con Firebase.

Ejemplo:

```text
Pantalla de tareas
        ↓
TaskRepository
        ↓
Firebase Firestore
```

Esta estructura permite que el código sea más ordenado, más fácil de mantener y más sencillo de explicar.

---

## Flujo básico de la aplicación

El funcionamiento general de LifePlanner es el siguiente:

1. El usuario abre la aplicación.
2. Si no tiene sesión iniciada, aparece la pantalla de login.
3. El usuario puede iniciar sesión o registrarse.
4. Al iniciar sesión, accede a la pantalla principal.
5. Desde la pantalla principal puede consultar resumen de tareas y hábitos.
6. Puede crear tareas con fecha, hora y prioridad.
7. Puede consultar tareas desde el calendario.
8. Puede crear y marcar hábitos.
9. Puede editar su perfil.
10. Puede cerrar sesión.

---

## Pruebas realizadas

Durante el desarrollo se probaron las funcionalidades principales de la aplicación.

Entre las pruebas realizadas se incluyen:

* Registro de usuario.
* Inicio de sesión.
* Cierre de sesión.
* Creación de tareas.
* Edición de tareas.
* Eliminación de tareas.
* Marcado de tareas como completadas.
* Filtros de tareas.
* Consulta de tareas en calendario.
* Creación de hábitos.
* Marcado de hábitos como realizados.
* Cálculo del progreso semanal.
* Edición de perfil.
* Sincronización de datos con Firebase.
* Pruebas en emulador Android.
* Pruebas en dispositivo Android real mediante APK.

---

## Dificultades encontradas

Durante el desarrollo aparecieron varias dificultades técnicas.

### Gestión de fechas y horas

Una de las dificultades fue gestionar correctamente las fechas y horas de las tareas.

La app tenía que permitir crear tareas con fecha obligatoria y hora opcional.
Además, las tareas creadas desde el calendario debían respetar el día seleccionado.

Para resolverlo, se mantuvo un formato común de fecha y se permitió que la hora fuera opcional.

---

### Sincronización con Firebase

Otra dificultad fue conseguir que los cambios se reflejaran correctamente entre pantallas.

Por ejemplo, si una tarea se marcaba como completada, ese cambio debía verse también en Inicio, Mis tareas y Calendario.

Para solucionarlo, se centralizó la lógica en repositorios conectados a Firestore.

---

### Cálculo del progreso de hábitos

El progreso de hábitos debía calcularse según los días completados durante la semana.

Para ello, cada hábito guarda una lista de fechas completadas.
Con esas fechas, la app puede calcular cuántas veces se ha cumplido el hábito y compararlo con el objetivo semanal.

---

### Ajustes visuales

También se realizaron ajustes visuales en diferentes pantallas para mejorar la claridad de la interfaz.

Se ajustaron:

* Tarjetas.
* Espaciados.
* Tamaños de texto.
* Botones.
* Colores.
* Distribución de elementos.

---

## Cómo abrir el proyecto

Para abrir el proyecto en Android Studio:

1. Descargar o clonar el repositorio.
2. Abrir Android Studio.
3. Seleccionar `Open`.
4. Abrir la carpeta:

```text
LifePlanner/LifePlanner
```

5. Esperar a que Gradle sincronice el proyecto.
6. Ejecutar la app en un emulador o dispositivo Android.

---

## Requisitos recomendados

Para ejecutar el proyecto se recomienda:

* Android Studio instalado.
* JDK compatible con el proyecto.
* Emulador Android configurado o dispositivo Android físico.
* Conexión a internet para sincronizar con Firebase.
* Gradle configurado mediante el wrapper incluido en el proyecto.

---

## Uso de internet

La versión actual de LifePlanner está pensada para funcionar conectada a internet, ya que utiliza Firebase Authentication y Firebase Firestore.

La conexión es necesaria para:

* Registrar usuarios.
* Iniciar sesión.
* Guardar datos.
* Sincronizar tareas, hábitos y perfil.

El modo offline se plantea como una mejora futura.

---

## Mejoras futuras

Algunas posibles mejoras para futuras versiones son:

* Añadir notificaciones y recordatorios.
* Implementar modo offline.
* Añadir vista mensual del calendario.
* Crear estadísticas más avanzadas.
* Añadir modo oscuro personalizado.
* Mejorar la personalización del perfil.
* Añadir categorías para tareas.
* Añadir búsqueda de tareas.
* Permitir ordenar tareas por prioridad o fecha.
* Mejorar la visualización del progreso semanal y mensual.

---

## Estado del proyecto

El proyecto se encuentra en una versión funcional.

La aplicación incluye las funcionalidades principales planteadas:

* Autenticación.
* Tareas.
* Calendario.
* Hábitos.
* Perfil.
* Firebase.
* APK probado en entorno real.

---

## Autor

Proyecto desarrollado por Hamza.
Proyecto académico de 2º DAM.
Junio de 2026.
