package com.example.handrehab.item

import androidx.annotation.DrawableRes

data class Exercises(
    val id: Int,
    val textItem: String,
    @DrawableRes val imageItem: Int,
    val descriptionItem: Int,
    val videoItem: Int,
    )
