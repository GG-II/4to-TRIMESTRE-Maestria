# Desafío 07 — Input Validation Issues (Part 2) — URI Scheme / WebView

**Procedimiento:**
1. Navegación: `MainActivity` → "8. Input Validation Issues - Part 2" (`InputValidation2URISchemeActivity`). Actividad no exportada; alcanzada mediante el flujo normal de la app.
2. El `WebView` tiene JavaScript habilitado y carga directamente cualquier URL ingresada por el usuario sin restricción de esquema.
3. Payload: `file:///data/data/jakhar.aseem.diva/shared_prefs/jakhar.aseem.diva_preferences.xml` en el campo "Enter the URL to view", toque en "VIEW".

**Resultado observado:** El WebView renderiza el contenido completo del archivo interno de la app:
```xml
<map>
  <string name="password">SuperSecret123</string>
  <string name="user">testuser</string>
</map>
```
(credenciales ficticias guardadas en el Desafío 03), demostrando lectura arbitraria de archivos internos de la aplicación vía esquema `file://` sin validación.

**Código vulnerable:** `java/jakhar/aseem/diva/InputValidation2URISchemeActivity.java`:
```java
wset.setJavaScriptEnabled(true);
...
wview.loadUrl(uriText.getText().toString());
```
Sin whitelist de esquemas (`http`/`https`) ni sanitización del input antes de `loadUrl`.

**Evidencia:** `02_resultado_file_uri.png`, `03_webview_file_content.png`.

**Impacto:** Lectura arbitraria de archivos del sandbox de la app (credenciales, bases de datos, configuración) por medio del esquema `file://`. Con JavaScript habilitado en el WebView, la superficie de ataque aumenta (`javascript:`, contenido web no confiable). El riesgo crece si la pantalla recibiera URLs de fuentes externas; en este laboratorio el dato se ingresa manualmente.

**Corrección propuesta:** Restringir `loadUrl` a esquemas `https` mediante validación explícita (`Uri.parse(input).getScheme()`), deshabilitar `file://`/`javascript://` con `setAllowFileAccess(false)` y `setAllowFileAccessFromFileURLs(false)`, y deshabilitar JavaScript si no es estrictamente necesario.
