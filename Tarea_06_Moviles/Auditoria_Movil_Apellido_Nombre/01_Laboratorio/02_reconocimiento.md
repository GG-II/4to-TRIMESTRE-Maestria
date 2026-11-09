# 2. Reconocimiento técnico

**Herramienta:** JADX 1.5.1 (CLI), decompilación del APK instalado (`jadx_output/`). Se contrasta contra el código fuente oficial compilado (`diva-source/app/src/main/`).

## Componentes relevantes (AndroidManifest.xml)

| Componente | Tipo | Exportado | Observación |
|---|---|---|---|
| `.MainActivity` | Activity | Sí (LAUNCHER) | Punto de entrada, menú de desafíos |
| `.LogActivity` | Activity | No declarado (no exported=false explícito → no-exported por defecto, sin intent-filter) | Desafío 01 |
| `.HardcodeActivity` | Activity | No | Desafío 02 |
| `.InsecureDataStorage1-4Activity` | Activity | No | Desafíos 03-06 |
| `.SQLInjectionActivity` | Activity | No | Desafío 05/07 (inyección SQL) |
| `.InputValidation2URISchemeActivity` | Activity | No | Maneja URI scheme — validación de entrada |
| `.AccessControl1Activity` | Activity | No | Desafío 08 |
| `.APICredsActivity` | Activity | **Sí**, intent-filter `jakhar.aseem.diva.action.VIEW_CREDS` | Invocable directo por intent externo sin pasar por flujo normal |
| `.AccessControl2Activity` | Activity | No | Desafío 09 |
| `.APICreds2Activity` | Activity | **Sí**, intent-filter `jakhar.aseem.diva.action.VIEW_CREDS2` | Igual que arriba |
| `.NotesProvider` | **ContentProvider** | **Sí** (`android:exported="true"`), authority `jakhar.aseem.diva.provider.notesprovider` | Acceso directo vía `content query` sin autenticación |
| `.AccessControl3Activity` | Activity | No | Desafío 10 |

## 5 superficies de ataque identificadas

1. **Logging del sistema (logcat)** — `LogActivity` escribe credenciales/datos de prueba con `Log.d/Log.v`. Prueba: `adb logcat` mientras se usa la actividad.
2. **Componentes exportados invocables por intent** (`APICredsActivity`, `APICreds2Activity`) — accesibles sin pasar por el flujo normal de la app. Prueba: `adb shell am start -a jakhar.aseem.diva.action.VIEW_CREDS`.
3. **ContentProvider expuesto** (`NotesProvider`, `exported=true`, sin permisos declarados) — lectura/escritura externa. Prueba: `adb shell content query --uri content://jakhar.aseem.diva.provider.notesprovider`.
4. **Almacenamiento local sin cifrar** (SharedPreferences, SQLite, archivos en almacenamiento externo) — varias activities de `InsecureDataStorage`. Prueba: `adb shell run-as jakhar.aseem.diva` + lectura directa de `/data/data/jakhar.aseem.diva/`.
5. **Consultas SQL concatenadas sin parametrizar** (`SQLInjectionActivity`) — inyección SQL vía campo de entrada. Prueba: payload `' OR '1'='1` en el campo de búsqueda.

Evidencia: `evidencia/03_manifest.png` (o archivo manifest adjunto), árbol de clases en `jadx_output/`.
