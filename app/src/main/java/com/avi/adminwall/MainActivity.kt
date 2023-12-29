package com.avi.adminwall

import android.app.Activity
import android.content.Intent
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

class MainActivity : AppCompatActivity() {
    private lateinit var chooseCategoryBinding: ActivityMainBinding

    private lateinit var firebaseInstance: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var division: String
    private lateinit var imageUri:String

    private var options = FirebaseOptions.Builder()
        .setProjectId("infinity-walls")
        .setApplicationId("1:355548059361:android:7bf635a2d582f5cb32c9a0")
        .setApiKey("AIzaSyBdNm-G_5Xh3oZ-Q9ulUPjYD5sqBkB01zU")
        .setDatabaseUrl("https://infinity-walls-default-rtdb.firebaseio.com/")
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
            }
            else if(imageUri.isEmpty())
            {
                Toast.makeText(applicationContext, "Select Image", Toast.LENGTH_SHORT).show()
            }
            else if(chooseCategoryBinding.EditName.text.toString().isEmpty())
            {
                Toast.makeText(applicationContext,"Set a name",Toast.LENGTH_SHORT).show()
            }
            else {
                databaseReference.child(division).
                child(chooseCategoryBinding.EditName.text.toString())
                    .push().setValue(imageUri)
                    .addOnSuccessListener {
                        Log.d("Data",imageUri+" added successfully")
                    }

                Toast.makeText(applicationContext,"Select category",Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
}