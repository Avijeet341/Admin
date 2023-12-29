package com.avi.adminwall

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avi.adminwall.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var chooseCategoryBinding: ActivityMainBinding

    private lateinit var firebaseInstance: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var division: String
    private lateinit var imageUri:String
    private  lateinit var storageRef:StorageReference

    private var options = FirebaseOptions.Builder()
        .setProjectId("infinity-walls")
        .setApplicationId("1:355548059361:android:7bf635a2d582f5cb32c9a0")
        .setApiKey("AIzaSyBdNm-G_5Xh3oZ-Q9ulUPjYD5sqBkB01zU")
        .setDatabaseUrl("https://infinity-walls-default-rtdb.firebaseio.com/")
        .setStorageBucket("infinity-walls.appspot.com")
        .build()

    // Use ActivityResultContracts to handle image selection
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the selected image
                val selectedImageUri = result.data?.data
                // Do something with the selected image URI, e.g., set it to the ImageView
                chooseCategoryBinding.userProfileImage.setImageURI(selectedImageUri)
                imageUri = selectedImageUri.toString()
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseCategoryBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = chooseCategoryBinding.root
        setContentView(view)
        // Initialize Firebase
        Firebase.initialize(this, options,"secondary")
        val second = Firebase.app("secondary")

        firebaseInstance = FirebaseDatabase.getInstance(second)
        databaseReference = firebaseInstance.getReference("Category")
        storageRef = FirebaseStorage.getInstance(second).reference.child(ConstantData.CATEGORY.category)

        // Set up the category list dropdown
        val categoryList = arrayOf(
            "Ai", "Abstract", "Amoled", "Anime", "Exclusive", "Games", "Minimal",
            "Nature", "Shapes", "Shows", "Sports", "Stock", "Superheroes"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        val autoCompleteTextView = chooseCategoryBinding.listOfCat
        autoCompleteTextView.setAdapter(adapter)

        // Set up onClickListener for the ImageView to choose an image
        chooseCategoryBinding.userProfileImage.setOnClickListener {
            pickImageFromGallery()
        }

        chooseCategoryBinding.buttonPush.setOnClickListener {
            division = autoCompleteTextView.text.toString()
            if (division == "Choose Category") {
                Toast.makeText(applicationContext, "Select category", Toast.LENGTH_SHORT).show()
            } else if (imageUri.isEmpty()) {
                Toast.makeText(applicationContext, "Select Image", Toast.LENGTH_SHORT).show()
            } else if (chooseCategoryBinding.EditName.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Set a name", Toast.LENGTH_SHORT).show()
            } else {
                // Create a reference to the image file in Firebase Storage
                val imageRef = storageRef.child(division).child(chooseCategoryBinding.EditName.text.toString()+System.currentTimeMillis())

                // Upload the file to Firebase Storage
                val uploadTask = imageRef.putFile(Uri.parse(imageUri))

                // Register observers to listen for when the download is done or if it fails
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // The uri variable now contains the download URL
                        val downloadUrl = task.result.toString()

                        // Now, you can save this download URL to the Realtime Database or perform any other necessary tasks
                        databaseReference.child(division).child(chooseCategoryBinding.EditName.text.toString())
                            .push()
                            .setValue(downloadUrl)
                            .addOnSuccessListener {
                                Log.d("Data", "$downloadUrl added successfully")
                                Toast.makeText(applicationContext, "Image added successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Data", "Error adding data: ${e.message}", e)
                                Toast.makeText(applicationContext, "Error adding data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Handle failures
                        Log.e("Firebase", "Error uploading image: ${task.exception?.message}", task.exception)
                        Toast.makeText(applicationContext, "Error uploading image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
}