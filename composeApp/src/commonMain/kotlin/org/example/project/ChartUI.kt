import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


// Data model for the line chart
data class LineChartData(
    val lines: List<Line>,
    val label: String = ""
) {
    data class Line(
        val points: List<Point>,
        val color: Color,
        val label: String = ""
    )

    data class Point(
        val timestamp: Long, // Unix timestamp in milliseconds
        val value: Float
    )
}

// Line chart composable with indicator
@Composable
fun LineChart(
    data: LineChartData,
    modifier: Modifier = Modifier,
    xAxisLabelCount: Int = 5,
    yAxisLabelCount: Int = 5
) {
    val maxValue = data.lines.maxOfOrNull { line -> line.points.maxOfOrNull { it.value } ?: 0f } ?: 1f
    val minValue = data.lines.minOfOrNull { line -> line.points.minOfOrNull { it.value } ?: 0f } ?: 0f
    val minTimestamp = data.lines.minOfOrNull { line -> line.points.minOfOrNull { it.timestamp } ?: 0L } ?: 0L
    val maxTimestamp = data.lines.maxOfOrNull { line -> line.points.maxOfOrNull { it.timestamp } ?: 0L } ?: 1L

    // State for indicator position and canvas size
    var indicatorX by remember { mutableFloatStateOf(-1f) } // -1f means no indicator
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    // Animation for line drawing
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }
    val yStep = (maxValue - minValue) / (yAxisLabelCount - 1)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
            .semantics { contentDescription = "Line chart with ${data.lines.size} lines" }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        indicatorX = offset.x.coerceIn(0f, size.width.toFloat())
                    }
                    detectDragGestures { change, _ ->
                        indicatorX = change.position.x.coerceIn(0f, size.width.toFloat())
                    }
                }
        ) {
            // Update canvas size
            canvasSize = size
            val canvasWidth = size.width
            val canvasHeight = size.height
            val xScale = canvasWidth / (maxTimestamp - minTimestamp).toFloat()
            val yScale = canvasHeight / (maxValue - minValue)

            // Draw y-axis grid lines

            for (i in 0 until yAxisLabelCount) {
                val y = canvasHeight - (i * yStep) * yScale
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
            }

            // Draw chart lines
            data.lines.forEach { line ->
                val path = Path()
                line.points.forEachIndexed { index, point ->
                    val x = (point.timestamp - minTimestamp) * xScale
                    val y = canvasHeight - (point.value - minValue) * yScale * animatedProgress.value
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(width = 4f)
                )
            }

            // Draw indicator line
            if (indicatorX >= 0f) {
                drawLine(
                    color = Color.Black,
                    start = Offset(indicatorX, 0f),
                    end = Offset(indicatorX, canvasHeight),
                    strokeWidth = 2f
                )
            }
        }

        // Y-axis labels
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(200.dp)
            ) {
                for (i in 0 until yAxisLabelCount) {
                    Text(
                        text = (minValue + i * yStep).roundToInt().toString(),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        color = Color.Black
                    )
                }
            }
        }

        // X-axis labels (timestamps)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = (maxTimestamp - minTimestamp) / (xAxisLabelCount - 1)
                for (i in 0 until xAxisLabelCount) {
                    val timestamp = minTimestamp + i * step
                    val instant = Instant.fromEpochMilliseconds(timestamp)
                    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    val formatted = "${dateTime.dayOfMonth}/${dateTime.monthNumber}"
                    Text(
                        text = formatted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
        }

        // Tooltip for indicator data
        if (indicatorX >= 0f && canvasSize != Size.Zero) {
            val xScale = canvasSize.width / (maxTimestamp - minTimestamp).toFloat()
            val selectedTimestamp = minTimestamp + (indicatorX / xScale).toLong()

            // Find closest point for each line
            val closestPoints = data.lines.map { line ->
                line to (line.points.minByOrNull { abs(it.timestamp - selectedTimestamp) } ?: line.points.first())
            }

            // Format tooltip content
            val instant = Instant.fromEpochMilliseconds(selectedTimestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val formattedTime = "${dateTime.dayOfMonth}/${dateTime.monthNumber} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = with(density) { (indicatorX / canvasSize.width * canvasSize.width.toDp()).coerceIn(0.dp, (canvasSize.width.toDp() - 50.dp)) },
                        y = 10.dp
                    )
                    .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Time: $formattedTime",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    closestPoints.forEach { (line, point) ->
                        Text(
                            text = "${line.label}: ${point.value.roundToInt()}",
                            fontSize = 12.sp,
                            color = line.color
                        )
                    }
                }
            }
        }
    }
}

// Dummy data for testing
@Composable
fun LineChartPreview() {
    MaterialTheme {
        // Generate dummy data: three lines over 30 days, points every 6 hours
        val now = Clock.System.now().toEpochMilliseconds()
        val dayInMillis = 24 * 60 * 60 * 1000L // One day
        val sixHoursInMillis = 6 * 60 * 60 * 1000L // 6 hours
        val days = 30
        val pointsPerLine = days * 4 // 4 points per day (every 6 hours)

        // Generate timestamps for 30 days, every 6 hours
        val timestamps = List(pointsPerLine) { index ->
            now - (days - 1) * dayInMillis + index * sixHoursInMillis
        }

        // Generate three lines: Temperature, Humidity, Pressure
        val chartData = LineChartData(
            lines = listOf(
                // Temperature: 15-30°C with variation
                LineChartData.Line(
                    points = timestamps.mapIndexed { index, timestamp ->
                        LineChartData.Point(
                            timestamp = timestamp,
                            value = 15f + (10f * sin(index * 0.2f)) + (5f * (index % 4))
                        )
                    },
                    color = Color.Blue,
                    label = "Temperature (°C)"
                ),
                // Humidity: 40-80% with variation
                LineChartData.Line(
                    points = timestamps.mapIndexed { index, timestamp ->
                        LineChartData.Point(
                            timestamp = timestamp,
                            value = 40f + (20f * cos(index * 0.15f)) + (10f * (index % 3))
                        )
                    },
                    color = Color.Red,
                    label = "Humidity (%)"
                ),
                // Pressure: 900-1100 hPa with variation
                LineChartData.Line(
                    points = timestamps.mapIndexed { index, timestamp ->
                        LineChartData.Point(
                            timestamp = timestamp,
                            value = 900f + (100f * sin(index * 0.1f)) + (50f * (index % 5))
                        )
                    },
                    color = Color.Green,
                    label = "Pressure (hPa)"
                )
            ),
            label = "Weather Data (30 Days)"
        )

        LineChart(
            data = chartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            xAxisLabelCount = 6,
            yAxisLabelCount = 5
        )
    }
}