package com.avi.adminwall

import android.app.Activity
import android.content.Intent
import android.media.Image
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
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

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the selected image
                val selectedImageUri = result.data?.data

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

        val categoryList = arrayOf(
            "Ai", "Abstract", "Amoled", "Anime", "Exclusive", "Games", "Minimal",
            "Nature", "Shapes", "Shows", "Sports", "Stock", "Superheroes"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        chooseCategoryBinding.listOfCat.setAdapter(adapter)

        chooseCategoryBinding.userProfileImage.setOnClickListener {
            pickImageFromGallery()
        }

        chooseCategoryBinding.buttonPush.setOnClickListener {
            division = chooseCategoryBinding.listOfCat.text.toString()
            if (division == "Choose Category") {
                Toast.makeText(applicationContext, "Select category", Toast.LENGTH_SHORT).show()
            } else if (imageUri.isEmpty()) {
                Toast.makeText(applicationContext, "Select Image", Toast.LENGTH_SHORT).show()
            } else if (chooseCategoryBinding.EditName.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Set a name", Toast.LENGTH_SHORT).show()
            } else {

                databaseReference.child(division).child(chooseCategoryBinding.EditName.text.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isPresent:Boolean = false
                            for(s in snapshot.children)
                            {
                                    val img:String = s.value.toString()
                                    if(img == imageUri)
                                    {
                                        isPresent = true

                                        break
                                    }
                            }
                            if (!isPresent) {
                                uploadImage()

                            } else {

                                Toast.makeText(
                                    applicationContext,
                                    "This image already uploaded in this category",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Database error: ${error.message}", error.toException())
                        }
                    })
            }
        }



    }

    private fun uploadImage() {
        val imageRef = storageRef.child(division)
            .child("${chooseCategoryBinding.EditName.text.toString()}${System.currentTimeMillis()}")

        val uploadTask = imageRef.putFile(Uri.parse(imageUri))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()

                val imgData = ImageUri(imageUri, downloadUrl)

                databaseReference.child(division)
                    .child(chooseCategoryBinding.EditName.text.toString())
                    .setValue(imgData)
                    .addOnSuccessListener {
                        Log.d("Data", "$downloadUrl added successfully")
                        Toast.makeText(
                            applicationContext,
                            "Image added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Data", "Error adding data: ${e.message}", e)
                        Toast.makeText(
                            applicationContext,
                            "Error adding data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Log.e("Firebase", "Error uploading image: ${task.exception?.message}", task.exception)
                Toast.makeText(
                    applicationContext,
                    "Error uploading image: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
}