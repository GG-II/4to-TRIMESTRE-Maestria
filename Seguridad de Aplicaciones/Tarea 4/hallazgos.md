# Hallazgos — Laboratorio bWAPP (bee-box v1.6)

Tarea 4 — Seguridad en Aplicaciones, Maestría en Seguridad Informática (UMG).
Entorno: VM bee-box (Ubuntu + bWAPP) en VirtualBox, red NAT aislada (127.0.0.1:8081 → VM:80), sin exposición externa.

---

## 1. SQL Injection (GET/Search)

- **Amenaza OWASP:** A1 - Injection (SQL Injection)
- **Descripción de la amenaza:** El parámetro `title` del buscador de películas se concatena directamente dentro de una consulta SQL sin validar ni parametrizar. Al enviar `' OR '1'='1` la condición WHERE se vuelve siempre verdadera, devolviendo toda la tabla en vez de respetar el filtro de búsqueda. Ocurre porque la aplicación arma el SQL con concatenación de strings en lugar de usar prepared statements.
- **Nivel de riesgo:** Alto
- **Mitigación:** Usar consultas parametrizadas / prepared statements (PDO o mysqli con bind_param), nunca concatenar entrada de usuario en el SQL. Aplicar validación de tipo/longitud en el parámetro y el principio de mínimo privilegio en el usuario de base de datos (sin permisos de lectura sobre tablas que no necesita).
- **Evidencia:** [capturas/01-sql-injection.png](capturas/01-sql-injection.png)
  - Payload: `http://localhost:8081/bWAPP/sqli_1.php?title=' OR '1'='1&action=search`
  - Resultado: la tabla completa de películas se vuelca sin que exista ninguna coincidencia real con el texto buscado.

---

## 2. HTML Injection - Reflected (GET)

- **Amenaza OWASP:** A1 - Injection (HTML Injection)
- **Descripción de la amenaza:** El formulario de nombre/apellido refleja el parámetro `firstname` directamente en el HTML de respuesta sin escapar caracteres especiales (`<`, `>`). Esto permite inyectar etiquetas arbitrarias que el navegador interpreta como marcado real, alterando la página o preparando el terreno para ataques más serios (phishing, defacement, XSS).
- **Nivel de riesgo:** Medio
- **Mitigación:** Escapar toda salida que incluya datos de usuario con `htmlspecialchars()` (PHP) antes de imprimirla en el HTML, o usar un motor de templates que escape por defecto (autoescaping). Aplicar Content-Security-Policy como defensa en profundidad.
- **Evidencia:** [capturas/02-html-injection.png](capturas/02-html-injection.png)
  - Payload: `http://localhost:8081/bWAPP/htmli_get.php?firstname=<h1>HTML Injection PoC</h1>&lastname=Bee&form=submit`
  - Resultado: el navegador renderiza un encabezado `<h1>` real insertado en la página, en vez de mostrar el texto plano.

---

## 3. Cross-Site Scripting - Reflected (GET)

- **Amenaza OWASP:** A3 - Cross-Site Scripting (XSS)
- **Descripción de la amenaza:** El mismo parámetro `firstname` no filtra etiquetas `<script>`, permitiendo ejecutar JavaScript arbitrario en el contexto del navegador de la víctima. Un atacante puede robar cookies de sesión, redirigir al usuario o realizar acciones en su nombre. Ocurre porque la aplicación confía en la entrada del usuario y la refleja sin sanitizar ni escapar.
- **Nivel de riesgo:** Alto
- **Mitigación:** Escapar/codificar toda entrada reflejada en el HTML (`htmlspecialchars`), usar cabecera `Content-Security-Policy` restrictiva, marcar las cookies de sesión como `HttpOnly` y `Secure` para que no sean accesibles desde JavaScript.
- **Evidencia:** [capturas/03-xss-reflected.png](capturas/03-xss-reflected.png)
  - Payload: `http://localhost:8081/bWAPP/xss_get.php?firstname=<script>alert(document.cookie)</script>&lastname=Bee&form=submit`
  - Resultado: el script se ejecuta en el navegador y expone la cookie de sesión activa (`PHPSESSID=...`), visible en la consola del navegador.

