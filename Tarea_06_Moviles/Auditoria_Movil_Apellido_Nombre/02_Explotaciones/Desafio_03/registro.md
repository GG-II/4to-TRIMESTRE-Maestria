# Desafío 03 — Insecure Data Storage (Part 1)

**Procedimiento:**
1. Navegación UI: `MainActivity` → "3. Insecure Data Storage - Part 1".
2. Ingreso de usuario `testuser` / contraseña `SuperSecret123` (ficticios) y toque en "SAVE".
3. Lectura directa del almacenamiento de la app con root (`run-as`):
```
adb shell run-as jakhar.aseem.diva cat shared_prefs/jakhar.aseem.diva_preferences.xml
```

**Resultado observado:**
```xml
<map>
    <string name="password">SuperSecret123</string>
    <string name="user">testuser</string>
</map>
```
Credenciales en texto plano, sin cifrar, en `SharedPreferences` default.

**Código vulnerable:** [`InsecureDataStorage1Activity.java`](../../../../diva-source/app/src/main/java/jakhar/aseem/diva/InsecureDataStorage1Activity.java) — `saveCredentials()`, usa `PreferenceManager.getDefaultSharedPreferences` con `putString` directo sin cifrado.

**Evidencia:** `02_verificacion.png`, `03_shared_prefs.xml`.

**Impacto:** Cualquier proceso con acceso al almacenamiento de la app (root, backup, malware con mismo UID o dispositivo comprometido) obtiene las credenciales en claro.

**Corrección propuesta:** Usar `EncryptedSharedPreferences` (Jetpack Security) con `MasterKey` del Android Keystore, nunca `SharedPreferences` planas para datos sensibles.
