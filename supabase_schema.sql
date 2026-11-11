-- ===============================================================
-- ESQUEMA COMPLETO DE BASE DE DATOS SUPABASE PARA PELUSAS
-- Ejecuta este script en el SQL Editor de tu Dashboard de Supabase
-- ===============================================================

-- 1. Tabla de sesiones de jugadores (registro de ingreso)
CREATE TABLE IF NOT EXISTS public.jugadores_sesiones (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    fecha_ingreso TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 2. Tabla de historial de partidas finalizadas
CREATE TABLE IF NOT EXISTS public.historial_partidas (
    id TEXT PRIMARY KEY,
    fecha TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    modo TEXT NOT NULL,
    ganador TEXT NOT NULL,
    puntos_ganador INTEGER DEFAULT 0,
    cartas_ganador INTEGER DEFAULT 0,
    jugadores_detalle JSONB DEFAULT '[]'::jsonb
);

-- 3. Tabla de ranking acumulado de victorias
CREATE TABLE IF NOT EXISTS public.ranking_jugadores (
    nombre TEXT PRIMARY KEY,
    victorias INTEGER DEFAULT 0,
    partidas INTEGER DEFAULT 0,
    puntos_totales INTEGER DEFAULT 0,
    ultima_victoria TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW())
);

-- ===============================================================
-- HABILITAR ROW LEVEL SECURITY (RLS) Y POLÍTICAS PÚBLICAS (ANON)
-- ===============================================================

ALTER TABLE public.jugadores_sesiones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.historial_partidas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ranking_jugadores ENABLE ROW LEVEL SECURITY;

-- Políticas para jugadores_sesiones
DROP POLICY IF EXISTS "Permitir insercion publica en jugadores_sesiones" ON public.jugadores_sesiones;
CREATE POLICY "Permitir insercion publica en jugadores_sesiones"
    ON public.jugadores_sesiones FOR INSERT TO anon, authenticated
    WITH CHECK (true);

DROP POLICY IF EXISTS "Permitir lectura publica en jugadores_sesiones" ON public.jugadores_sesiones;
CREATE POLICY "Permitir lectura publica en jugadores_sesiones"
    ON public.jugadores_sesiones FOR SELECT TO anon, authenticated
    USING (true);

-- Políticas para historial_partidas
DROP POLICY IF EXISTS "Permitir insercion publica en historial_partidas" ON public.historial_partidas;
CREATE POLICY "Permitir insercion publica en historial_partidas"
    ON public.historial_partidas FOR INSERT TO anon, authenticated
    WITH CHECK (true);

DROP POLICY IF EXISTS "Permitir lectura publica en historial_partidas" ON public.historial_partidas;
CREATE POLICY "Permitir lectura publica en historial_partidas"
    ON public.historial_partidas FOR SELECT TO anon, authenticated
    USING (true);

-- Políticas para ranking_jugadores
DROP POLICY IF EXISTS "Permitir todo en ranking_jugadores" ON public.ranking_jugadores;
CREATE POLICY "Permitir todo en ranking_jugadores"
    ON public.ranking_jugadores FOR ALL TO anon, authenticated
    USING (true)
    WITH CHECK (true);

