import { supabase } from '../supabaseClient';

const LOCAL_SESSIONS_KEY = 'pelusas_local_sessions';
const LOCAL_HISTORY_KEY = 'pelusas_local_history';
const LOCAL_RANKING_KEY = 'pelusas_local_ranking';

/**
 * Registra a la persona que entra al juego.
 * Se guarda en Supabase (tabla 'jugadores_sesiones') y en almacenamiento local de respaldo.
 */
export async function registrarIngresoJugador(nombre) {
  const jugadorLimpio = (nombre || 'Jugador').trim();
  const sesion = {
    id: `sess-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
    nombre: jugadorLimpio,
    fecha_ingreso: new Date().toISOString(),
  };

  // Guardar en caché local
  try {
    const prev = JSON.parse(localStorage.getItem(LOCAL_SESSIONS_KEY) || '[]');
    prev.unshift(sesion);
    localStorage.setItem(LOCAL_SESSIONS_KEY, JSON.stringify(prev.slice(0, 50)));
  } catch {
    // ignorar error de quota
  }

  // Intentar guardar en Supabase
  try {
    const { data, error } = await supabase
      .from('jugadores_sesiones')
      .insert([sesion])
      .select();

    if (error) {
      console.warn('[Supabase] Aviso al registrar jugador (usando respaldo local):', error.message);
    }
    return data || sesion;
  } catch (err) {
    console.warn('[Supabase] Error de conexión:', err);
    return sesion;
  }
}

/**
 * Guarda el historial completo de una partida terminada y actualiza
 * el ranking de la persona que ha ganado más veces.
 */
export async function guardarPartidaFinalizada({ modo = 'local', ganador, resultados = [] }) {
  if (!ganador) return null;

  const registroPartida = {
    id: `game-${Date.now()}`,
    fecha: new Date().toISOString(),
    modo,
    ganador: ganador.name,
    puntos_ganador: ganador.score || 0,
    cartas_ganador: ganador.cardsCount || 0,
    jugadores_detalle: resultados.map(r => ({
      nombre: r.name,
      puntos: r.score,
      cartas: r.cardsCount,
      esBot: Boolean(r.isBot),
    })),
  };

  // 1. Guardar en historial local
  try {
    const historialPrev = JSON.parse(localStorage.getItem(LOCAL_HISTORY_KEY) || '[]');
    historialPrev.unshift(registroPartida);
    localStorage.setItem(LOCAL_HISTORY_KEY, JSON.stringify(historialPrev.slice(0, 50)));

    // Actualizar ranking local
    const rankingPrev = JSON.parse(localStorage.getItem(LOCAL_RANKING_KEY) || '{}');
    resultados.forEach(r => {
      if (!rankingPrev[r.name]) {
        rankingPrev[r.name] = { nombre: r.name, victorias: 0, partidas: 0, puntosTotales: 0 };
      }
      rankingPrev[r.name].partidas += 1;
      rankingPrev[r.name].puntosTotales += r.score || 0;
    });
    if (rankingPrev[ganador.name]) {
      rankingPrev[ganador.name].victorias += 1;
    }
    localStorage.setItem(LOCAL_RANKING_KEY, JSON.stringify(rankingPrev));
  } catch {
    // ignorar
  }

  // 2. Guardar en Supabase (historial_partidas)
  try {
    const { error: histError } = await supabase
      .from('historial_partidas')
      .insert([{
        id: registroPartida.id,
        fecha: registroPartida.fecha,
        modo: registroPartida.modo,
        ganador: registroPartida.ganador,
        puntos_ganador: registroPartida.puntos_ganador,
        cartas_ganador: registroPartida.cartas_ganador,
        jugadores_detalle: registroPartida.jugadores_detalle,
      }]);

    if (histError) {
      console.warn('[Supabase] Historial guardado localmente (tabla remota en configuración):', histError.message);
    }
  } catch (err) {
    console.warn('[Supabase] Error al guardar partida:', err);
  }

  // 3. Actualizar ranking en Supabase (ranking_jugadores)
  try {
    const { data: existente } = await supabase
      .from('ranking_jugadores')
      .select('*')
      .eq('nombre', ganador.name)
      .maybeSingle();

    if (existente) {
      await supabase
        .from('ranking_jugadores')
        .update({
          victorias: (existente.victorias || 0) + 1,
          partidas: (existente.partidas || 0) + 1,
          puntos_totales: (existente.puntos_totales || 0) + (ganador.score || 0),
          ultima_victoria: new Date().toISOString(),
        })
        .eq('nombre', ganador.name);
    } else {
      await supabase
        .from('ranking_jugadores')
        .insert([{
          nombre: ganador.name,
          victorias: 1,
          partidas: 1,
          puntos_totales: ganador.score || 0,
          ultima_victoria: new Date().toISOString(),
        }]);
    }
  } catch (err) {
    console.warn('[Supabase] Error al actualizar ranking en Supabase:', err);
  }

  return registroPartida;
}

/**
 * Obtiene el ranking de jugadores que han ganado más veces.
 */
export async function obtenerRankingMasVictorias() {
  try {
    const { data, error } = await supabase
      .from('ranking_jugadores')
      .select('*')
      .order('victorias', { ascending: false })
      .limit(10);

    if (!error && data && data.length > 0) {
      return data;
    }
  } catch (err) {
    console.warn('[Supabase] Usando ranking en respaldo local:', err);
  }

  // Respaldo desde LocalStorage
  try {
    const rankingMap = JSON.parse(localStorage.getItem(LOCAL_RANKING_KEY) || '{}');
    const list = Object.values(rankingMap).map(item => ({
      nombre: item.nombre,
      victorias: item.victorias || 0,
      partidas: item.partidas || 0,
      puntos_totales: item.puntosTotales || 0,
    }));
    list.sort((a, b) => b.victorias - a.victorias || b.puntos_totales - a.puntos_totales);
    return list;
  } catch {
    return [];
  }
}

/**
 * Obtiene el historial de partidas terminadas.
 */
export async function obtenerHistorialPartidas() {
  try {
    const { data, error } = await supabase
      .from('historial_partidas')
      .select('*')
      .order('fecha', { ascending: false })
      .limit(20);

    if (!error && data && data.length > 0) {
      return data;
    }
  } catch (err) {
    console.warn('[Supabase] Usando historial en respaldo local:', err);
  }

  try {
    return JSON.parse(localStorage.getItem(LOCAL_HISTORY_KEY) || '[]');
  } catch {
    return [];
  }
}

