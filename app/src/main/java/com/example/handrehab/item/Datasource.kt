package com.example.handrehab.item
import com.example.handrehab.R

class Datasource {

    fun loadItems(): List<Exercises> {
        return listOf<Exercises>(
            Exercises(1, "Spreizen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause, 0),
            Exercises(2, "Alle Finger: Spreizen", R.drawable.all_fingers_spread, R.string.description_all_fingers_spread, R.raw.all_fingers_spread),
            Exercises(3, "Kleiner Finger: Spreizen", R.drawable.little_finger_spread, R.string.description_little_finger_spread, R.raw.little_finger_spread_vid),
            Exercises(4, "Mittelfinger: Spreizen", R.drawable.middle_finger_spread, R.string.description_middle_finger_spread, R.raw.middle_finger_spread_vid),
            Exercises(5, "Zeigefinger: Spreizen", R.drawable.pointing_finger_spread, R.string.description_pointing_finger_spread, R.raw.pointing_finger_spread_vid),
            Exercises(6, "Daumen: Spreizen", R.drawable.thumb_spread, R.string.description_thumb_spread, R.raw.thumb_spread_vid),
            Exercises(7, "Öffnen und Schließen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause,0),
            Exercises(8, "Zeigefinger: Schließen und Öffnen", R.drawable.open_pointing_finger, R.string.description_pointing_finger_open_close, R.raw.open_pointing_finger_vid),
            Exercises(9, "Mittelfinger: Schließen und Öffnen", R.drawable.open_middle_finger, R.string.description_middle_finger_open_close, R.raw.middle_finger_open_vid),
            Exercises(10, "Ringfinger: Schließen und Öffnen", R.drawable.ring_finger_open, R.string.description_ring_finger_open_close, R.raw.ring_finger_open_vid),
            Exercises(11, "Kleinen Finger: Schließen und Öffnen", R.drawable.little_finger_open, R.string.description_little_finger_open_close, R.raw.little_finger_open_vid),
            Exercises(12, "Daumen zur Handinnenfläche bewegen", R.drawable.thumb_to_palm, R.string.description_thumb_to_palm, R.raw.thumb_to_palm_vid),
            Exercises(13, "Alle Finger: Schließen und Öffnen", R.drawable.all_fingers_open, R.string.description_all_finger_open_close, R.raw.open_close_all_fingers_vid),
            Exercises(14, "Halbes Öffnen und Schließen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause,0),
            Exercises(15, "Zeigefinger: halb Schließen und Öffnen", R.drawable.pointing_finger_half, R.string.description_pointing_finger_half_open_close, R.raw.pointing_finger_half_vid),
            Exercises(16, "Kleiner Finger: halb Schließen und Öffnen", R.drawable.little_finger_half, R.string.description_little_finger_half_open_close, R.raw.little_finger_half_vid),
            Exercises(17, "Mittelfinger: halb Schließen und Öffnen", R.drawable.middle_finger_half, R.string.description_middle_finger_half_open_close, R.raw.middle_finger_half_vid),
            Exercises(18, "Ringfinger: halb Schließen und Öffnen", R.drawable.ring_finger_half, R.string.description_ring_finger_half_open_close, R.raw.ring_finger_half_vid),
            Exercises(19, "Sonstige Übungen", R.drawable.ic_arrow, R.string.description_pause,0),
            Exercises(20, "Neigung des Handgelenks", R.drawable.tilt_hand_joint, R.string.description_tilt_hand_joint, R.raw.tilt_hand_joint_vid),


            )
    }

}