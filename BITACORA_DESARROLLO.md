# Bitácora de Aprendizaje y Desarrollo: Proyecto Pelusas

Este documento registra paso a paso las decisiones de arquitectura, la estructura del proyecto y los comandos para ejecutar y probar tanto el backend como el frontend. Su propósito es servir como guía de estudio para comprender el funcionamiento integral de la aplicación y preparar la sustentación técnica.

---

## 1. Arquitectura General del Proyecto

El proyecto está dividido en dos partes independientes:

```
PELUSAS/
├── backend/            # Lógica de negocio, reglas del juego, persistencia y API REST (Java)
├── frontend/           # Interfaz de usuario web 2D e interacción (React)
├── REQUERIMIENTOS.md   # Especificación oficial de reglas y evaluación
├── README.md           # Ficha general del repositorio
└── BITACORA_DESARROLLO.md # Este documento de seguimiento y aprendizaje
```

### ¿Por qué esta separación?
- **Separación de responsabilidades:** El frontend solo se encarga de mostrar la interfaz y enviar peticiones del jugador. Toda la validación de reglas (si es su turno, si pierde, si roba cartas, conteo de mazo) ocurre en el backend.
- **Seguridad e integridad:** Si la lógica estuviera en el frontend, cualquier usuario podría manipularla desde la consola del navegador.

---

## 2. Estructura y Dependencias del Backend

El backend se construyó con **Java 21** y **Spring Boot 3.3.4** gestionado con **Maven**.

### Archivos clave:
- `backend/pom.xml`: Archivo de configuración de Maven donde se declaran las dependencias y plugins.
- `backend/src/main/resources/application.properties`: Configuración de puerto (`8080`), base de datos H2 y consola web.
- `backend/src/main/java/com/pelusas/game/PelusasApplication.java`: Punto de entrada que levanta el servidor Spring Boot.
- `backend/src/test/java/com/pelusas/game/PelusasApplicationTests.java`: Prueba inicial que valida que el contexto de Spring cargue correctamente.

### ¿Para qué sirve cada dependencia incluida en el `pom.xml`?
1. **`spring-boot-starter-web`**: Permite crear controladores REST (`@RestController`), recibir peticiones HTTP (`GET`, `POST`) y responder en formato JSON.
2. **`spring-boot-starter-data-jpa`**: Permite interactuar con la base de datos mediante entidades Java (`@Entity`) e interfaces de repositorio sin escribir sentencias SQL manuales.
3. **`spring-boot-starter-validation`**: Valida que los datos que envía el frontend cumplan condiciones mínimas (por ejemplo, nombres no vacíos o números válidos).
4. **`h2`**: Base de datos relacional en memoria. No requiere instalar servidores externos para desarrollo y pruebas; se crea al arrancar y se reinicia limpia.
5. **`lombok`**: Reduce código repetitivo en Java generando getters, setters, constructores y `toString` en tiempo de compilación.
6. **`spring-boot-starter-test`**: Provee JUnit 5 para pruebas unitarias, Mockito para simular dependencias aisladas y MockMvc para probar endpoints REST sin levantar un navegador real.
7. **`jacoco-maven-plugin`**: Analiza qué porcentaje del código está cubierto por pruebas automatizadas (la meta de evaluación es 100%).
8. **`pitest-maven`**: Aplica pruebas de mutación (introduce pequeños cambios en el código para comprobar si las pruebas realmente detectan los fallos; la meta de evaluación es ≥ 90%).

---

## 3. Estructura y Dependencias del Frontend

El frontend fue creado con **React 19** sobre **Vite** para ofrecer una experiencia rápida de recarga en caliente (HMR).

