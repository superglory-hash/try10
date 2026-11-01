package com.seoul.fmonair

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionToken = SessionToken(this, ComponentName(this, RadioPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, mainExecutor)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                SeoulRadioAppScreen(
                    playStation = { station -> playStation(station) },
                    stopPlayback = { mediaController?.stop() }
                )
            }
        }
    }

    private fun playStation(station: RadioStation) {
        val controller = mediaController ?: return
        // Background thread for URL resolution
        Thread {
            kotlinx.coroutines.runBlocking {
                val resolvedUrl = StationRepository.resolvePlayableStreamUrl(station)
                if (resolvedUrl.isNotEmpty()) {
                    runOnUiThread {
                        controller.setMediaItem(MediaItem.fromUri(resolvedUrl))
                        controller.prepare()
                        controller.play()
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
    }
}

@Composable
fun SeoulRadioAppScreen(playStation: (RadioStation) -> Unit, stopPlayback: () -> Unit) {
    var currentStation by remember { mutableStateOf<RadioStation?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Seoul FM OnAir",
            color = Color(0xFF00E5FF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        // Player Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(currentStation?.name ?: "채널을 선택하세요", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentStation?.frequency ?: "-", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { 
                    currentStation?.let { playStation(it) }
                    isPlaying = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAA00FF))
            ) {
                Text("재생 / 새로고침")
            }
            Button(
                onClick = { 
                    stopPlayback()
                    isPlaying = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("정지")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Station Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StationRepository.stations) { station ->
                Button(
                    onClick = {
                        currentStation = station
                        playStation(station)
                        isPlaying = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStation == station) Color(0xFF00E5FF) else Color(0xFF2C2C2C),
                        contentColor = if (currentStation == station) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(station.name, fontSize = 14.sp)
                }
            }
        }
    }
}
