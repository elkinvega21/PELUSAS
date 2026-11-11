package com.pelusas.game.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pelusas Backend - Catálogo de APIs</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        background: #0f0a1c;
                        color: #f8fafc;
                        margin: 0;
                        padding: 30px 20px;
                        display: flex;
                        justify-content: center;
                    }
                    .container {
                        max-width: 820px;
                        width: 100%;
                    }
                    .header {
                        border-bottom: 2px solid #3b0764;
                        padding-bottom: 18px;
                        margin-bottom: 24px;
                    }
                    h1 {
                        color: #fbbf24;
                        margin: 0 0 10px 0;
                        font-size: 26px;
                        letter-spacing: 1px;
                    }
                    .status {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        background: rgba(34, 197, 94, 0.15);
                        color: #4ade80;
                        border: 1px solid #22c55e;
                        padding: 6px 14px;
                        border-radius: 20px;
                        font-size: 13px;
                        font-weight: 600;
                    }
                    .pulse {
                        width: 10px;
                        height: 10px;
                        background: #22c55e;
                        border-radius: 50%;
                        box-shadow: 0 0 10px #22c55e;
                    }
                    .card {
                        background: #191230;
                        border: 1px solid #372559;
                        border-radius: 8px;
                        padding: 16px 20px;
                        margin-bottom: 12px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                        transition: border-color 0.2s ease;
                    }
                    .card:hover {
                        border-color: #8b5cf6;
                    }
                    .endpoint-row {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                    }
                    .method {
                        font-weight: 800;
                        padding: 4px 8px;
                        border-radius: 4px;
                        font-size: 11px;
                        letter-spacing: 0.5px;
                    }
                    .get { background: #0284c7; color: #e0f2fe; }
                    .post { background: #16a34a; color: #dcfce7; }
                    .path {
                        font-family: monospace;
                        font-size: 15px;
                        color: #38bdf8;
                        font-weight: bold;
                    }
                    .desc {
                        color: #94a3b8;
                        font-size: 13px;
                        margin: 8px 0 0 0;
                        line-height: 1.4;
                    }
                    code {
                        background: #0d061a;
                        padding: 2px 6px;
                        border-radius: 4px;
                        color: #fde047;
                        font-size: 12px;
                    }
                    .links-bar {
                        margin-top: 28px;
                        display: flex;
                        gap: 14px;
                        flex-wrap: wrap;
                    }
                    a.btn {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        background: #7c3aed;
                        color: #ffffff;
                        padding: 12px 20px;
                        border-radius: 6px;
                        text-decoration: none;
                        font-weight: bold;
                        font-size: 13px;
                        transition: background-color 0.15s ease;
                    }
                    a.btn:hover { background: #6d28d9; }
                    a.btn-secondary { background: #334155; }
                    a.btn-secondary:hover { background: #475569; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎮 Pelusas Backend REST API</h1>
                        <div class="status"><span class="pulse"></span> Servidor Spring Boot Online en Puerto 8080</div>
                    </div>

                    <h2 style="font-size: 17px; color: #cbd5e1; margin-bottom: 16px;">Endpoints REST Disponibles:</h2>

                    <div class="card">
                        <div class="endpoint-row"><span class="method post">POST</span> <span class="path">/api/partidas</span></div>
                        <p class="desc">Crea una nueva partida con la lista de jugadores. Body: <code>{"jugadores": ["Tú", "Amigo"]}</code></p>
                    </div>

                    <div class="card">
                        <div class="endpoint-row"><span class="method get">GET</span> <span class="path">/api/partidas/{id}</span></div>
                        <p class="desc">Obtiene el estado completo de una partida por su ID UUID.</p>
                    </div>

                    <div class="card">
                        <div class="endpoint-row"><span class="method post">POST</span> <span class="path">/api/partidas/{id}/sacar-carta</span></div>
                        <p class="desc">El jugador en turno saca una carta del mazo. Body: <code>{"jugador": "Nombre"}</code></p>
                    </div>

                    <div class="card">
                        <div class="endpoint-row"><span class="method post">POST</span> <span class="path">/api/partidas/{id}/guardar</span></div>
                        <p class="desc">Guarda las cartas acumuladas en el montón seguro. Body: <code>{"jugador": "Nombre"}</code></p>
                    </div>

                    <div class="card">
                        <div class="endpoint-row"><span class="method post">POST</span> <span class="path">/api/partidas/{id}/plantarse</span></div>
                        <p class="desc">El jugador se planta y pasa el turno al siguiente. Body: <code>{"jugador": "Nombre"}</code></p>
                    </div>

                    <div class="card">
                        <div class="endpoint-row"><span class="method get">GET</span> <span class="path">/api/partidas/historico</span></div>
                        <p class="desc">Consulta el historial de todas las partidas finalizadas.</p>
                    </div>

                    <div class="links-bar">
                        <a href="/h2-console" class="btn">🗄️ Abrir Consola H2 Database</a>
                        <a href="http://localhost:5175" class="btn btn-secondary">🕹️ Ir al Juego (Frontend :5175)</a>
                    </div>
                </div>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }
}

