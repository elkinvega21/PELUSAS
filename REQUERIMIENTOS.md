# Especificación y Reglas del Proyecto: PELUSAS

Repositorio oficial: https://github.com/elkinvega21/PELUSAS.git

Este documento contiene la especificación oficial y requerimientos estrictos para el desarrollo del proyecto final "Pelusas". Debe ser consultado como fuente única de verdad para la arquitectura, lógica de negocio y pruebas.

---

## 1. Descripción General
Desarrollar una aplicación web funcional que permita jugar una versión digital del juego de mesa **Pelusas**.
El proyecto deberá implementar las reglas principales del juego, contar con:
- Un **backend** desarrollado en Java con Spring Boot.
- Una **base de datos** para persistencia e histórico de partidas.
- Una serie de **reglas de negocio** claramente implementadas en el backend.
- Una **API REST** bien estructurada.
- Un **frontend** web interactivo con estilo 2D.
- Demostración rigurosa de calidad de software mediante **pruebas automatizadas**: Unitarias (JUnit, Mockito), Integración (MockMvc, JPA/BD), Funcionales E2E (Playwright + JavaScript), y pruebas de mutación (Pitest >= 90%, cobertura 100%).

---

## 2. Organización y Responsabilidades
- Trabajo en equipo (1 a 3 estudiantes).
- Todos los integrantes deben dominar Backend, Frontend, Base de Datos, Reglas de Negocio, Endpoints REST, Pruebas y Arquitectura.

---

## 3. Requerimientos Tecnológicos

### 3.1 Backend
- **Lenguaje:** Java (Java 17 o superior recomendado).
- **Framework:** Spring Boot (Spring Web, Spring Data JPA, Lombok, Validation).
- **Base de Datos:** H2 (para desarrollo/pruebas rápidas) y configurable para PostgreSQL / MySQL.
- **Pruebas Unitarias:** JUnit 5 + Mockito.
- **Pruebas de Integración:** MockMvc y persistencia real sin mocks.
- **Pruebas de Mutación:** PITest con score objetivo >= 90% y 100% de cobertura.

### 3.2 Frontend
- **Tecnología:** Aplicación web moderna (React + Vite + TailwindCSS/CSS con estilo de juego 2D y animaciones).
- **Pruebas Funcionales:** Playwright con JavaScript / TypeScript.
- **Comunicación:** Consumo de servicios REST del backend.

### 3.3 Base de Datos
- Almacenamiento del estado y del **histórico de partidas terminadas**.
- Por cada partida terminada registrar:
  - Identificador de la partida.
  - Fecha / hora de la partida.
  - Jugadores participantes.
  - Resultado de la partida.
  - Por cada jugador: nombre, cartas acumuladas al finalizar, puntuación final, indicador si fue ganador.

---

## 4. Implementación de las Cartas y el Mazo

El mazo contiene **110 cartas** con distribución estricta:
- **Valores del 1 al 5:** 13 cartas de cada valor (13 × 5 = 65 cartas).
- **Valores del 6 al 10:** 9 cartas de cada valor (9 × 5 = 45 cartas).
- **Total:** 110 cartas.

> *Nota:* Está prohibido generar simplemente cantidades iguales para todos los valores.

---

## 5. Dinámica y Reglas del Juego

### 5.1 Creación y Configuración
- Crear partida con N jugadores (mínimo 2).
- Determinar quién inicia.
- Inicializar y barajar el mazo de 110 cartas.
- Estado inicial válido.

### 5.2 Estructura del Turno
Un turno consta de tres fases o pasos secuenciales:

#### Paso 1 — Guardar pelusas (Al inicio del turno)
- Si el jugador activo posee cartas boca arriba delante de él provenientes de su turno anterior (que sobrevivieron al turno de los otros jugadores), puede guardarlas en su **montón de puntos**.
- Una vez guardadas en el montón de puntos, estas cartas quedan seguras y dejan de estar en riesgo.

#### Paso 2 — Buscar pelusas (Robar del montón de polvo)
- El jugador saca una carta del montón de polvo (mazo).
- La carta queda visible delante del jugador.
- Las cartas del mismo valor se identifican y agrupan.
- El jugador puede decidir en cada carta sacada:
  - **Continuar jugando** (sacar otra carta).
  - **Finalizar voluntariamente su turno** (plantarse, dejando las cartas boca arriba delante de él hasta el inicio de su próximo turno).

#### Paso 3 — Robar cartas a los demás jugadores
- Cuando el jugador activo saca una carta de un determinado valor, si algún otro jugador tiene cartas boca arriba de **ese mismo valor** delante de él:
  - El jugador activo toma inmediatamente todas las cartas de ese valor de los demás jugadores.
  - Esas cartas pasan a estar delante del jugador actual.

