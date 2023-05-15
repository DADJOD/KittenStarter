package com.example.kittenstarter

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import com.squareup.picasso.Picasso;

class Detail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Расширим окно Activity
        // как только возможно
        /*
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        */window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        // Не хотим заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_detail)
        val image = findViewById<View>(R.id.detail) as ImageView

        // Получим Intent с помощью которого
        // запустили
        val intent = intent
        if (intent.hasExtra(MainActivity.IMAGE_URL)) {
            var url = intent.getStringExtra(MainActivity.IMAGE_URL)
            if (url != null) {
                // "Маленькая" картинка имеет в конце URL
                // _q.jpg  , "большая" картинка имеет
                // в конце _h.jpg
                url = url.replace("_q.jpg", "_h.jpg")
                Picasso.with(this)
                    .load(url)
                    .fit()
                    .centerCrop()
                    .into(image)
            }
        }
    }
}
