# Desafío 05 — Input Validation Issues (Part 1) — SQL Injection

**Procedimiento:**
1. Navegación UI: `MainActivity` → "7. Input Validation Issues - Part 1" (SQLInjectionActivity).
2. Baseline con usuario inexistente `noexiste` → sin resultados (`02_exploit_toast.png` muestra el payload ya escrito, antes de pulsar SEARCH).
3. Payload de inyección en el campo de búsqueda: `x' OR '1'='1` (introducido carácter por carácter vía `adb shell input text` + `KEYCODE_APOSTROPHE` para las comillas simples).
4. Toque en "SEARCH". El logcat confirma ejecución sin excepción (`Diva-sqli` no reporta error con el payload correcto, a diferencia de un intento previo con comilla sobrante que sí falló con `SQLITE_ERROR`).
5. Verificación del resultado exacto reproduciendo la consulta inyectada contra la base de datos `sqli` de la app vía `run-as`.

**Resultado observado:**
```
admin|passwd123|1234567812345678
diva|p@ssword|1111222233334444
john|password123|5555666677778888
```
Los tres usuarios y sus tarjetas de crédito (ficticias) quedan expuestos con una única búsqueda maliciosa, cumpliendo el objetivo planteado por el propio desafío ("output data of all the three users with a single malicious search").

**Código vulnerable:** `java/jakhar/aseem/diva/SQLInjectionActivity.java` — método `search()`:
```java
cr = mDB.rawQuery("SELECT * FROM sqliuser WHERE user = '" + srchtxt.getText().toString() + "'", null);
```
Concatenación directa del input del usuario en la consulta SQL.

**Nota sobre la evidencia:** DIVA muestra el resultado de la búsqueda en un `Toast` efímero, que no quedó capturado en las imágenes. Las capturas documentan el payload ingresado y la acción; el resultado exacto de la consulta inyectada se comprobó ejecutándola contra la base de datos `sqli` de la app (`04_resultado_3_usuarios.txt`).

**Evidencia:** `01_baseline_sin_resultado.png`, `02_exploit_toast.png` y `d5_payload2.png` (payload en el campo), `04_resultado_3_usuarios.txt`.

**Impacto:** Bypass total de filtro de búsqueda; exposición de credenciales y datos financieros de todos los usuarios de la tabla sin autenticación.

**Corrección propuesta:** Reemplazar `rawQuery` con concatenación por `rawQuery` parametrizado (`?` + `selectionArgs`) o `SQLiteQueryBuilder`, nunca interpolar el input del usuario directamente en el SQL.