### 5.3 Regla de Pérdida del Turno
Un jugador pierde su turno inmediatamente si se cumplen simultáneamente ambas condiciones:
1. Saca una carta cuyo valor ya tiene delante de él (carta repetida).
2. **Y tiene 3 o más cartas en total delante de él** (incluyendo la repetida que acaba de sacar).

**Consecuencias de la pérdida:**
- El jugador pierde el turno de inmediato.
- **Todas las cartas** que tenía delante son descartadas (no van al montón de puntos).
- El turno pasa al siguiente jugador.

*Casos límite:*
- Repetición teniendo menos de 3 cartas en total (ej. tiene 1 carta de valor 4 y saca otro 4, total 2 cartas) → **No pierde**.
- Repetición teniendo exactamente 3 cartas → **Pierde**.
- Repetición teniendo más de 3 cartas → **Pierde**.
- Carta nueva (no repetida) → **No pierde**.

### 5.4 Finalización de la Partida
- La partida finaliza en el momento exacto en que se agotan todas las cartas del montón de polvo (mazo vacío).
- Al finalizar:
  1. Todos los jugadores guardan automáticamente las cartas que todavía tengan delante en sus montones de puntos.
  2. Se suman los valores nominales de las cartas acumuladas en los montones de puntos.
  3. Se determina el ganador (mayor puntaje).
  4. En caso de empate, se aplican las reglas de desempate (ej. mayor cantidad de cartas o criterio oficial).
  5. Se registra la partida en la base de datos histórica.

---

## 6. Arquitectura y Reglas de Negocio en el Backend
- **El backend es la única fuente de verdad:** El frontend solicita acciones; el backend valida y ejecuta las reglas.
- Validaciones en backend:
  - Validar si es el turno del jugador que envía la petición.
  - Validar si puede sacar carta o si el mazo está vacío.
  - Detección automática y obligatoria de robo de cartas a oponentes.
  - Detección de pérdida de turno por repetición con >= 3 cartas.
  - Validación de guardar cartas al inicio del turno.
  - Cálculo de fin de partida y puntuaciones.

---

## 7. API REST Requerida
Endpoints mínimos:
- `POST /api/partidas` — Crear nueva partida (jugadores, configuración).
- `GET /api/partidas/{id}` — Consultar estado completo de la partida.
- `POST /api/partidas/{id}/guardar` — Guardar cartas del turno anterior en el montón de puntos.
- `POST /api/partidas/{id}/sacar-carta` — Sacar carta del mazo (aplica robo o pérdida según reglas).
- `POST /api/partidas/{id}/plantarse` — Finalizar voluntariamente el turno.
- `GET /api/partidas/{id}/resultado` — Consultar resultado si ha finalizado.
- `GET /api/historico` — Consultar historial de partidas terminadas.

---

## 8. Estrategia de Pruebas Automatizadas

1. **Pruebas Unitarias (JUnit 5 + Mockito):**
   - Mazo: 110 cartas, conteo por valor (1-5: 13 c/u, 6-10: 9 c/u), extracción y mezcla.
   - Turno: sacar carta, continuar, plantarse, guardar cartas, robo de cartas entre jugadores.
   - Regla de pérdida: casos <3 cartas, =3 cartas, >3 cartas, sin repetición.
   - Fin de juego y desempates.

2. **Pruebas de Integración (MockMvc + JPA/H2):**
   - Flujos HTTP completos controller -> service -> repository.
   - Códigos de estado HTTP, validaciones, control de errores.
   - Persistencia real en base de datos de pruebas (sin mocks de DB).

3. **Pruebas Funcionales E2E (Playwright + JavaScript):**
   - **Caso 1: Turno exitoso:** Crear partida -> iniciar -> guardar si aplica -> sacar carta -> plantarse -> verificar estado.
   - **Caso 2: Turno perdido:** Crear partida -> alcanzar >= 3 cartas con repetición -> verificar descarte y cambio de turno.

4. **Pruebas de Mutación:**
   - Plugin Pitest configurado en Maven.
   - Objetivo: Mutation score >= 90%, Cobertura 100%.

---

## 9. Entregables Obligatorios
1. Repositorio Git con carpetas separadas `backend/` y `frontend/`.
2. Archivo `README.md` con instrucciones detalladas de compilación, ejecución y testeo.
3. Colección de comandos `CURL` para todos los endpoints.
4. Reportes de cobertura (JaCoCo) y mutación (Pitest).
5. Suites de pruebas unitarias, integración y Playwright.
