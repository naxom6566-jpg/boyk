package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GeneratedMediaEntity
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun CreateScreen(viewModel: AssistantViewModel) {
    var selectedMediaTab by remember { mutableStateOf("image") } // image, video, audio
    val media by viewModel.media.collectAsState()
    val isGenerating by viewModel.isGeneratingMedia.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("create_screen")
    ) {
        // --- Modular creation subtabs design ---
        TabRow(
            selectedTabIndex = when (selectedMediaTab) {
                "image" -> 0
                "video" -> 1
                else -> 2
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedMediaTab == "image",
                onClick = { selectedMediaTab = "image" },
                text = { Text("Image") },
                icon = { Icon(Icons.Default.Brush, contentDescription = "Image Gen") },
                modifier = Modifier.testTag("tab_image_gen")
            )
            Tab(
                selected = selectedMediaTab == "video",
                onClick = { selectedMediaTab = "video" },
                text = { Text("Video") },
                icon = { Icon(Icons.Default.Videocam, contentDescription = "Video Gen") },
                modifier = Modifier.testTag("tab_video_gen")
            )
            Tab(
                selected = selectedMediaTab == "audio",
                onClick = { selectedMediaTab = "audio" },
                text = { Text("Audio") },
                icon = { Icon(Icons.Default.AudioFile, contentDescription = "Audio Gen") },
                modifier = Modifier.testTag("tab_audio_gen")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isGenerating) {
                // Spinning stars creation screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Processing Latent Diffusion...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Synthesizing visual pixels. Safe local rendering mode.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                when (selectedMediaTab) {
                    "image" -> ImagePlayground(
                        mediaList = media.filter { it.type == "IMAGE" },
                        onGenerate = { p, r, s -> viewModel.generateImage(p, r, s) },
                        onToggleFav = { id, f -> viewModel.toggleMediaFavorite(id, f) },
                        onDelete = { id -> viewModel.deleteMediaItem(id) }
                    )
                    "video" -> VideoPlayground(
                        mediaList = media.filter { it.type == "VIDEO" },
                        onGenerate = { p, m, d -> viewModel.generateVideo(p, m, d) },
                        onToggleFav = { id, f -> viewModel.toggleMediaFavorite(id, f) },
                        onDelete = { id -> viewModel.deleteMediaItem(id) }
                    )
                    "audio" -> AudioPlayground(
                        mediaList = media.filter { it.type == "AUDIO" },
                        onGenerate = { p, v, s -> viewModel.generateAudio(p, v, s) },
                        onToggleFav = { id, f -> viewModel.toggleMediaFavorite(id, f) },
                        onDelete = { id -> viewModel.deleteMediaItem(id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePlayground(
    mediaList: List<GeneratedMediaEntity>,
    onGenerate: (prompt: String, ratio: String, style: String) -> Unit,
    onToggleFav: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf("Cinematic") }

    val stylesList = listOf("Cinematic", "Cyberpunk", "Anime", "Surrealist", "3D Render", "Watercolor")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text Input Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Creative Prompt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("An astronaut playing guitar on neon Mars, cyberpunk style...") },
                    modifier = Modifier.fillMaxWidth().testTag("image_prompt_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Style selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Render Style", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stylesList) { style ->
                        val active = selectedStyle == style
                        FilterChip(
                            selected = active,
                            onClick = { selectedStyle = style },
                            label = { Text(style) }
                        )
                    }
                }
            }
        }

        // Aspect Ratio Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aspect Ratio Selection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    aspectRatios.forEach { ratio ->
                        val active = selectedRatio == ratio
                        OutlinedButton(
                            onClick = { selectedRatio = ratio },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(ratio)
                        }
                    }
                }
            }
        }

        // Generate Buttons
        item {
            Button(
                onClick = {
                    if (promptInput.trim().isNotEmpty()) {
                        onGenerate(promptInput, selectedRatio, selectedStyle)
                        promptInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_image_generation"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Spark Grid")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Synthesize Image Rendering")
            }
        }

        // Output Gallery Grid banner
        item {
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your Created Images Gallery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (mediaList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Brush, contentDescription = "None", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No images created yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            // LazyVerticalGrid doesn't fit inside LazyColumn directly unless nested in fixed height or items, so we stack rows or render custom grid in item
            val chunked = mediaList.chunked(2)
            items(chunked) { rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowList.forEach { m ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            // Loaded image using Coil AsyncImage
                            AsyncImage(
                                model = m.url,
                                contentDescription = m.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Quick overlay for favorite and delete
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onToggleFav(m.id, !m.isFavorite) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (m.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Fav",
                                            tint = if (m.isFavorite) Color.Red else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDelete(m.id) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Del",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (rowList.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayground(
    mediaList: List<GeneratedMediaEntity>,
    onGenerate: (prompt: String, motion: String, duration: Int) -> Unit,
    onToggleFav: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(5) }
    var selectedMotion by remember { mutableStateOf("Hyper-Pan") }

    val motionStyles = listOf("Hyper-Pan", "Zoom-In", "Orbit", "Slow Tracking", "Dolly Orbit")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Video Narrative Prompt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("A cinematic drone sweep of a lost futuristic jungle civilization, high definition...") },
                    modifier = Modifier.fillMaxWidth().testTag("video_prompt_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Active Camera Motion Direction", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(motionStyles) { motion ->
                        FilterChip(
                            selected = selectedMotion == motion,
                            onClick = { selectedMotion = motion },
                            label = { Text(motion) }
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Temporal Length: $selectedDuration seconds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Slider(
                    value = selectedDuration.toFloat(),
                    onValueChange = { selectedDuration = it.toInt() },
                    valueRange = 3f..10f,
                    steps = 7
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (promptInput.trim().isNotEmpty()) {
                        onGenerate(promptInput, selectedMotion, selectedDuration)
                        promptInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_video_generation"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Runway video")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Synthesize Video Narrative")
            }
        }

        item {
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Created Video Clips", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (mediaList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.OndemandVideo, contentDescription = "None", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No video files synthesized yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(mediaList) { item ->
                VideoPreviewCard(
                    media = item,
                    onToggleFav = { onToggleFav(item.id, !item.isFavorite) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }
    }
}

@Composable
fun VideoPreviewCard(
    media: GeneratedMediaEntity,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Simulated preview layout containing play logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Gradient or visual particle to show life
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF232526),
                                    Color(0xFF414345)
                                )
                            )
                        )
                )

                IconButton(
                    onClick = {
                        Toast.makeText(context, "Playing synthesized video stream: ${media.style}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Aspect/Time tags
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${media.durationSec}s • MP4",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = media.prompt,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Style: ${media.style}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onToggleFav) {
                        Icon(
                            imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Fav",
                            tint = if (media.isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayground(
    mediaList: List<GeneratedMediaEntity>,
    onGenerate: (prompt: String, voice: String, speed: Float) -> Unit,
    onToggleFav: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf("Serena-V1") }
    var selectedSpeed by remember { mutableStateOf(1.0f) }

    val voiceOptions = listOf("Serena-V1 (Premium Narrative)", "Daniel (Deep Tech)", "Emma (Friendly)", "Cloned (My Custom Voice)")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Text to Speech Content", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Welcome home, master. All secure core database files are stored inside local SQLite protocols...") },
                    modifier = Modifier.fillMaxWidth().testTag("audio_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Speaker Voice Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    voiceOptions.forEach { v ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVoice = v }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedVoice == v, onClick = { selectedVoice = v })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(v, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Speech Playback Speed: %1.1f x".format(selectedSpeed), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Slider(
                    value = selectedSpeed,
                    onValueChange = { selectedSpeed = it },
                    valueRange = 0.5f..2.0f
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (promptInput.trim().isNotEmpty()) {
                        onGenerate(promptInput, selectedVoice, selectedSpeed)
                        promptInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_audio_generation"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "TTS Vocal")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Synthesize Text-to-Speech")
            }
        }

        item {
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Created Audio Audio Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (mediaList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SettingsVoice, contentDescription = "None", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No speech synthesized yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(mediaList) { audio ->
                AudioPreviewCard(
                    media = audio,
                    onToggleFav = { onToggleFav(audio.id, !audio.isFavorite) },
                    onDelete = { onDelete(audio.id) }
                )
            }
        }
    }
}

@Composable
fun AudioPreviewCard(
    media: GeneratedMediaEntity,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    Toast.makeText(context, "Synthesizing voice playback: ${media.voiceId}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.prompt,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Speaker: ${media.voiceId} • ${media.style}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Star",
                    tint = if (media.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
