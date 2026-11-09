# Desafío 02 — Hardcoding Issues (Part 1)

**Procedimiento:**
1. Decompilación con JADX / revisión de `HardcodeActivity.java` en el código fuente.
2. Localización de la cadena hardcodeada `vendorsecretkey` en la comparación `equals()`.
3. Navegación por UI: `MainActivity` → "2. Hardcoding Issues - Part 1".
4. Ingreso de `vendorsecretkey` en el campo y toque en "ACCESS".

**Resultado observado:** Toast `Access granted! See you on the other side :)` — confirma que el valor hardcodeado concede acceso.

**Código vulnerable:** `java/jakhar/aseem/diva/HardcodeActivity.java`
```java
if (hckey.getText().toString().equals("vendorsecretkey")) {
    Toast.makeText(this, "Access granted! See you on the other side :)", Toast.LENGTH_SHORT).show();
}
```

**Evidencia:** `04_toast_granted.png` (t2.png).

**Impacto:** Cualquier atacante con acceso al APK (decompilación trivial) obtiene el secreto sin esfuerzo; si esa clave protege funcionalidad real (API, licencia, feature flag) queda completamente comprometida.

**Corrección propuesta:** No embeber secretos en el cliente; validar contra un servidor backend con el secreto fuera del APK, o usar un mecanismo de atestación/token de corta vida.
