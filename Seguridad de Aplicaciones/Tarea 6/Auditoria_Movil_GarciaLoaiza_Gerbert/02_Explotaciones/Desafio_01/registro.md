# Desafío 01 — Insecure Logging

**Procedimiento:**
1. Navegación por la UI de DIVA (no exportada): `MainActivity` → "1. Insecure Logging".
2. Ingreso de dato ficticio en el campo "Enter your credit card number": `4111111111111111` (número de prueba estándar, no real).
3. Toque en "CHECK OUT", lo que dispara una excepción controlada y el log correspondiente.
4. Captura con `adb logcat`.

**Resultado observado:**
```
11-09 12:15:03.133  2381  2381 E diva-log: Error while processing transaction with credit card: 4111111111111111
```
El dato ficticio de tarjeta queda expuesto en texto plano en el log del sistema, accesible a cualquier app con permiso de lectura de logs o vía `adb logcat` sin privilegios especiales.

**Código vulnerable:** `java/jakhar/aseem/diva/LogActivity.java`
```java
Log.e("diva-log", "Error while processing transaction with credit card: " + cctxt.getText().toString());
```

**Evidencia:** `03_dato_ingresado.png`, `04_logcat_completo.txt`.

**Impacto:** Cualquier app con `READ_LOGS` (o acceso físico/ADB) puede recuperar datos financieros sensibles del usuario sin interacción adicional.

**Corrección propuesta:** Eliminar el log de datos sensibles o reemplazarlo por un identificador no reversible (p. ej. hash truncado o id de transacción), nunca el valor ingresado por el usuario.
