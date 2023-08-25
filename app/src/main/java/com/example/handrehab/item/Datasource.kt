package com.example.handrehab.item
import com.example.handrehab.R

class Datasource {

    fun loadItems(): List<Exercises> {
        return listOf<Exercises>(
            Exercises(1, "Hand Extension/Flexion", R.drawable.hand_screenshot, R.string.description_hand_extension_flexion, R.raw.hand_flexion_extension_video),
            Exercises(2, "Kleiner Finger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_little_finger_extension_flexion, R.raw.little_finger_extension_flexion),
            Exercises(3, "Mittelfinger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_middle_finger_extension_flexion, R.raw.middle_finger),
            Exercises(4, "Zeigefinger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_pointing_finger_extension_flexion, R.raw.pointing_finger),
            Exercises(5, "Daumen Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(6, "Neigung des Handgelenks", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(7, "Alle Finger schließen/öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(8, "Zeigefinger schließen/öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(9, "Mittelfinger schließen/öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(10, "Ringfinger schließen/öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(11, "Kleinen Finger schließen öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(12, "Daumen schließen/öffnen", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            Exercises(13, "Daumen Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),

            )
    }

}