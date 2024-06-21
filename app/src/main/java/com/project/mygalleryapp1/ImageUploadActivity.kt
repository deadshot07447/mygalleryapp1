package com.project.mygalleryapp1
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class ImageUploadActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var imageView: ImageView
    private lateinit var buttonChooseImage: Button
    private lateinit var buttonUploadImage: Button
    private lateinit var buttonShareImage: Button
    private lateinit var buttonHome: Button
    private lateinit var textViewStatus: TextView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        imageView = findViewById(R.id.imageView)
        buttonChooseImage = findViewById(R.id.buttonChooseImage)
        buttonUploadImage = findViewById(R.id.buttonUploadImage)
        buttonShareImage = findViewById(R.id.buttonShareImage)
        buttonHome = findViewById(R.id.buttonHome)
        textViewStatus = findViewById(R.id.textViewStatus)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationView)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener(this)

        updateNavHeader()

        buttonChooseImage.setOnClickListener { chooseImage() }
        buttonUploadImage.setOnClickListener { uploadImage() }
        buttonShareImage.setOnClickListener { shareImage() }
        buttonHome.setOnClickListener { viewUploadedImages() }
    }

    private fun updateNavHeader() {
        val headerView = navigationView.getHeaderView(0)
        val navUserName = headerView.findViewById<TextView>(R.id.textViewUserName)
        val navUserEmail = headerView.findViewById<TextView>(R.id.textViewEmail)

        navUserName.text = user?.displayName ?: "User"
        navUserEmail.text = user?.email ?: "user@example.com"
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref = storageReference.child("images/${user?.uid}/${System.currentTimeMillis()}")
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    textViewStatus.text = "Uploaded Successfully"
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        // Save or use the download URL as needed
                        Log.d(ContentValues.TAG, "File Location: $uri")
                    }
                }
                .addOnFailureListener { e ->
                    textViewStatus.text = "Upload Failed: " + e.message
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    textViewStatus.text = "Uploaded $progress%"
                }
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage() {
        if (filePath != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, filePath)
                type = "image/*"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun viewUploadedImages() {
        // Intent to navigate to an activity to display all uploaded images
        val intent = Intent(this, ViewUploadedImagesActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle home action
                viewUploadedImages()
            }
            R.id.nav_profile -> {
                // Handle profile action
                // Show user profile details
            }
            R.id.nav_total_images -> {
                // Handle total images uploaded action
                // Show total images uploaded by the user
            }
            R.id.nav_logout -> {
                // Handle logout action
                auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawers()
        return true
    }
}