---

## 4. OS Command Injection

- **Amenaza OWASP:** A1 - Injection (Command Injection)
- **Descripción de la amenaza:** El módulo de "DNS lookup" pasa el valor del campo `target` directamente a una llamada de sistema (tipo `nslookup`/`dig`) sin sanitizar separadores de comandos (`&&`, `;`, `|`). Esto permite encadenar comandos arbitrarios del sistema operativo con los mismos privilegios del servidor web.
- **Nivel de riesgo:** Alto
- **Mitigación:** Nunca pasar entrada de usuario a funciones que invocan el shell (`exec`, `system`, `shell_exec`). Si es inevitable, usar listas blancas estrictas de caracteres permitidos y funciones que invoquen el binario directamente con argumentos separados (sin shell), p. ej. `escapeshellarg()` combinado con validación de formato (solo IP/dominio válido).
- **Evidencia:** [capturas/04-os-command-injection.png](capturas/04-os-command-injection.png)
  - Payload: `target = 127.0.0.1 && whoami && id`
  - Resultado: la respuesta del servidor incluye la salida real de `whoami`/`id`: `www-data uid=33(www-data) gid=33(www-data) groups=33(www-data)`, confirmando ejecución de comandos del sistema operativo.

---

## 5. Broken Authentication - Insecure Login Forms

- **Amenaza OWASP:** A2 - Broken Authentication and Session Management
- **Descripción de la amenaza:** El formulario de login se sirve por HTTP plano (sin TLS) y además expone en el propio formulario (como placeholder/hint) las credenciales válidas de ejemplo (`tonystark` / `I am Iron Man`). Al no cifrarse el transporte, cualquiera en la misma red puede capturar usuario y contraseña con un sniffer; además revelar credenciales de ejemplo en el front-end facilita el reconocimiento para un atacante.
- **Nivel de riesgo:** Alto
- **Mitigación:** Forzar HTTPS en todo el flujo de autenticación (HSTS incluido), nunca mostrar credenciales reales ni de ejemplo en el HTML/placeholder de producción, aplicar límite de intentos (rate limiting / bloqueo temporal) y registrar intentos fallidos.
- **Evidencia:** [capturas/05-insecure-login.png](capturas/05-insecure-login.png)
  - Observación: formulario accesible en `http://localhost:8081/...` (sin cifrar) con credenciales de ejemplo visibles directamente en los campos del formulario.

---

## 6. Insecure Direct Object Reference - IDOR (Change Secret)

- **Amenaza OWASP:** A4 - Insecure Direct Object References
- **Descripción de la amenaza:** El formulario para cambiar el "secreto" de la cuenta envía un campo oculto `login` con el nombre de usuario propietario del registro (`bee` por defecto). El servidor no valida que el usuario autenticado en sesión coincida con el valor de ese campo oculto antes de aplicar el cambio, por lo que modificando `login` en el HTML/petición se puede sobrescribir el secreto de **cualquier otra cuenta** sin autorización.
- **Nivel de riesgo:** Alto
- **Mitigación:** Nunca confiar en identificadores de objeto enviados por el cliente (campos ocultos, parámetros GET/POST) para decisiones de autorización. El servidor debe derivar el usuario objetivo únicamente de la sesión autenticada (server-side), y validar explícitamente que el recurso solicitado pertenece al usuario actual antes de modificarlo.
- **Evidencia:** [capturas/06-idor-change-secret.png](capturas/06-idor-change-secret.png)
  - Payload: formulario con `secret=HACKED_BY_IDOR` + campo oculto `login` modificado de `bee` a otro usuario antes de enviar el POST.
  - Resultado: el servidor acepta la petición y aplicaría el cambio sobre la cuenta indicada en `login`, no sobre el usuario realmente autenticado.

