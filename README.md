# GAME TURBO AI

Centro de control gaming nativo para Android desarrollado en **Kotlin + Jetpack Compose + Material 3**, enfocado en telemetría real del sistema, gestión de videojuegos, perfiles de rendimiento compatibles con Android, almacenamiento seguro y consola administrativa externa para Termux.

> **Aviso de transparencia técnica:** `GAME TURBO AI` es únicamente el nombre comercial del proyecto. La aplicación **no contiene modelos de inteligencia artificial, LLMs, Gemini ni chatbots**. Todas las métricas provienen exclusivamente de las APIs oficiales del SDK de Android.

---

## Arquitectura del Proyecto

```text
app/src/main/java/com/example/MainActivity.kt       # Punto de entrada Edge-to-Edge
app/src/main/java/com/gameturbo/ai/
├── MainEntry.kt          # Splash animado + Navegación adaptativa (NavigationBar / NavigationRail)
├── dashboard/            # Panel principal y polling periódico eficiente
├── games/                # Game Booster (detección, registro manual, perfiles por juego, sesiones)
├── performance/          # Modelos y lectores reales del SDK + Hub de monitores
├── memory/               # Monitor de RAM (ActivityManager.MemoryInfo + historial Room)
├── cpu/                  # Monitor de CPU (arquitectura, núcleos, frecuencias accesibles con control SELinux)
├── temperature/          # Monitor térmico (temperatura de batería + PowerManager.currentThermalStatus)
├── battery/              # Monitor de batería (nivel, estado, voltaje, tecnología, salud, corriente)
├── storage/              # Administrador de almacenamiento (StatFs + limpieza confirmada de archivos propios)
├── profiles/             # Perfiles (Equilibrado, Gaming, Ahorro de batería, Personalizado)
├── advanced/             # Modo Avanzado (Normal, Shizuku, ADB, Root) + Auditoría de seguridad
├── settings/             # Herramientas Gaming (Dispositivo, Pantalla, Red, Servicios, Accesos del sistema)
├── security/             # Validación de rutas sandbox, sanitización de entradas y lanzamiento seguro
└── data/                 # Room Database + Jetpack DataStore + Repositorios
gameturbo-admin/          # Consola administrativa externa independiente para Termux (fuera del menú de la app)
.github/workflows/        # Flujo CI/CD de GitHub Actions
```

---

## Permisos y Capacidades Avanzadas (Normal / Shizuku / ADB / Root)

1. **Modo Normal (Sin Root):** Funciona de serie con APIs oficiales (`ActivityManager`, `StatFs`, `BatteryManager`, `PowerManager`, `PackageManager` con `<queries>`).
2. **Permisos Opcionales de Usuario:**
   - `WRITE_SETTINGS` (ajuste de brillo global): se solicita mediante la pantalla oficial de Android.
   - Acceso a **No Molestar (DND)**: para automatizar el modo concentración del sistema cuando el usuario lo autorice.
3. **Modo Shizuku:** Verifica si `moe.shizuku.privileged.api` está instalado y detalla las operaciones que requieren autorización explícita dentro de Shizuku.
4. **Modo ADB:** Comprueba si la depuración ADB está activa en el sistema y documenta comandos ejecutables por el propietario desde PC/Depuración inalámbrica (`adb shell dumpsys battery`, `dumpsys thermalservice`, `cmd game mode`).
5. **Modo Root (Opcional):** Solo comprueba la presencia pasiva del binario `su`. La aplicación **no ejecuta comandos privilegiados automáticamente** ni incluye puertas traseras.

---

## Consola Privada de Termux (`gameturbo-admin`)

Esta herramienta administrativa es externa al menú de la aplicación Android y se ejecuta en **Termux** o Linux Bash:

### Instalación en Termux
```bash
chmod +x gameturbo-admin/scripts/install.sh
./gameturbo-admin/scripts/install.sh
```

### Comandos disponibles
```bash
gameturbo-admin help                # Muestra la ayuda completa
gameturbo-admin status              # Estado del proyecto, configuración y APK Debug + SHA-256
gameturbo-admin modules             # Lista los módulos Kotlin y extensiones administrativas
gameturbo-admin verify              # Verifica archivos críticos y ausencia de APIs de IA
gameturbo-admin build [debug|rel]   # Compila el APK Debug o Release y calcula SHA-256
gameturbo-admin update              # Actualiza permisos y verifica entorno Gradle
gameturbo-admin backup              # Genera copia de seguridad .tar.gz con suma SHA-256
gameturbo-admin logs                # Muestra el registro de operaciones administrativas
```

---

## Compilación Profesional y Firma Release

### Compilación Debug local
```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

### Compilación Release firmada (sin guardar claves en el repositorio)
Configura las variables de entorno o los **GitHub Secrets** (`ANDROID_KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`) antes de compilar:
```bash
export KEYSTORE_PATH="/ruta/segura/my-upload-key.jks"
export STORE_PASSWORD="tu_password_keystore"
export KEY_PASSWORD="tu_password_alias"
gradle :app:assembleRelease
sha256sum app/build/outputs/apk/release/app-release.apk
```
