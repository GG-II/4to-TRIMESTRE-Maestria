# Tarea 4 · Práctica en aplicaciones web con OWASP (bWAPP)

Curso: Seguridad en Aplicaciones. Individual. Documento con carátula, subido a Canvas.

| Archivo | Qué es |
|---|---|
| `Tarea4-Practica-bWAPP-OWASP.docx` | Informe con las 10 vulnerabilidades explotadas en bWAPP (bee-box v1.6): amenaza OWASP, descripción, nivel de riesgo, mitigación y captura de cada prueba |
| `hallazgos.md` | Notas originales de la sesión que ejecutó las pruebas en la máquina virtual |
| `capturas/` | Las 10 capturas de pantalla de cada prueba |

## Qué se hizo
Laboratorio bWAPP v2.2 (bee-box v1.6, Ubuntu) en VirtualBox, en red NAT aislada sin salida a Internet ni a la LAN del equipo, accedido solo por el puerto reenviado 127.0.0.1:8081. Se explotaron 10 vulnerabilidades de distintas categorías OWASP: SQL Injection, HTML Injection, XSS reflejado, inyección de comandos del sistema, autenticación insegura, IDOR, XXE, carga de archivos sin restricción, CSRF y SSRF.

## Antes de entregar
1. Revisar el documento en Word: que las 10 capturas se vean completas y el índice esté actualizado (clic derecho, *Actualizar campo*).
2. Confirmar que el documento cumple el formato de carátula que pide el enunciado.
3. Subir el PDF o el Word a Canvas, según lo que indique el docente.
