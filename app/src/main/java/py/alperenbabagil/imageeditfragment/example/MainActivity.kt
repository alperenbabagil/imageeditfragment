package py.alperenbabagil.imageeditfragment.example

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alperenbabagil.simpleanimationpopuplibrary.SapActivity
import com.alperenbabagil.simpleanimationpopuplibrary.showDefaultDialog
import com.alperenbabagil.simpleanimationpopuplibrary.showLoadingDialog
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import py.alperenbabagil.imageeditfragmentlib.fragment.fragment.ImageEditFragment
import py.alperenbabagil.imageeditfragmentlib.fragment.fragment.ImageEditFragment.DrawOnFragmentStatus
import py.alperenbabagil.imageeditfragmentlib.fragment.fragment.ImageEditFragment.SourceType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), DrawOnFragmentStatus,SapActivity {
    var drawedImagePath: String? = null
    var path=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + "dp.jpg"

        showLoadingDialog()

        //setting open edited image button click listener
        findViewById<View>(R.id.openImage).setOnClickListener {
            if (drawedImagePath == null) {
                Toast.makeText(this@MainActivity, "No edited image", Toast.LENGTH_SHORT).show()
            } else {
                //for demo app. In production, you must implement a file provider
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                        m.invoke(null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(Uri.parse("file://$drawedImagePath"), "image/*")
                startActivity(intent)
            }
        }
        openFragment.setOnClickListener { openImageEditFragment(path, SourceType.FILE_PATH) }
        openImageFromUrl.setOnClickListener { openImageEditFragment("https://picsum.photos/600/1200", SourceType.URL,true) }
        resetImage.setOnClickListener { //refreshing image
            showLoadingDialog()
            writeDrawableToDisk()
            currentDialog?.dismiss()
        }
        writeDrawableToDisk()
        currentDialog?.dismiss()
    }

    private fun openImageEditFragment(imagePath: String?,
                                      sourceType: SourceType,
                                      hideSaveBtn:Boolean =false
                                      ) {

        //hiding status bar and action bar to enter full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        try {
            supportActionBar!!.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // prevent buttons to be seen
        buttonLayout!!.visibility = View.GONE
        val bundle = Bundle()

        //setting data source type
        bundle.putSerializable(ImageEditFragment.SOURCE_TYPE_KEY, sourceType)
        //setting image path
        bundle.putString(ImageEditFragment.SOURCE_DATA_KEY, imagePath)

        bundle.putBoolean(ImageEditFragment.HIDE_SAVE_BTN_KEY,hideSaveBtn);

        //creating fragment
        val imageEditFragment = ImageEditFragment()

        //setting arguments
        imageEditFragment.arguments = bundle

        //putting fragment
        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, imageEditFragment).commit()
    }

    //putting an example image to external storage
    private fun writeDrawableToDisk(): Boolean {
        val bm = BitmapFactory.decodeResource(resources, R.drawable.dp)
        val imageFile = File(path)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            return true
        } catch (e: IOException) {
            Log.e("app", e.message)
            if (fos != null) {
                try {
                    fos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
        return false
    }


    override fun drawingCompleted(success: Boolean, path: String?) {
        drawedImagePath = path
        Toast.makeText(this, "Edited image saved succesfully", Toast.LENGTH_SHORT).show()
        removeFragment()
    }

    override fun drawingCancelled(path: String?) {
        removeFragment()
    }

    private fun removeFragment() {
        // exiting full screen
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        try {
            supportActionBar!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //showing buttons
        buttonLayout!!.visibility = View.VISIBLE

        //Here we are clearing back stack fragment entries
        val backStackEntry = supportFragmentManager.backStackEntryCount
        if (backStackEntry > 0) {
            for (i in 0 until backStackEntry) {
                supportFragmentManager.popBackStackImmediate()
            }
        }

        //Here we are removing all the fragment that are shown here
        if (supportFragmentManager.fragments.size > 0) {
            for (i in supportFragmentManager.fragments.indices) {
                val mFragment = supportFragmentManager.fragments[i]
                if (mFragment != null) {
                    supportFragmentManager.beginTransaction().remove(mFragment).commit()
                }
            }
        }
    }

    override var currentDialog: Dialog? = null
}