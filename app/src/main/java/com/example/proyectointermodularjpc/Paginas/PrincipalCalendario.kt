package com.example.proyectointermodularjpc.Paginas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectointermodularjpc.ConsumoApiSpringboot.model.Trabajo
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeParseException

/* Aqui fijaremos un calendario y una lista debajo del calendario con las tareas del dia
 * que seleccionemos (pudiendo desmarcar el dia). Los dias que tengan una tarea asignada
 * tendran debajo del numero de dia un punto azul para resaltar que hay una tarea.
 */

@Immutable
data class TrabajoUI(
    val id: Long?,
    val titulo: String,
    val descripcion: String?,
    val fechaProgramada: String,
    val prioridad: String,
)

private fun Trabajo.aUi(): TrabajoUI = TrabajoUI(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaProgramada = fechaProgramada.orEmpty(),
    prioridad = prioridad.name,
)

@Composable
fun PrincipalCalendario(
    modifier: Modifier = Modifier,
    zoneId: ZoneId = ZoneId.systemDefault(),
    trabajos: List<Trabajo> = emptyList(),
) {
    val hoy = remember { LocalDate.now(zoneId) }
    var mesActual by remember { mutableStateOf(YearMonth.from(hoy)) }
    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(hoy) }

    val trabajosUi = remember(trabajos) { trabajos.map { it.aUi() } }

    val diasConTareas = remember(trabajosUi, mesActual, zoneId) {
        trabajosUi
            .mapNotNull { it.fechaProgramada.aFechaLocal(zoneId) }
            .filter { YearMonth.from(it) == mesActual }
            .toSet()
    }

    val trabajosFiltrados = remember(trabajosUi, fechaSeleccionada, zoneId) {
        if (fechaSeleccionada == null) emptyList()
        else trabajosUi
            .filter { it.fechaProgramada.aFechaLocal(zoneId) == fechaSeleccionada }
            .sortedBy { it.fechaProgramada }
    }

    Column(modifier = modifier.padding(16.dp)) {
        CabeceraMes(
            mesActual = mesActual,
            alAnterior = { mesActual = mesActual.minusMonths(1) },
            alSiguiente = { mesActual = mesActual.plusMonths(1) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        RejillaCalendarioMes(
            yearMonth = mesActual,
            fechaSeleccionada = fechaSeleccionada,
            diasConTareas = diasConTareas,
            alPulsarDia = { fechaPulsada ->
                fechaSeleccionada = if (fechaSeleccionada == fechaPulsada) null else fechaPulsada
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fechaSeleccionada?.let { "Tareas de ${it.dayOfMonth}/${it.monthValue}/${it.year}" }
                ?: "Selecciona un día para ver sus tareas",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (fechaSeleccionada == null) {
            Text(
                text = "(Día no seleccionado)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (trabajosFiltrados.isEmpty()) {
            Text(
                text = "No hay tareas para este día.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = trabajosFiltrados,
                    key = { it.id ?: (it.titulo + it.fechaProgramada) },
                ) { trabajo ->
                    FilaTrabajo(trabajo = trabajo, zoneId = zoneId)
                }
            }
        }
    }
}

@Composable
private fun CabeceraMes(
    mesActual: YearMonth,
    alAnterior: () -> Unit,
    alSiguiente: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "<",
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = alAnterior)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "${mesActual.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${mesActual.year}",
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = ">",
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = alSiguiente)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun RejillaCalendarioMes(
    yearMonth: YearMonth,
    fechaSeleccionada: LocalDate?,
    diasConTareas: Set<LocalDate>,
    alPulsarDia: (LocalDate) -> Unit,
) {
    val primerDiaDelMes = remember(yearMonth) { yearMonth.atDay(1) }
    val diasEnMes = remember(yearMonth) { yearMonth.lengthOfMonth() }

    // Queremos calendario empezando en Lunes.
    val primerDiaSemana = DayOfWeek.MONDAY
    val desplazamiento = remember(yearMonth) {
        val indice = (primerDiaDelMes.dayOfWeek.value - primerDiaSemana.value + 7) % 7
        indice
    }

    val celdasTotales = desplazamiento + diasEnMes
    val filas = (celdasTotales + 6) / 7

    CabeceraSemana(primerDiaSemana = primerDiaSemana)

    Spacer(modifier = Modifier.height(4.dp))

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        var dia = 1
        repeat(filas) { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(7) { col ->
                    val indiceCelda = fila * 7 + col
                    if (indiceCelda < desplazamiento || dia > diasEnMes) {
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    } else {
                        val fecha = yearMonth.atDay(dia)
                        CeldaDia(
                            fecha = fecha,
                            estaSeleccionado = fechaSeleccionada == fecha,
                            tieneTarea = diasConTareas.contains(fecha),
                            alPulsar = { alPulsarDia(fecha) },
                            modifier = Modifier.weight(1f),
                        )
                        dia++
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraSemana(primerDiaSemana: DayOfWeek) {
    val etiquetas = remember(primerDiaSemana) {
        // Desde lunes.
        listOf("L", "M", "X", "J", "V", "S", "D")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        etiquetas.forEach { etiqueta ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CeldaDia(
    fecha: LocalDate,
    estaSeleccionado: Boolean,
    tieneTarea: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fondo = if (estaSeleccionado) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface

    val colorContenido = if (estaSeleccionado) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = alPulsar),
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = fecha.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorContenido,
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (tieneTarea) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(modifier = Modifier.size(6.dp))
                }
            }
        }
    }
}

@Composable
private fun FilaTrabajo(trabajo: TrabajoUI, zoneId: ZoneId) {
    val fecha = remember(trabajo.fechaProgramada, zoneId) {
        trabajo.fechaProgramada.aFechaLocal(zoneId)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = trabajo.titulo.ifBlank { "(Sin título)" },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (trabajo.prioridad.isNotBlank()) {
                    Text(
                        text = trabajo.prioridad,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            val descripcion = trabajo.descripcion
            if (!descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = fecha?.let { "${it.dayOfMonth}/${it.monthValue}/${it.year}" } ?: "Sin fecha",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun String.aFechaLocal(zoneId: ZoneId): LocalDate? {
    val raw = this.trim()
    if (raw.isEmpty()) return null

    // Intento 1: ISO local date (yyyy-MM-dd)
    try {
        return LocalDate.parse(raw)
    } catch (_: DateTimeParseException) {
    }

    // Intento 2: ISO local datetime (yyyy-MM-ddTHH:mm:ss)
    try {
        return LocalDateTime.parse(raw).toLocalDate()
    } catch (_: DateTimeParseException) {
    }

    // Intento 3: ISO offset datetime (2026-02-23T10:15:30+01:00)
    try {
        return OffsetDateTime.parse(raw).atZoneSameInstant(zoneId).toLocalDate()
    } catch (_: DateTimeParseException) {
    }

    // Intento 4: epoch millis en texto
    val millis = raw.toLongOrNull()
    if (millis != null && millis > 0L) {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    }

    return null
}

@Deprecated("Ya no se usa: la fecha programada viene como String desde la API")
private fun Long.aFechaLocal(zoneId: ZoneId): LocalDate {
    if (this <= 0L) return LocalDate.MIN
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
