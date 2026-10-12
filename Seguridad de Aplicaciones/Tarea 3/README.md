# Tarea 3 · Rediseño seguro de FinanciaWeb con OWASP ASVS

Curso: Seguridad en Aplicaciones. Individual. Documento único de 6 a 10 páginas (sin portada ni referencias), en PDF o Word según indique el docente.

| Archivo | Qué es |
|---|---|
| `Informe-Tarea3-FinanciaWeb.docx` | Informe sobre la plantilla DOC BASE: portada, perfil de seguridad, 10 problemas de diseño, diagramas actual y propuesto, 7 principios, matriz de 12 requisitos ASVS de 8 capítulos, 5 decisiones críticas, escenario antes y después, respuesta ejecutiva y referencias |
| `Figuras/` | Los dos diagramas de arquitectura en PNG |
| `Tarea Semana 3 -- Diseño Seguro.pdf` | Enunciado |

## Antes de entregar
1. **Reescribir con tus palabras** los textos en el artifact de reescritura y pasarme el resultado para aplicarlo al Word.
2. **Confirmar con el docente la versión de ASVS.** El informe usa la 5.0.0 (IDs como `V8.2.2`). Si pide la 4.0.3, los IDs cambian.
3. **Actualizar el índice en Word** (clic derecho sobre la tabla de contenido, *Actualizar campo*) y comprobar que el cuerpo queda entre 6 y 10 páginas. No se pudo contar páginas desde el entorno de trabajo.
4. **Revisar los diagramas** en el documento y exportar a PDF si el docente lo pide.

## Decisiones de diseño
- La arquitectura actual se dibujó según el texto de la sección 3.3 del caso (API expuesta, un solo servidor, una cuenta de base de datos). El diagrama del enunciado muestra WAF y gateway, que contradicen ese texto; el informe lo aclara.
- Nivel ASVS propuesto: 2, con V8.4.2 (Nivel 3) solo para la administración.
