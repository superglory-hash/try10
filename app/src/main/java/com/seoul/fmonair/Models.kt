package com.seoul.fmonair

data class RadioStation(
    val id: String,
    val name: String,
    val frequency: String,
    val streamUrl: String,
    val isDynamic: Boolean = false,
    val dynamicType: String = "" // "KBS", "MBC", "SBS"
)

data class RecognizedSong(
    val title: String,
    val artist: String,
    val album: String = ""
)

data class SleepTimerOption(
    val label: String,
    val minutes: Int
)
