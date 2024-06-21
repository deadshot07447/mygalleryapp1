//import com.project.mygalleryapp1
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.project.mygalleryapp1.ImageAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.mygalleryapp1.FullImageActivity
import com.project.mygalleryapp1.R
import com.project.mygalleryapp1.SignInActivity

class ImageUploadActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var fabUpload: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    private lateinit var imageAdapter: ImageAdapter
    private val imageUrls: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        fabUpload = findViewById(R.id.fabUpload)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        fabUpload.setOnClickListener {
            // Implement upload functionality here
            Toast.makeText(this, "Upload clicked", Toast.LENGTH_SHORT).show()
        }

        // Initialize RecyclerView
        imageAdapter = ImageAdapter(imageUrls) { imageUrl ->
            val intent = Intent(this, FullImageActivity::class.java)
            intent.putExtra("IMAGE_URL", imageUrl)
            startActivity(intent)
        }
        recyclerViewImages.adapter = imageAdapter

        loadImages()
    }

    private fun loadImages() {
        // Load images from Firebase and update the RecyclerView
        val user = auth.currentUser ?: return
        firestore.collection("images")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("url")
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl)
                    }
                }
                imageAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading images: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
