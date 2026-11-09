# Desafío 06 — Insecure Data Storage (Part 3)

**Procedimiento:**
1. Navegación UI: `MainActivity` → "5. Insecure Data Storage - Part 3" (`InsecureDataStorage3Activity`).
2. Ingreso de usuario `fileuser` / contraseña `FilePass789` (ficticios), toque en "SAVE".
3. Listado del directorio de datos de la app:
```
adb shell run-as jakhar.aseem.diva ls -la
```
4. Lectura del archivo temporal creado (`uinfo*tmp`):
```
adb shell run-as jakhar.aseem.diva cat uinfo2424796984966974941tmp
```

**Resultado observado:** `fileuser:FilePass789` en texto plano dentro de un archivo temporal creado con `setReadable(true)`/`setWritable(true)` (permisos world-readable) en el `dataDir` de la app.

**Código vulnerable:** [`InsecureDataStorage3Activity.java`](../../../../diva-source/app/src/main/java/jakhar/aseem/diva/InsecureDataStorage3Activity.java):
```java
File uinfo = File.createTempFile("uinfo", "tmp", ddir);
uinfo.setReadable(true);
uinfo.setWritable(true);
FileWriter fw = new FileWriter(uinfo);
fw.write(usr.getText().toString() + ":" + pwd.getText().toString() + "\n");
```

**Evidencia:** `01_listado_dir.txt`, `02_archivo_temp.txt`.

**Impacto:** Credenciales en texto plano en un archivo con permisos deliberadamente abiertos; en un dispositivo con otra app maliciosa de igual UID/root, o vía backup, el archivo es trivialmente legible.

**Corrección propuesta:** No usar archivos temporales world-readable para datos sensibles; si se requiere persistencia en archivo, cifrar el contenido (p. ej. `EncryptedFile` de Jetpack Security) y mantener permisos por defecto (`MODE_PRIVATE`), nunca `setReadable(true)`/`setWritable(true)`.
