# Agenda Px

Aplicación Android para estudiantes de la Universidad Privada Antenor Orrego (UPAO) que permite consultar notas académicas, horarios de clases y calculadora de promedios en tiempo real.

## Funcionalidades

- **Login seguro** contra el portal UPAO PX (sin almacenar credenciales)
- **Consulta de notas** por período académico con detalle de componentes y subcomponentes
- **Calculadora de promedio** con cálculo automático de nota final necesaria
- **Horario semanal** interactivo con selección de días
- **Sincronización en segundo plano** cada 30 minutos con WorkManager
- **Notificaciones** cuando se actualizan las notas
- **Selector de tema** claro / oscuro / seguir configuración del sistema
- **Cache local** para funcionamiento offline

## Tecnologías

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin |
| UI | XML Layouts + ViewBinding |
| Temas | Material Design 3 (Material3 Light/Dark) |
| Arquitectura | Single Activity + Fragments |
| Red | HttpURLConnection (vanilla) |
| Persistencia | DataStore Preferences + SharedPreferences |
| Background | WorkManager (sincronización periódica) |
| Notificaciones | NotificationCompat + NotificationChannel |
| Coroutines | Kotlin Coroutines + Lifecycle Scope |
| Backend | Node.js + Playwright scraper ([Upao_PX Backend](https://github.com/alessandrorr1007-debug/Upao_.git)) |

## Requisitos

- Android Studio Ladybug (2024.2+) o superior
- JDK 11+
- Android SDK 36
- Dispositivo con Android 7.0 (API 24) o superior

## Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/alessandrorr1007-debug/AppUni-Android.git
   ```

2. Abrir el proyecto en Android Studio

3. Esperar a que Gradle sincronice las dependencias

4. Conectar un dispositivo Android o iniciar un emulador

5. Presionar **Run** (▶) o ejecutar:
   ```bash
   ./gradlew installDebug
   ```

6. Iniciar sesión con tus credenciales UPAO

## Estructura del Proyecto

```
app/src/main/
├── java/com/example/agendapx/
│   ├── MainActivity.kt                  # Activity principal con navegación
│   ├── adapter/
│   │   └── HorarioAdapter.kt            # Adapter del RecyclerView de horario
│   ├── data/
│   │   ├── AppConstants.kt              # URLs, constantes, utilidades de fecha
│   │   ├── Curso.kt                     # Data class de cursos
│   │   ├── HorarioData.kt               # Datos del horario semanal
│   │   ├── NetworkUtils.kt              # Utilidades HTTP (GET/POST)
│   │   ├── ThemeManager.kt              # Gestión de temas claro/oscuro
│   │   └── UserPreferences.kt           # Persistencia de sesión y preferencias
│   ├── theme/
│   │   └── ThemeHelper.kt               # Helper para resolver colores del tema
│   └── ui/
│       ├── horario/
│       │   └── HorarioFragment.kt       # Pantalla de horario semanal
│       ├── inicio/
│       │   └── InicioFragment.kt        # Dashboard con resumen y calculadora
│       ├── login/
│       │   └── LoginActivity.kt         # Pantalla de inicio de sesión
│       └── notas/
│           ├── NotasFragment.kt         # Pantalla de notas con detalle
│           ├── NotasNotificacionHelper.kt # Sistema de notificaciones
│           └── NotasSyncWorker.kt       # Worker de sincronización en background
├── res/
│   ├── drawable/                        # Recursos gráficos (gradientes, formas)
│   ├── layout/                          # Layouts XML (6 archivos)
│   ├── values/                          # Colores, estilos, temas (light)
│   ├── values-night/                    # Colores y temas (dark)
│   └── xml/                             # Reglas de backup
```

## Temas

La aplicación soporta 3 modos de tema:

- **☀ Claro** (predeterminado) — Fondo blanco, tarjetas limpias, sombras suaves
- **🌙 Oscuro** — Fondo oscuro, colores vibrantes sobre superficies oscuras
- **⚙ Sistema** — Sigue la configuración del dispositivo

El selector de tema se encuentra en la pantalla de **Inicio** > **Configuración**.

## Backend

La aplicación se comunica con un backend Node.js que realiza scraping del portal UPAO PX usando Playwright. El backend está desplegado en Render.com.

- **Backend URL**: `https://upao-px-backend.onrender.com`
- **Repositorio Backend**: [Upao_PX](https://github.com/alessandrorr1007-debug/Upao_.git)

## Autor

**Alessandro Rodriguez**
- GitHub: [@alessandrorr1007-debug](https://github.com/alessandrorr1007-debug)

## Licencia

Este proyecto es de uso académico.
