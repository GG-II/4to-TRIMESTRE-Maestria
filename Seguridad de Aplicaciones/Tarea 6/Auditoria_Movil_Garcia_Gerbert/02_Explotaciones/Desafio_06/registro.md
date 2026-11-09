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

**Resultado observado:** `fileuser:FilePass789` en texto plano dentro de un archivo temporal en el directorio de datos de la app. El listado muestra permisos `-rw-------` (solo el propietario): `File.setReadable(true)` sin segundo argumento habilita lectura únicamente para el propietario, por lo que el archivo **no** es legible por otras apps en este entorno. La debilidad demostrada es el almacenamiento de credenciales en texto plano en un archivo temporal que persiste, legible con `run-as`/root y recogido por copias de seguridad (`allowBackup="true"`).

**Código vulnerable:** `java/jakhar/aseem/diva/InsecureDataStorage3Activity.java`:
```java
File uinfo = File.createTempFile("uinfo", "tmp", ddir);
uinfo.setReadable(true);
uinfo.setWritable(true);
FileWriter fw = new FileWriter(uinfo);
fw.write(usr.getText().toString() + ":" + pwd.getText().toString() + "\n");
```

**Evidencia:** `01_listado_dir.txt`, `02_archivo_temp.txt`.

**Impacto:** Credenciales en texto plano en un archivo temporal que nunca se elimina; quien tenga acceso al almacenamiento de la app (root, `run-as` en builds depurables, copias de seguridad, dispositivo comprometido) las obtiene sin esfuerzo.

**Corrección propuesta:** No guardar credenciales en archivos temporales; si se requiere persistencia en archivo, cifrar el contenido (p. ej. `EncryptedFile` de Jetpack Security), mantener permisos por defecto (`MODE_PRIVATE`) y eliminar el archivo al terminar.
