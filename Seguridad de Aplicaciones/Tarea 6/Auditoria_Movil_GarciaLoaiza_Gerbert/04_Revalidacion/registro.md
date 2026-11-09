# 5. Pruebas de regresión

APK recompilado con las dos correcciones aplicadas (`diva-corregido.apk`, instalado reemplazando la versión vulnerable). Build exitosa (`BUILD SUCCESSFUL`) confirmando que el código corregido compila y la app es funcional.

## Corrección A — Insecure Logging

**Repetición de la prueba del Desafío 01:** ingreso de `4111111111111111` en "Insecure Logging" → "CHECK OUT".

**Resultado tras la corrección (repetido en esta revisión, build limpia desde el APK final con ambas correcciones):**
```
11-09 15:45:40.718  3253  3253 E diva-log: Error while processing transaction. Card ref: ****1111
```
El log ya no expone el número completo; solo últimos 4 dígitos (referencia no reversible). El comportamiento de la app (mensaje de error al usuario) se mantiene sin cambios.

**Comportamiento inseguro eliminado:** ✅ Sí — ya no es posible recuperar el PAN completo desde logcat.
**Funcionalidad legítima:** ✅ Se mantiene — el flujo de "checkout" sigue mostrando el toast de error esperado.

**Evidencia:** `04_logcat_corregido.txt` (logcat crudo, filtrado por tag `diva-log`).

## Corrección B — SQL Injection

**Repetición de la prueba del Desafío 05:** payload `x' OR '1'='1` en "Input Validation Issues - Part 1" → "SEARCH".

**Resultado tras la corrección:** sin excepciones en logcat (`Diva-sqli` no reporta errores). Verificación directa en la base de datos tratando el payload como valor literal exacto (equivalente exacto al efecto de `rawQuery` parametrizado):
```
SELECT * FROM sqliuser WHERE user = 'x'' OR ''1''=''1';   -- (comillas escapadas = valor literal)
→ 0 filas
```
El payload ya no altera la estructura de la consulta; se compara como cadena literal contra la columna `user` y no coincide con ningún usuario.

**Captura real en pantalla** (fotograma extraído con `adb shell screenrecord` + `ffmpeg`, igual que en el Desafío 05, para capturar el Toast en el instante exacto): `04_sqli_payload_sin_resultado.png` — muestra el toast `User: (x' OR '1'='1) not found`, confirmando que el motor SQLite trata todo el payload como el literal exacto a buscar en la columna `user`, sin ninguna fila que lo iguale.

**Repetición de búsqueda legítima** (`admin`, mencionada en el propio hint de la app):
```
SELECT * FROM sqliuser WHERE user = 'admin';
→ admin|passwd123|1234567812345678
```
El caso de uso normal (buscar un usuario existente) sigue funcionando exactamente igual que antes de la corrección. Captura real: `05_sqli_busqueda_admin.png` — toast `User: (admin) pass: (passwd123) Credit card: (1234567812345678)`.

**Comportamiento inseguro eliminado:** ✅ Sí — ya no es posible extraer todos los usuarios con un payload malicioso.
**Funcionalidad legítima:** ✅ Se mantiene — la búsqueda por nombre exacto de usuario funciona sin cambios.

**Evidencia:** `03_sqli_regresion.txt`, `02_busqueda_legitima.png` (captura de campo, sesión anterior), `04_sqli_payload_sin_resultado.png` (toast real del payload, sin resultado), `05_sqli_busqueda_admin.png` (toast real de la búsqueda legítima), `01_sqli_corregido_payload_no_resultado.png` (campo con el payload antes de pulsar SEARCH).

## Nota sobre capturas de terminal (Desafíos 04 y 10)

Se solicitó opcionalmente capturas de terminal de los comandos `sqlite3` (D04) y `content query` (D10). Este entorno no tiene una ventana de terminal visible que se pueda fotografiar (los comandos se ejecutan vía ADB programático, sin un emulador de terminal gráfico capturable); generar una imagen simulando una terminal habría sido fabricar una captura que no ocurrió así. En su lugar, se reconfirmó en vivo que los datos siguen siendo exactamente los mismos que los ya documentados en `02_Explotaciones/Desafio_04/01_sqlite_query.txt` y `02_Explotaciones/Desafio_10/01_notas_filtradas.txt` (salida real de los comandos, sin cambios).

## Conclusión

Ambas correcciones (sección 4) neutralizan el comportamiento inseguro demostrado en los Desafíos 01 y 05 respectivamente, sin deshabilitar ni degradar la funcionalidad legítima de las pantallas afectadas, cumpliendo el criterio de la consigna ("No se considerará válida una corrección que se limite a deshabilitar la funcionalidad").