### Archivos y recursos clave:
- `frontend/package.json`: Lista de dependencias del ecosistema Node.js y scripts de ejecución.
- `frontend/vite.config.js`: Configura el puerto de desarrollo (`5175`) y el proxy hacia el backend (`/api` redirige a `http://localhost:8080`).
- `frontend/public/cards/`: Contiene los recursos gráficos oficiales de las cartas (`OneCard.png` a `TenCard.png`, `QuestionCard.png` para el reverso del mazo, y sus variantes `Mini*.png`). Al estar en `public/`, quedan disponibles directamente como URLs estáticas (`/cards/OneCard.png`) sin necesidad de importaciones complejas.
- `frontend/src/App.jsx`: Componente raíz de React (actualmente pantalla limpia con el fondo base del juego).
- `frontend/src/App.css`: Estilos del contenedor visual.
- `frontend/playwright.config.js`: Configuración del motor de pruebas funcionales automatizadas E2E.
- `frontend/tests/pelusas.spec.js`: Prueba funcional base que verifica la carga de la aplicación.

> **¿Por qué las imágenes van en el Frontend y no en el Backend?**  
> El backend en Java únicamente procesa reglas lógicas, números (valores del 1 al 10) y estados del juego (JSON). El frontend es el responsable exclusivo de la capa visual y de renderizar las imágenes correspondientes a cada carta para el usuario.

### ¿Para qué sirve cada herramienta en el Frontend?
1. **`React`**: Biblioteca para construir interfaces de usuario declarativas mediante componentes y estados.
2. **`Vite`**: Herramienta de compilación ultrarrápida para desarrollo frontend moderno.
3. **`Playwright`**: Herramienta para automatizar navegadores reales (Chromium) y ejecutar pruebas funcionales desde la perspectiva del usuario final (requerimiento 15).

---

## 4. Guía de Ejecución

### 4.1 Arrancar el Backend (Java)
Abrir una terminal en la carpeta `backend`:
```bash
cd C:\Users\USUARIO\Downloads\PELUSAS\backend
mvn spring-boot:run
```
- **URL del Backend:** `http://localhost:8080`
- **Consola de Base de Datos H2:** `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:pelusasdb`
  - Usuario: `sa`
  - Contraseña: *(vacía)*

#### Ejecutar pruebas del Backend:
```bash
# Pruebas unitarias
mvn test

# Reporte de cobertura JaCoCo
mvn test jacoco:report
# El reporte generado queda en: target/site/jacoco/index.html

# Pruebas de mutación PITest
mvn pitest:mutationCoverage
# El reporte generado queda en: target/pit-reports/index.html
```

---

### 4.2 Arrancar el Frontend (React)
Abrir una terminal en la carpeta `frontend`:
```bash
cd C:\Users\USUARIO\Downloads\PELUSAS\frontend
npm run dev
```
- **URL del Frontend:** `http://localhost:5175`

#### Ejecutar pruebas del Frontend (Playwright):
```bash
# Ejecución en consola
npm run test:e2e

# Ejecución con interfaz visual interactiva
npm run test:e2e:ui
```

---

## 5. Registro de Avance del Proyecto

| Fase | Descripción | Estado |
|---|---|---|
| **Fase 0** | Creación de repositorio, enlace con GitHub, especificación de requerimientos (`REQUERIMIENTOS.md`) | Completado |
| **Fase 1** | Configuración base de Backend (Spring Boot 3, Java 21, H2, JaCoCo, PITest) y Frontend (React, Vite, Playwright), dependencias instaladas y verificadas | Completado |
| **Fase 2** | Modelado de dominio en Java (Cartas, Mazo de 110 cartas con distribución 1-5 y 6-10, Jugador) con pruebas unitarias | Pendiente |
| **Fase 3** | Implementación de reglas del turno (Guardar, Buscar, Robar entre oponentes, Regla de pérdida) con pruebas | Pendiente |
| **Fase 4** | Persistencia e histórico en Base de Datos con Spring Data JPA | Pendiente |
| **Fase 5** | Controladores REST y pruebas de integración con MockMvc | Pendiente |
| **Fase 6** | Construcción de interfaz 2D y conexión con API REST | Pendiente |
| **Fase 7** | Pruebas funcionales obligatorias en Playwright y reportes finales | Pendiente |
