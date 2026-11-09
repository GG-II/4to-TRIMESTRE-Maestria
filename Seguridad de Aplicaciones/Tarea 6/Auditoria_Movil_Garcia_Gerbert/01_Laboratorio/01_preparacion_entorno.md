# 1. Preparación del entorno

**Emulador:** MuMuPlayer (NetEase), instancia local index 0, Android 15.0, arquitectura x86_64, root vía KernelSU.

**Estado ADB:**
```
List of devices attached
127.0.0.1:16384        device product:a15x model:SM_A156E device:a15x transport_id:2
```
Conexión establecida vía `adb connect 127.0.0.1:16384` (puerto no estándar, propio de MuMu).

**Versión Android:** `ro.build.version.release` = 15

**Origen de la aplicación:** El APK oficial de DIVA (payatu/diva-android) ya no se distribuye como binario precompilado (enlaces originales caídos: GitHub sin releases/APK en el árbol del repo, payatu.com sin el archivo). Se compiló desde el código fuente oficial (`github.com/payatu/diva-android`, commit del branch master), actualizando únicamente el tooling de build (Android Gradle Plugin, Gradle wrapper, `compileSdkVersion`→34, `targetSdkVersion`→24 para cumplir el mínimo de instalación de Android 15) sin tocar la lógica ni el código fuente original de los desafíos.

**Paquete:** `jakhar.aseem.diva`, versionName 1.0, primera instalación 2026-11-09.

**Permisos solicitados (manifest + reescritos por el sistema moderno de medios):**
```
android.permission.INTERNET
android.permission.READ_EXTERNAL_STORAGE
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.POST_NOTIFICATIONS
android.permission.READ_MEDIA_VISUAL_USER_SELECTED
android.permission.READ_MEDIA_IMAGES
android.permission.READ_MEDIA_AUDIO
android.permission.READ_MEDIA_VIDEO
android.permission.ACCESS_MEDIA_LOCATION
```
(Los permisos de medios/notificaciones son sintéticos, agregados automáticamente por Android al declarar `READ/WRITE_EXTERNAL_STORAGE` bajo un `targetSdkVersion` bajo en un sistema reciente.)

**Evidencia:** ver `evidencia/01_pantalla_inicial.png`, `evidencia/02_diva_corriendo.png`.