---

## 7. XML External Entity Attacks (XXE)

- **Amenaza OWASP:** A7 - Missing Function Level Access Control / XXE (clasificado también como A4 en OWASP Top 10 2017/2021)
- **Descripción de la amenaza:** El endpoint que resetea el "secreto" del usuario acepta un cuerpo XML y lo procesa con un parser que no deshabilita la resolución de entidades externas (`DOCTYPE`/`ENTITY`). Esto permite declarar una entidad `SYSTEM` apuntando a un archivo local (`file:///etc/passwd`) y hacer que el parser la resuelva, abriendo la puerta a lectura de archivos arbitrarios del servidor, SSRF interno o denegación de servicio (billion laughs).
- **Nivel de riesgo:** Alto
- **Mitigación:** Deshabilitar la carga de entidades externas y DTDs en el parser XML (`libxml_disable_entity_loader(true)` en PHP antiguo, o configurar el parser para no resolver `SYSTEM`/`PUBLIC`), validar el XML contra un esquema estricto, y usar formatos menos permisivos (JSON) cuando no se necesite XML.
- **Evidencia:** [capturas/07-xxe.png](capturas/07-xxe.png)
  - Payload: `<!DOCTYPE reset [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><reset><login>bee</login><secret>&xxe;</secret></reset>` enviado por POST a `xxe-2.php`.
  - Resultado: el servidor procesa la entidad externa y confirma el reseteo del secreto usando el contenido resuelto, evidenciando que el parser XML resuelve referencias a archivos del sistema.

---

## 8. Unrestricted File Upload

- **Amenaza OWASP:** A5 - Security Misconfiguration (clasificado también en OWASP Top 10 bajo validación de archivos)
- **Descripción de la amenaza:** El formulario de subida de "imágenes" no valida el tipo real del archivo (ni extensión, ni content-type, ni contenido) antes de guardarlo en una carpeta servida públicamente (`/images/`). Esto permite subir un archivo `.php` con código ejecutable y luego invocarlo directamente desde el navegador, logrando ejecución remota de código (RCE) en el servidor.
- **Nivel de riesgo:** Alto
- **Mitigación:** Validar extensión y contenido real del archivo (magic bytes), renombrar archivos subidos con nombres aleatorios sin extensión ejecutable, almacenar los archivos fuera del document root o en un storage que no ejecute scripts, y servirlos siempre con `Content-Disposition: attachment` cuando aplique.
- **Evidencia:** [capturas/08-unrestricted-upload.png](capturas/08-unrestricted-upload.png)
  - Payload: archivo `poc.php` con `<?php echo 'UPLOAD_RCE_POC_OK - '.php_uname().' - whoami:'.shell_exec('whoami'); ?>` subido vía `multipart/form-data`.
  - Resultado: al visitar `images/poc.php` el servidor ejecuta el código PHP subido, devolviendo el kernel del sistema y el usuario del proceso (`www-data`), confirmando ejecución remota de código.

---

## 9. Cross-Site Request Forgery (CSRF) - Change Password

- **Amenaza OWASP:** A8 - Cross-Site Request Forgery (CSRF)
- **Descripción de la amenaza:** El endpoint que cambia la contraseña (`csrf_1.php`) acepta la petición por **GET**, sin ningún token anti-CSRF ni verificación de origen, y sin pedir la contraseña actual. Como el navegador de la víctima adjunta automáticamente la cookie de sesión en cualquier petición al dominio, basta con que la víctima autenticada visite una página externa controlada por el atacante (o cargue una imagen/iframe con esa URL) para que su contraseña se cambie sin su conocimiento ni consentimiento.
- **Nivel de riesgo:** Alto
- **Mitigación:** Usar tokens anti-CSRF únicos por sesión/petición y validarlos server-side, exigir que operaciones sensibles (cambio de contraseña) se hagan por POST (nunca GET) y requieran la contraseña actual, y configurar cookies de sesión con `SameSite=Strict` o `Lax`.
- **Evidencia:** [capturas/09-csrf-change-password.png](capturas/09-csrf-change-password.png)
  - Payload: página externa (`http://127.0.0.1:9191/csrf-poc.html`, origen distinto a bWAPP) con un formulario auto-enviado por JavaScript hacia `http://localhost:8081/bWAPP/csrf_1.php?password_new=hacked123&password_conf=hacked123&action=change`.
  - Resultado: la página responde "The password has been changed!" y se verifica el login exitoso con `bee/hacked123`, confirmando que la contraseña cambió sin interacción directa del usuario con el sitio legítimo. (Contraseña revertida a `bug` tras la prueba.)

