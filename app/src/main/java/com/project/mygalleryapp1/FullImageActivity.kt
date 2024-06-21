package com.project.mygalleryapp1

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
//import kotlinx.android.synthetic.main.activity_full_image.*

class FullImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)
        var imageViewFull = findViewById<ImageView>(R.id.imageViewFull)
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(imageViewFull)
        }
    }
}
