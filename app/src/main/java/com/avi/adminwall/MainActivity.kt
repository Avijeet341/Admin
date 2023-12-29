package com.avi.adminwall
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avi.adminwall.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var chooseCategoryBinding: ActivityMainBinding

    // Use ActivityResultContracts to handle image selection
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the selected image
                val selectedImageUri = result.data?.data
                // Do something with the selected image URI, e.g., set it to the ImageView
                chooseCategoryBinding.userProfileImage.setImageURI(selectedImageUri)
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseCategoryBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = chooseCategoryBinding.root
        setContentView(view)

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
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
}
