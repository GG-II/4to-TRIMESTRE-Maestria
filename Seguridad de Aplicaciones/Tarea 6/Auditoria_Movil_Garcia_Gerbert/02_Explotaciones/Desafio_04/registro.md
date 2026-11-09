# Desafío 04 — Insecure Data Storage (Part 2)

**Procedimiento:**
1. Navegación UI: `MainActivity` → "4. Insecure Data Storage - Part 2".
2. Ingreso de usuario `dbuser` / contraseña `DbPass456` (ficticios), toque en "SAVE".
3. Consulta directa a la base SQLite local de la app:
```
adb shell "run-as jakhar.aseem.diva sqlite3 databases/ids2 'select * from myuser;'"
```

**Resultado observado:**
```
dbuser|DbPass456
```

**Código vulnerable:** `java/jakhar/aseem/diva/InsecureDataStorage2Activity.java` — tabla `myuser` creada en texto plano, inserción vía `execSQL` sin cifrado de columnas.

**Evidencia:** `01_sqlite_query.txt`.

**Impacto:** Igual que el desafío 03 — acceso con root/`run-as` revela credenciales en claro, esta vez desde una base de datos relacional.

**Corrección propuesta:** Cifrar la base con SQLCipher, o cifrar los valores sensibles a nivel de columna antes de insertarlos, con la clave resguardada en Android Keystore.
