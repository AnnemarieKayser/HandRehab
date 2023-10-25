package com.example.handrehab.item
import com.example.handrehab.R

class Datasource {

    fun loadItems(): List<Exercises> {
        return listOf<Exercises>(
            Exercises(1, "Alle Finger: Spreizen", R.drawable.hand_screenshot, R.string.description_hand_extension_flexion, R.raw.hand_flexion_extension_video),
            Exercises(2, "Kleiner Finger: Spreizen", R.drawable.hand_screenshot2, R.string.description_little_finger_extension_flexion, R.raw.little_finger_extension_flexion),
            Exercises(3, "Mittelfinger: Spreizen", R.drawable.hand_screenshot2, R.string.description_middle_finger_extension_flexion, R.raw.middle_finger),
            Exercises(4, "Zeigefinger: Spreizen", R.drawable.hand_screenshot2, R.string.description_pointing_finger_extension_flexion, R.raw.pointing_finger),
            Exercises(5, "Daumen: Spreizen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(6, "Neigung des Handgelenks", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(8, "Zeigefinger: Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(9, "Mittelfinger: Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(10, "Ringfinger: Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(11, "Kleinen Finger: Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(12, "Daumen zur Handinnenfläche bewegen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(13, "Alle Finger: SChließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(14, "Zeigefinger: halb Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(15, "Kleiner Finger: halb Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(16, "Mittelfinger: halb Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(17, "Ringfinger: halb Schließen und Öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),


            )
    }

}