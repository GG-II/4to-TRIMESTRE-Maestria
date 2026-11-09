# Desafío 09 — Access Control Issues (Part 2)

**Procedimiento:**
1. Análisis de `AccessControl2Activity.java`: envía un intent con acción `VIEW_CREDS2` y un extra booleano `check_pin` que indica si el usuario ya está "registrado" con PIN.
2. Análisis de `APICreds2Activity.java`: si `check_pin == false`, muestra directamente las credenciales de la API sin pedir PIN; solo si es `true` (o si el extra no viene) pide registro/PIN. **No hay verificación real del lado del servidor ni del PIN** — la decisión de mostrar los datos depende enteramente de un extra del intent controlable por el llamador.
3. Explotación: invocar el componente exportado directamente, forzando `check_pin=false`, sin pasar por `AccessControl2Activity` ni ingresar PIN alguno:
```
adb shell am start -a jakhar.aseem.diva.action.VIEW_CREDS2 --ez check_pin false
```

**Resultado observado:**
```
TVEETER API Key: secrettveeterapikey
API User name: diva2
API Password: p@ssword2
```
Acceso directo a credenciales protegidas "por PIN", sin conocer ni ingresar ningún PIN.

**Código vulnerable:** `java/jakhar/aseem/diva/APICreds2Activity.java`:
```java
boolean bcheck = i.getBooleanExtra(getString(R.string.chk_pin), true);
if (bcheck == false) {
    // muestra credenciales directamente
}
```
El control de acceso se basa en un dato enviado por el propio cliente/llamador (el intent extra), nunca verificado contra el servidor.

**Evidencia:** `01_bypass_pin.png`.

**Impacto:** Control de acceso completamente del lado del cliente y manipulable; cualquier app puede obtener las credenciales sin autenticarse.

**Corrección propuesta:** Eliminar la lógica de decisión basada en extras del intent; la verificación del PIN y la entrega de credenciales debe ocurrir en el backend (servidor), devolviendo las credenciales solo tras una validación de PIN exitosa del lado servidor, nunca confiando en un flag enviado por el cliente.
