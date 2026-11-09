# Guion técnico — DIVA Android (Tarea 06)

Paquete: `jakhar.aseem.diva` — repo: github.com/payatu/diva-android
Todo en emulador/dispositivo de pruebas, datos ficticios.

## 0. Preparación del entorno

```bash
adb devices
adb shell getprop ro.build.version.release
adb shell pm list packages | grep diva
adb shell dumpsys package jakhar.aseem.diva | grep -A5 "requested permissions"
```
Evidencia: captura de `adb devices`, versión Android, permisos listados.

## 1. Reconocimiento (JADX + Manifest)

- Abrir el APK en JADX-GUI → exportar/leer `AndroidManifest.xml`.
- Buscar: actividades exportadas (`android:exported="true"`), content providers, servicios, broadcast receivers.
- 5 superficies de ataque típicas en DIVA:
  1. `LogActivity` — logging de credenciales/datos sensibles con `Log.d`.
  2. `HardcodeActivity` — claves/API keys embebidas en el código decompilado.
  3. `InsecureDataStorageActivity1-4` — SharedPreferences, SQLite, archivo externo, backup.
  4. `SQLInjectionActivity` — concatenación de queries sin parametrizar.
  5. `AccessControlActivity1-3` + `APICredsActivity` — componentes exportados invocables vía `am start`/intents sin control de autenticación.

Evidencia: tabla componente → tipo → riesgo → prueba prevista.

## Bloque A

### 01 — Insecure Logging
```bash
adb logcat | grep -i diva
```
Ingresar credenciales ficticias en la actividad de login/registro de DIVA y capturar el logcat mostrando usuario/contraseña en claro.

### 02 — Hardcoding Issues (Part 1)
En JADX: clase `HardcodeActivity` → buscar cadena constante (vendor key). Confirmar uso ingresando el valor hallado en el campo de la app y validando que acepta/compara contra el hardcodeado.

### 03 — Insecure Data Storage (Part 1) — SharedPreferences
```bash
adb shell run-as jakhar.aseem.diva cat /data/data/jakhar.aseem.diva/shared_prefs/*.xml
```
Mostrar credenciales en texto plano tras login.

### 04 — Insecure Data Storage (Part 2) — SQLite
```bash
adb shell run-as jakhar.aseem.diva ls /data/data/jakhar.aseem.diva/databases/
adb shell run-as jakhar.aseem.diva sqlite3 /data/data/jakhar.aseem.diva/databases/<db> "select * from <tabla>;"
```

### 05 — Input Validation Issues (Part 1) — SQL Injection
En el campo de búsqueda/consulta de `SQLInjectionActivity`, probar:
```
' OR '1'='1
```
Capturar que devuelve todos los registros (bypass de filtro).

## Bloque B

### 06 — Insecure Data Storage (Part 3) — archivo externo / almacenamiento no cifrado
```bash
adb shell run-as jakhar.aseem.diva ls -la /data/data/jakhar.aseem.diva/
adb pull /sdcard/<archivo_creado_por_diva>
```

### 07 — Input Validation Issues (Part 2) — Access Control vía ContentProvider / IPC
Usar `content query`:
```bash
adb shell content query --uri content://jakhar.aseem.diva.provider.notesprovider
```
Demostrar lectura/escritura sin autenticación.

### 08 — Access Control Issues (Part 1)
Invocar actividad exportada directamente, saltando el flujo normal (p. ej. pantalla de "premium" sin pagar):
```bash
adb shell am start -n jakhar.aseem.diva/.APICredsActivity
```

### 09 — Access Control Issues (Part 2)
Identificar debilidad de verificación (p. ej. validación solo del lado cliente) y mostrar bypass editando/enviando el intent con extras manipulados.

### 10 — Access Control Issues (Part 3)
Exponer información sensible a través de un componente Android (broadcast receiver o provider) sin permisos declarados correctamente.

## Correcciones (sección 4)

**Corrección A — Logging.** Ubicar `Log.d(...)` con dato sensible en el código fuente DIVA (Java), reemplazar por log sin el valor sensible (o remover la línea), recompilar, repetir prueba 01 y confirmar que el logcat ya no expone el dato — con la función de login intacta.

**Corrección B — SQL Injection.** En `SQLInjectionActivity.java`, sustituir `Cursor cursor = db.rawQuery("select * from users where name='" + name + "'", null);` (patrón típico) por `db.query(..., selection, selectionArgs, ...)` o `PreparedStatement`/`rawQuery` con `?` y argumentos. Recompilar, repetir el payload `' OR '1'='1` y confirmar que ya no retorna todo, mientras una búsqueda legítima por nombre exacto sigue funcionando.

## Plantilla de registro por desafío (repetir x10 en 02_Explotaciones/Desafio_NN)

- Procedimiento ejecutado:
- Resultado observado:
- Evidencia (captura/log adjunto):
- Impacto (máx. 2 líneas):
- Corrección propuesta (máx. 2 líneas):
