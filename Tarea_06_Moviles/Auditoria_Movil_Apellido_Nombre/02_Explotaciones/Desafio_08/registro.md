# Desafío 08 — Access Control Issues (Part 1)

**Procedimiento:**
1. Revisión del AndroidManifest: `APICredsActivity` declarada con `android:exported="true"` e intent-filter `jakhar.aseem.diva.action.VIEW_CREDS` / categoría `DEFAULT`.
2. Revisión de `AccessControl1Activity.java`: el flujo "normal" de la app dispara un intent implícito con esa misma action desde un botón dentro de la app.
3. Se fuerza el cierre de la app (`am force-stop`) y se invoca el componente **directamente**, como lo haría cualquier app de terceros instalada en el dispositivo, sin abrir DIVA ni pasar por `AccessControl1Activity`:
```
adb shell am start -a jakhar.aseem.diva.action.VIEW_CREDS
```

**Resultado observado:** Se abre `APICredsActivity` mostrando directamente:
```
API Key: 123secretapikey123
API User name: diva
API Password: p@ssword
```
sin ninguna verificación de origen del llamador ni de autenticación previa.

**Código vulnerable:** [`AndroidManifest.xml`](../../../../diva-source/app/src/main/AndroidManifest.xml) — `APICredsActivity` exportada con intent-filter de acción personalizada; [`APICredsActivity.java`](../../../../diva-source/app/src/main/java/jakhar/aseem/diva/APICredsActivity.java) no implementa ningún control de acceso al mostrar los datos.

**Evidencia:** `01_creds_via_intent_externo.png`.

**Impacto:** Cualquier app maliciosa instalada en el mismo dispositivo (sin permisos especiales) puede extraer las credenciales de API del vendor simplemente enviando el intent implícito, sin interacción del usuario ni necesidad de abrir DIVA.

**Corrección propuesta:** Declarar el componente como `android:exported="false"` si solo se usa internamente, o protegerlo con un `permission` custom (`signature`-level) si debe ser accesible entre apps del mismo desarrollador. Nunca exponer actividades que muestren datos sensibles sin control de acceso.
