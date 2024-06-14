package fr.ro.recipemanager.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import fr.ro.recipemanager.Accueil
import fr.ro.recipemanager.R

class AddRecipeFragment(private val context: Accueil): Fragment(){

    private var uploadedImage:ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =inflater?.inflate(R.layout.fragment_add_recipe,container,false)

        if (view != null) {
            uploadedImage=view.findViewById(R.id.preview_image)
        }


        val pickupImageButton = view?.findViewById<Button>(R.id.upload_button)

        if (pickupImageButton != null) {
            pickupImageButton.setOnClickListener{pickupImage()}
        }

        return view
    }

    private fun pickupImage() {
        val intent = Intent()
        intent.type ="image/"
        intent.action =Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),47)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==47 && resultCode == Activity.RESULT_OK){
            if(data==null || data.data == null) return

            val selectedImage = data.data


            uploadedImage?.setImageURI(selectedImage)

        }

    }
}