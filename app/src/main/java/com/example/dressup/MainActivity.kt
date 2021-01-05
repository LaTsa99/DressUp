package com.example.dressup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.example.dressup.R.drawable.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST = 0
    private val GALLERY_PERMISSION_REQUEST = 1
    private val ANALYZE_PERMISSION_REQUEST = 2
    private var filePhoto: File? = null

    private var progressDialog: ProgressDialog? = null
    private var image_path : String = ""
    private var fromGallery : Boolean = false

    companion object {
        private const val IMAGE_CAPTURE_CODE = 0
        private const val IMAGE_PICK_CODE = 1;
        private var uri: Uri? = null
        private var state: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCamera.setOnClickListener {
            camera()
        }

        btnGallery.setOnClickListener {
            gallery()
        }

        btnAnalyze.setOnClickListener {
            analyze()
        }
    }

    private fun camera() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)){
                Toast.makeText(this, "I need this to take your picture", Toast.LENGTH_SHORT).show()
            }
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this, "I need this to save the image", Toast.LENGTH_SHORT).show()
            }

            val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST)
        }else{
            /*
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)*/
            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            filePhoto = getPhotoFile(Math.abs(Random.nextInt()).toString())
            uri = FileProvider.getUriForFile(this, "com.example.dressup.fileprovider", filePhoto!!)
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            Log.d("ITTEN_86", uri.toString())
            startActivityForResult(takePhotoIntent, IMAGE_CAPTURE_CODE)
        }
    }

    private fun getPhotoFile(fileName: String): File{
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage).apply {
            image_path = absolutePath
        }
    }

    private fun gallery() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "I need this to save the image", Toast.LENGTH_SHORT).show()
            }

            val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, GALLERY_PERMISSION_REQUEST)
        }else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            layResultText.visibility = View.INVISIBLE
            layResultIcon.visibility = View.INVISIBLE

            layStart.visibility = View.INVISIBLE
            Log.d("ITTEN_107", requestCode.toString())
            Log.d("ITTEN_108", data?.dataString ?: "nope")
            fromGallery = false
            if (requestCode == IMAGE_PICK_CODE) { //image_path = data?.data.toString()
                uri = data?.data
                fromGallery = true
            }
            Log.d("ITTEN_109", uri.toString())
            layAnalyze.visibility = View.VISIBLE
            image_view.setImageURI(uri)
            state = "Analyze"
        }else{

        super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun analyze() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "I need this to analyze the image", Toast.LENGTH_SHORT).show()
            }

            val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, ANALYZE_PERMISSION_REQUEST)
        } else {
            showProgressDialog()
            if (fromGallery)
                filePhoto = File(getPath(uri))
            Log.d("ITTEN_124", filePhoto?.isFile.toString())
            NetworkManager.getResult(filePhoto!!, this::onSuccess, this::onError)
            state = "Result"
        }

        //}
    }

    fun onSuccess(bikiniResult: BikiniResult){
        hideProgressDialog()
        val answer = bikiniResult.answer
        Log.d("ITTEN", answer)
        if (answer == "NotBikini") {
            approved()
        } else if (answer == "Bikini") {
            dressUp()
        }
    }

    fun onError(e: Throwable){
        hideProgressDialog()
        Log.d("ITTEN", e.toString())
        e.printStackTrace()
    }

    private fun approved() {
        imgResult.setImageResource(p)
        tvResult.text = "Appropriate"
        tvResult.setBackgroundColor(Color.GREEN)
        layResultText.visibility = View.VISIBLE
        layResultIcon.visibility = View.VISIBLE
    }

    private fun dressUp() {
        imgResult.setImageResource(x)
        tvResult.text = "Inappropriate"
        tvResult.setBackgroundColor(Color.RED)
        layResultText.visibility = View.VISIBLE
        layResultIcon.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (state == null) {
            super.onBackPressed()
        } else {
            if (state == "Result") {
                layResultIcon.visibility = View.INVISIBLE
                layResultText.visibility = View.INVISIBLE
            }
            layAnalyze.visibility = View.INVISIBLE
            layStart.visibility = View.VISIBLE
            state = null
        }
    }

    fun showProgressDialog() {
        if (progressDialog != null) {
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading...")
            show()
        }
    }

    protected fun hideProgressDialog() {
        progressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        progressDialog = null
    }

    fun getPath(uri: Uri?): String? {
        if(uri == null) return null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor == null){
            return null
        }

        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(columnIndex)
        cursor.close()
        return s
    }
}
