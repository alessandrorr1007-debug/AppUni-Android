# AppUni-Android

Aplicación Android para estudiantes de la **Universidad Privada Antenor Orrego (UPAO)** que permite consultar notas académicas, horarios de clases, asistencia y calcular promedios en tiempo real. La app se conecta con el portal UPAO a través de un backend seguro con autenticación JWT.

---

## 📱 Descargar AppUni-Android

[⬇️ Descargar APK de AppUni-Android](https://github.com/alessandrorr1007-debug/AppUni-Android/releases/latest)

> **Nota:** El enlace de descarga redirige a la última Release publicada en GitHub. Si ves "Aquí no hay ninguna edición", significa que aún no se ha creado una Release. El APK estará disponible en GitHub Releases → AppUni-Android APK una vez que se publique la primera versión.

> Versión actual: **1.1.0** · Requiere Android 7.0 o superior

---

## 🚀 Instalación

1. Descargar el APK desde el enlace anterior.
2. Si Android muestra una advertencia, habilitar **"Fuentes desconocidas"** en Configuración > Seguridad.
3. Abrir el archivo APK descargado.
4. Presionar **Instalar**.
5. Abrir la aplicación y comenzar a usarla.

---

## ✨ Características principales

- **Inicio de sesión seguro** — Autenticación JWT contra el portal UPAO PX, sin almacenar credenciales
- **Consulta de notas** — Por período académico con detalle de componentes (EP1, Parcial, EP2, Final) y subcomponentes
- **Calculadora de promedio** — Cálculo automático de la nota final necesaria para aprobar cada curso
- **Horario semanal** — Vista interactiva con selección de días (Lunes a Sábado)
- **Asistencia** — Consulta porcentaje de asistencia por materia con estados (Aprobado / En riesgo / Reprobado)
- **Sincronización en segundo plano** — Actualización automática cada 30 minutos con WorkManager
- **Notificaciones** — Alertas cuando se detectan cambios en las notas
- **Tema claro / oscuro / sistema** — Selector de tema en la pantalla de Inicio
- **Actualizaciones in-app** — Detección automática de nuevas versiones desde GitHub Releases
- **Modo offline** — Caché local que muestra la última información guardada sin conexión

---

## 🛠️ Tecnologías utilizadas

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin |
| UI | XML Layouts + ViewBinding |
| Diseño | Material Design 3 (Material3 Light/Dark) |
| Arquitectura | Single Activity + Fragments |
| Red | HttpURLConnection |
| Persistencia | DataStore Preferences + SharedPreferences |
| Background | WorkManager (sincronización periódica) |
| Notificaciones | NotificationCompat + NotificationChannel |
| Coroutines | Kotlin Coroutines + Lifecycle Scope |
| Backend | Node.js + Express + Playwright |
| Seguridad | JWT (JSON Web Tokens) + Rate Limiting |
| Despliegue | Render.com (backend) + GitHub Releases (APK) |

---

## 📸 Capturas de pantalla

> Agregar imágenes de las principales pantallas de la aplicación.

![Pantalla de login](screenshots/login.png)
![Pantalla de notas](screenshots/notas.png)
![Pantalla de horario](screenshots/horario.png)

---

## 👨‍💻 Instalación para desarrolladores

### Requisitos

- Android Studio Ladybug (2024.2) o superior
- JDK 21 (incluido en Android Studio JBR)
- Android SDK 36
- Dispositivo con Android 7.0 (API 24) o superior

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/alessandrorr1007-debug/AppUni-Android.git
   ```

2. Abrir el proyecto en Android Studio.

3. Esperar a que Gradle sincronice las dependencias.

4. Conectar un dispositivo Android o iniciar un emulador.

5. Presionar **Run** (▶) o ejecutar:

   ```bash
   ./gradlew installDebug
   ```

### Backend

La app requiere el backend activo para funcionar. El repositorio del backend está en:

[Upao_PX Backend](https://github.com/alessandrorr1007-debug/Upao_.git)

Para ejecutar el backend localmente:

```bash
git clone https://github.com/alessandrorr1007-debug/Upao_.git
cd "UPAO PX/backend"
npm install
npm run dev
```

### iOS

El repositorio de la versión nativa para iOS está en:

[AppUni-Ios](https://github.com/alessandrorr1007-debug/AppUni-Ios)

---

## 📌 Versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.1.0 | Julio 2026 | Sistema de actualizaciones in-app, fix de errores de compilación, migración a coroutines |
| 1.0.0 | Junio 2026 | Versión inicial: login, notas, horario, calculadora, temas claro/oscuro |


