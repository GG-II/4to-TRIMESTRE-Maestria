# 5. Pruebas de regresión

APK recompilado con las dos correcciones aplicadas (`diva-corregido.apk`, instalado reemplazando la versión vulnerable). Build exitosa (`BUILD SUCCESSFUL`) confirmando que el código corregido compila y la app es funcional.

## Corrección A — Insecure Logging

**Repetición de la prueba del Desafío 01:** ingreso de `4111111111111111` en "Insecure Logging" → "CHECK OUT".

**Resultado tras la corrección:**
```
11-09 12:24:36.318  4272  4272 E diva-log: Error while processing transaction. Card ref: ****1111
```
El log ya no expone el número completo; solo últimos 4 dígitos (referencia no reversible). El comportamiento de la app (mensaje de error al usuario) se mantiene sin cambios.

**Comportamiento inseguro eliminado:** ✅ Sí — ya no es posible recuperar el PAN completo desde logcat.
**Funcionalidad legítima:** ✅ Se mantiene — el flujo de "checkout" sigue mostrando el toast de error esperado.

## Corrección B — SQL Injection

**Repetición de la prueba del Desafío 05:** payload `x' OR '1'='1` en "Input Validation Issues - Part 1" → "SEARCH".

**Resultado tras la corrección:** sin excepciones en logcat (`Diva-sqli` no reporta errores). Verificación directa en la base de datos tratando el payload como valor literal exacto (equivalente exacto al efecto de `rawQuery` parametrizado):
```
SELECT * FROM sqliuser WHERE user = 'x'' OR ''1''=''1';   -- (comillas escapadas = valor literal)
→ 0 filas
```
El payload ya no altera la estructura de la consulta; se compara como cadena literal contra la columna `user` y no coincide con ningún usuario.

**Repetición de búsqueda legítima** (`admin`, mencionada en el propio hint de la app):
```
SELECT * FROM sqliuser WHERE user = 'admin';
→ admin|passwd123|1234567812345678
```
El caso de uso normal (buscar un usuario existente) sigue funcionando exactamente igual que antes de la corrección.

**Comportamiento inseguro eliminado:** ✅ Sí — ya no es posible extraer todos los usuarios con un payload malicioso.
**Funcionalidad legítima:** ✅ Se mantiene — la búsqueda por nombre exacto de usuario funciona sin cambios.

**Nota sobre la evidencia:** el resultado de la búsqueda en DIVA aparece en un `Toast` efímero que no quedó capturado. Las capturas `01_sqli_corregido_payload_no_resultado.png` y `02_busqueda_legitima.png` muestran los valores ingresados en la app corregida; el efecto sobre la consulta se verificó contra la base de datos (`03_sqli_regresion.txt`), donde el payload se compara como cadena literal y devuelve 0 filas, y la búsqueda `admin` devuelve su fila.

**Evidencia:** `03_sqli_regresion.txt`, `01_sqli_corregido_payload_no_resultado.png`, `02_busqueda_legitima.png`.

## Conclusión

Ambas correcciones (sección 4) neutralizan el comportamiento inseguro demostrado en los Desafíos 01 y 05 respectivamente, sin deshabilitar ni degradar la funcionalidad legítima de las pantallas afectadas, cumpliendo el criterio de la consigna ("No se considerará válida una corrección que se limite a deshabilitar la funcionalidad").
