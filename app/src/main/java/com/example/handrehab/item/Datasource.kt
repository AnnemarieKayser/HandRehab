package com.example.handrehab.item
import com.example.handrehab.R

class Datasource {

    fun loadItems(): List<Exercises> {
        return listOf<Exercises>(
            Exercises("Hand Extension/Flexion", R.drawable.hand_screenshot, R.string.description_hand_extension_flexion, R.raw.hand_flexion_extension_video),
            Exercises("Kleiner Finger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_little_finger_extension_flexion, R.raw.little_finger_extension_flexion),
            Exercises("Mittelfinger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_middle_finger_extension_flexion, R.raw.middle_finger),
            Exercises("Zeigefinger Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_pointing_finger_extension_flexion, R.raw.pointing_finger),
            Exercises("Daumen Extension/Flexion", R.drawable.hand_screenshot2, R.string.description_thumb_extension_flexion, R.raw.thumb_extension_flexion),
            )
    }

}