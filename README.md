# 🎲 Pelusas - Juego de Mesa Digital

Proyecto final de desarrollo web y calidad de software para el juego de mesa **Pelusas**.  
Repositorio oficial: [https://github.com/elkinvega21/PELUSAS.git](https://github.com/elkinvega21/PELUSAS.git)

---

##  Integrantes del Equipo
- **Estudiante(s):** Elkin Vega

---

##  Tecnologías Utilizadas

### Backend
- **Lenguaje:** Java 21 LTS
- **Framework:** Spring Boot 3.3.x (Spring Web, Spring Data JPA, Bean Validation)
- **Base de Datos:** H2 Database (en memoria para desarrollo y pruebas rápidas) / PostgreSQL configurable
- **Pruebas Unitarias:** JUnit 5 + Mockito
- **Pruebas de Integración:** MockMvc y persistencia JPA real
- **Análisis y Mutación:** 
  - JaCoCo (Objetivo: 100% Cobertura de código)
  - PITest (Objetivo: ≥ 90% Mutation Score)

### Frontend
- **Framework:** React 19 + Vite 8
- **Estilo:** CSS Moderno / 2D Game Board Design con animaciones fluidas
- **Iconografía e Interacción:** Lucide React, Canvas Confetti
- **Pruebas Funcionales E2E:** Playwright + JavaScript

---

##  Arquitectura del Sistema

El proyecto sigue una arquitectura desacoplada y por capas con estricta separación de responsabilidades:

```
PELUSAS/
├── backend/                   # API REST y Motor de Reglas en Java / Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pelusas/game/
│   │   │   │   ├── domain/        # Entidades, Cartas, Mazo (110 cartas) y Turnos
│   │   │   │   ├── service/       # Lógica de negocio y reglas del juego
│   │   │   │   ├── repository/    # Persistencia de partidas e histórico
│   │   │   │   └── controller/    # Endpoints REST
│   │   │   └── resources/         # Configuración y propiedades (H2, logging)
│   │   └── test/                  # Pruebas Unitarias e Integración (MockMvc)
│   └── pom.xml                    # Configuración Maven, JaCoCo y PITest
├── frontend/                  # Interfaz Web 2D y Pruebas E2E
│   ├── src/                       # Componentes de tablero, cartas, jugadores
│   ├── tests/                     # Pruebas funcionales con Playwright + JS
│   ├── playwright.config.js       # Configuración de Playwright E2E
│   └── package.json               # Dependencias de Vite, React y Playwright
├── REQUERIMIENTOS.md          # Especificación estricta y reglas del negocio
└── README.md                  # Guía de instalación, ejecución y pruebas
```

### Regla de Oro Arquitectural
El **Frontend** es reactivo y solicita acciones del usuario. El **Backend** es la única fuente de verdad: valida turnos, gestiona el mazo de 110 cartas, calcula robos automáticos de cartas entre oponentes, aplica la regla de pérdida (repetición con 3 o más cartas) y persiste el histórico en base de datos.

---

## Requisitos Previos
- **Java:** JDK 21 o superior instalado (`java -version`)
- **Maven:** 3.8+ instalado (`mvn -version`) o uso del wrapper
- **Node.js:** v20+ o v22+ (`node -v`)
- **npm:** v10+ u 11+ (`npm -v`)
- **Navegador:** Google Chrome / Chromium para Playwright

---

## Instalación y Configuración

Clonar el repositorio:
```bash
git clone https://github.com/elkinvega21/PELUSAS.git
cd PELUSAS
```

### 1. Backend (Java)
```bash
cd backend
mvn clean install -DskipTests
```

### 2. Frontend (React)
```bash
cd ../frontend
npm install
npx playwright install chromium
```

---

## ▶Ejecución del Proyecto

### 1. Iniciar el Backend
En una terminal:
```bash
cd backend
mvn spring-boot:run
```
El servidor backend se iniciará en `http://localhost:8080`.  
Consola H2 disponible en: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:pelusasdb`, Usuario: `sa`, Contraseña: vacía).

### 2. Iniciar el Frontend
En otra terminal:
```bash
cd frontend
npm run dev
```
La aplicación web se ejecutará en `http://localhost:5173`.

---

##  Ejecución de Pruebas Automatizadas

### 1. Pruebas Unitarias (Backend)
Ejecuta las pruebas unitarias con JUnit 5 y Mockito:
```bash
cd backend
mvn test
```

### 2. Pruebas de Integración (Backend)
Ejecuta las pruebas de integración con MockMvc y base de datos H2 real:
```bash
cd backend
mvn test -Dtest=*IntegrationTest
```

### 3. Reporte de Cobertura de Código (JaCoCo)
Genera el reporte de cobertura HTML (Meta: 100%):
```bash
cd backend
mvn test jacoco:report
```
El reporte se encuentra en: `backend/target/site/jacoco/index.html`.

### 4. Pruebas de Mutación (PITest)
Genera el análisis de mutación (Meta: ≥ 90% score):
```bash
cd backend
mvn pitest:mutationCoverage
```
El reporte de mutación se genera en: `backend/target/pit-reports/index.html`.

### 5. Pruebas Funcionales E2E (Playwright + JavaScript)
Ejecuta las pruebas funcionales de extremo a extremo:
```bash
cd frontend
npm run test:e2e
```
Para ver la interfaz interactiva de Playwright:
```bash
cd frontend
npm run test:e2e:ui
```
Para consultar el reporte HTML de Playwright:
```bash
cd frontend
npm run test:e2e:report
```

---

## 📖 Reglas Principales del Juego

1. **Mazo de 110 Cartas:**
   - Valores del 1 al 5: 13 cartas de cada valor (65 cartas).
   - Valores del 6 al 10: 9 cartas de cada valor (45 cartas).
2. **Turno de Juego:**
   - **Paso 1: Guardar pelusas:** Si el jugador tiene cartas de su turno anterior delante de él, las guarda en su montón de puntos (quedan seguras).
   - **Paso 2: Buscar pelusas:** Saca una carta del mazo. Puede plantarse o seguir sacando cartas.
   - **Paso 3: Robar cartas:** Si saca un valor que otro jugador tiene boca arriba delante de él, se las roba de inmediato.
3. **Regla de Pérdida:**
   - Si saca una carta repetida (mismo valor que ya tiene delante) **Y** tiene 3 o más cartas en total delante de él:
     - Pierde el turno inmediatamente.
     - Todas las cartas que tenía delante son descartadas.
     - Pasa el turno al siguiente jugador.
4. **Fin de Partida:**
   - Cuando se agotan las cartas del mazo. Todos guardan las cartas restantes, se suman los puntos y se declara el ganador.
