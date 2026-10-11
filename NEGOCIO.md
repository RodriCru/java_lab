# NEGOCIO.md — Java Practice Lab

> Contexto de negocio: qué es, para quién y por qué. La memoria técnica está en [CLAUDE.md](./CLAUDE.md).

---

## Qué es

Una plataforma web de **práctica de Java para entrevistas técnicas**. El alumno resuelve ejercicios
de lógica directamente en el navegador y recibe la validación automática de sus tests, sin instalar
ni configurar nada.

---

## El problema que resuelve

Practicar para una entrevista técnica de Java normalmente obliga a montar un proyecto, crear clases,
escribir tus propios tests y ejecutarlos a mano. Eso añade fricción justo en el momento en que
quieres concentrarte en **resolver problemas**. Además, corregirse uno mismo es difícil: nadie te
dice si tu solución cubre los casos borde.

Este laboratorio elimina la fricción: escribes el método, pulsas enviar, y en segundos ves qué
tests pasaron y cuáles no, con el motivo exacto de cada fallo.

---

## Para quién

**Usuario principal:** el propio desarrollador, que se está preparando para entrevistas técnicas.
Es un proyecto personal de entrenamiento, no un producto comercial.

**Uso previsto:** sesiones cortas y repetidas (10–20 minutos), practicando por tema y volviendo
sobre lo que se falló.

---

## Propuesta de valor

1. **Cero configuración.** Se escribe en el navegador con resaltado de sintaxis; el backend compila
   y ejecuta los tests.
2. **Validación objetiva.** Los tests cubren casos normales, casos borde (vacíos, negativos, `null`)
   y casos límite, así que una solución "que funciona en el ejemplo" no pasa.
3. **Progreso que se conserva.** Cada cuenta guarda su historial, su código, su XP y su racha, y
   tiene una vista de repaso con lo que se resistió.
4. **Enfoque a entrevista.** El temario prioriza lo que de verdad se pregunta: lógica, cadenas,
   arreglos, estructuras de datos, dos punteros, recursión y complejidad — no teoría del lenguaje.

---

## Alcance del producto

**Niveles:** Básico (~50 ejercicios) e Intermedio (~50), hasta unos 100 en total.
**Fuera de alcance por ahora:** nivel avanzado, concurrencia y patrones de diseño. El foco es la
resolución de problemas; la teoría se limita a un repaso breve por tema, visible en cada ejercicio,
para quien no tiene Java fresco (no es un curso del lenguaje).

**Cómo se mide el avance del alumno:** ejercicios resueltos, XP (10 por ejercicio), racha de días
consecutivos y porcentaje por nivel.

---

## Principios de producto

- **El alumno nunca ve la solución.** Puede pedir pistas de una en una, y solo hasta el número de
  pistas que tenga el ejercicio.
- **El error debe enseñar.** Un test que falla muestra qué se esperaba y qué se obtuvo, sin stack
  traces internos.
- **Una solución que pasa se queda registrada**, aunque después se envíe algo peor: el esfuerzo no
  se pierde.
- **Seguro por defecto.** El código del alumno se ejecuta aislado; no puede tocar archivos, red ni
  el sistema. Y una cuenta está detrás de un login, así que el progreso es privado.
