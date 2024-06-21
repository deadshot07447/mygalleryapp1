package com.project.mygalleryapp1
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ViewUploadedImagesActivity : AppCompatActivity() {

    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var adapter: ImageAdapter
    private lateinit var storageReference: StorageReference

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_uploaded_images)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        storageReference = FirebaseStorage.getInstance().reference.child("images/${user?.uid}")

        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        recyclerViewImages.layoutManager = GridLayoutManager(this, 3)
        adapter = ImageAdapter(mutableListOf()) { imageUrl ->
            val intent = Intent(this, FullImageActivity::class.java)
            intent.putExtra("IMAGE_URL", imageUrl)
            startActivity(intent)
        }
        recyclerViewImages.adapter = adapter

        fetchUploadedImages()
    }

    private fun fetchUploadedImages() {
        storageReference.listAll().addOnSuccessListener { result ->
            val items = result.items
            val imageUrls = mutableListOf<String>()
            for (item in items) {
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                    if (imageUrls.size == items.size) {
                        adapter.updateImages(imageUrls)
                    }
                }
            }
        }.addOnFailureListener {
            // Handle any errors
        }
    }
}
