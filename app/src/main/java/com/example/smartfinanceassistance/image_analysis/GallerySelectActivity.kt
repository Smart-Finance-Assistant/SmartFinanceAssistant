package com.example.smartfinanceassistance.image_analysis

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.databinding.ActivityGallerySelectBinding

class GallerySelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGallerySelectBinding

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGallerySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                // 다음 단계로 전환: OCRProcessingFragment로 넘기기
                val intent = Intent(this, OCRProcessingActivity::class.java)
                intent.putExtra("image_uri", imageUri.toString())
                startActivity(intent)
            }
        }
    }
}