---

## 10. Server Side Request Forgery (SSRF)

- **Amenaza OWASP:** A10 - Server-Side Request Forgery (SSRF)
- **Descripción de la amenaza:** El módulo de selección de idioma (`rlfi.php?language=...`) incluye como código PHP el archivo indicado en el parámetro sin validar que pertenezca a una lista blanca cerrada (vulnerabilidad de Local/Remote File Inclusion). Combinando esto con un script auxiliar que abre conexiones TCP (`fsockopen`) hacia una IP/puerto arbitrarios, un atacante externo puede forzar al **servidor** a realizar conexiones salientes hacia su propia red interna (`127.0.0.1` y otros hosts no expuestos), revelando qué puertos/servicios internos están disponibles (FTP, SSH, SMTP, MySQL, etc.) sin tener acceso directo a esa red.
- **Nivel de riesgo:** Alto
- **Mitigación:** Nunca incluir/ejecutar archivos a partir de un parámetro controlado por el usuario (usar una lista blanca estricta de nombres de archivo permitidos, sin rutas). Para cualquier funcionalidad que haga peticiones salientes a una URL/host suministrado por el usuario, validar y restringir destinos permitidos (lista blanca de hosts/IPs, bloquear rangos privados/loopback), y ejecutar esas peticiones desde una red segmentada sin acceso a servicios internos sensibles.
- **Evidencia:** [capturas/10-ssrf.png](capturas/10-ssrf.png)
  - Payload: `http://localhost:8081/bWAPP/rlfi.php?language=../evil/ssrf-1.txt&ip=127.0.0.1&action=go`
  - Resultado: el servidor ejecuta el script incluido y devuelve el estado de 9 puertos internos escaneados contra `127.0.0.1`, mostrando FTP(21), SSH(22), SMTP(25), HTTP(80) y MySQL(3306) como accesibles — el servidor actuó como proxy/escáner hacia su propia red interna a pedido del atacante.

---

### Resumen

| # | Vulnerabilidad | Categoría OWASP | Riesgo |
|---|---|---|---|
| 1 | SQL Injection (GET/Search) | A1 - Injection | Alto |
| 2 | HTML Injection - Reflected (GET) | A1 - Injection | Medio |
| 3 | XSS - Reflected (GET) | A3 - XSS | Alto |
| 4 | OS Command Injection | A1 - Injection | Alto |
| 5 | Broken Auth. - Insecure Login Forms | A2 - Broken Authentication | Alto |
| 6 | Insecure DOR (Change Secret) | A4 - IDOR | Alto |
| 7 | XML External Entity Attacks (XXE) | A7 / XXE | Alto |
| 8 | Unrestricted File Upload | A5 - Security Misconfiguration | Alto |
| 9 | CSRF (Change Password) | A8 - CSRF | Alto |
| 10 | Server Side Request Forgery (SSRF) | A10 - SSRF | Alto |

**Entorno de pruebas:** VM bee-box v1.6 (Ubuntu + bWAPP v2.2) en VirtualBox 7.2.20, red NAT aislada, sin exposición a Internet ni a la LAN del host. Todas las pruebas se realizaron contra `localhost:8081` (port-forward NAT hacia la VM), con la autorización del curso Seguridad en Aplicaciones (Maestría en Seguridad Informática, UMG) para fines estrictamente educativos.
