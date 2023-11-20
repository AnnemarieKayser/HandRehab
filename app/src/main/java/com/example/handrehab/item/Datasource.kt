package com.example.handrehab.item
import com.example.handrehab.R

class Datasource {

    fun loadItems(): List<Exercises> {
        return listOf<Exercises>(
            Exercises(1, "Spreizen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause,  false),
            Exercises(2, "Alle Finger: Spreizen", R.drawable.all_fingers_spread, R.string.description_all_fingers_spread,  false),
            Exercises(3, "Kleiner Finger: Spreizen", R.drawable.little_finger_spread, R.string.description_little_finger_spread, false),
            Exercises(4, "Mittelfinger: Spreizen", R.drawable.middle_finger_spread, R.string.description_middle_finger_spread, false),
            Exercises(5, "Zeigefinger: Spreizen", R.drawable.pointing_finger_spread, R.string.description_pointing_finger_spread,  false),
            Exercises(6, "Daumen: Spreizen", R.drawable.thumb_spread, R.string.description_thumb_spread, false),
            Exercises(7, "Öffnen und Schließen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause, false),
            Exercises(8, "Zeigefinger: Schließen und Öffnen", R.drawable.open_pointing_finger, R.string.description_pointing_finger_open_close, false),
            Exercises(9, "Mittelfinger: Schließen und Öffnen", R.drawable.open_middle_finger, R.string.description_middle_finger_open_close,  false),
            Exercises(10, "Ringfinger: Schließen und Öffnen", R.drawable.ring_finger_open, R.string.description_ring_finger_open_close,  false),
            Exercises(11, "Kleinen Finger: Schließen und Öffnen", R.drawable.little_finger_open, R.string.description_little_finger_open_close,  false),
            Exercises(12, "Daumen zur Handinnenfläche", R.drawable.thumb_to_palm, R.string.description_thumb_to_palm, false),
            Exercises(13, "Alle Finger: Schließen und Öffnen", R.drawable.all_fingers_open, R.string.description_all_finger_open_close,  false),
            Exercises(14, "Halbes Öffnen und Schließen der einzelnen Finger", R.drawable.ic_arrow, R.string.description_pause,false),
            Exercises(15, "Zeigefinger: halb Schließen und Öffnen", R.drawable.pointing_finger_half, R.string.description_pointing_finger_half_open_close,  false),
            Exercises(16, "Kleiner Finger: halb Schließen und Öffnen", R.drawable.little_finger_half, R.string.description_little_finger_half_open_close, false),
            Exercises(17, "Mittelfinger: halb Schließen und Öffnen", R.drawable.middle_finger_half, R.string.description_middle_finger_half_open_close,  false),
            Exercises(18, "Ringfinger: halb Schließen und Öffnen", R.drawable.ring_finger_half, R.string.description_ring_finger_half_open_close,  false),
            Exercises(19, "Sonstige Übungen", R.drawable.ic_arrow, R.string.description_pause, false),
            Exercises(20, "Neigung des Handgelenks", R.drawable.tilt_hand_joint, R.string.description_tilt_hand_joint, false),


            )
    }

    fun getVideo(id: Int): Int {

        var video = 0

        when(id){

            2 -> video = R.raw.v2
            3 -> video = R.raw.v3
            4-> video = R.raw.v4
            5 -> video = R.raw.v5
            6 -> video = R.raw.v6
            8 -> video = R.raw.v8
            9 -> video = R.raw.v9
            10 -> video = R.raw.v10
            11-> video = R.raw.v11
            12 -> video =R.raw.v12
            13 -> video =R.raw.v13
            15 -> video =R.raw.v15
            16 -> video =R.raw.v16
            17 -> video =R.raw.v17
            18 -> video =R.raw.v18
            20 -> video = R.raw.v20
            else -> video = 0

        }

        return video
    }

}