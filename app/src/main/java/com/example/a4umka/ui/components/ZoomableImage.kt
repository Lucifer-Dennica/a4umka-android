package com.example.a4umka.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds // <-- ПРАВИЛЬНЫЙ ИМПОРТ
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import kotlin.math.abs

@Composable
fun ZoomableImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                // Умный обработчик: не мешает листать страницы, если нет зума
                awaitEachGesture {
                    var zoom = 1f
                    var pan = Offset.Zero
                    val touchSlop = viewConfiguration.touchSlop
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            zoom *= zoomChange
                            pan += panChange

                            val zoomMotion = abs(1 - zoom) * 10
                            val panMotion = pan.getDistance()

                            if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                // Перехватываем жест только если картинка увеличена или мы ее увеличиваем
                                if (scale > 1f || zoom > 1.1f) {
                                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                                    offset += panChange
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Предпросмотр",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
