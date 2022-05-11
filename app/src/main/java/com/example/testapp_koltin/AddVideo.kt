package com.example.testapp_koltin

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testapp_koltin.databinding.ActivityAddVideoBinding


class AddVideo : AppCompatActivity() {
    lateinit var binding: ActivityAddVideoBinding
    lateinit var videoview : VideoView

    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101

    private val CAMRA_REQUEST_CODE = 102
    private lateinit var camerapermission: Array<String> //camera permission
    private var videoUri: Uri? = null
    private var tittle : String = ""
    private lateinit var progressbardilog : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Add Video"
        videoview = findViewById(R.id.viewvideo)

        camerapermission = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        progressbardilog = ProgressDialog(this)
        progressbardilog.setTitle("Uploading")
        progressbardilog.setMessage("Please wait...")
        progressbardilog.setCanceledOnTouchOutside(false)

        binding.uploadvideo.setOnClickListener {
            tittle =binding.videotittle.text.toString()
            if(TextUtils.isEmpty(tittle)){
                Toast.makeText(this,"Please enter video title", Toast.LENGTH_SHORT).show()
            }else if(videoUri == null){
                Toast.makeText(this,"Please select video First", Toast.LENGTH_SHORT).show()
            }
            else{
                uploadVideotoFirebase()
            }
        }

        binding.pickvideo.setOnClickListener {
            videoPiclDiloag()
        }

    }

    private fun uploadVideotoFirebase() {
        progressbardilog.show()

        val timestamp = ""+System.currentTimeMillis()
        val filePathAndname = "Videos/video_$timestamp"

        val videoStorage = FirebaseStorage.getInstance().getReference(filePathAndname)

        videoStorage.putFile(videoUri!!)
            .addOnSuccessListener {takesnapshot ->
                val uritask = takesnapshot.storage.downloadUrl
                while(!uritask.isSuccessful);
                val downloaduri = uritask.result
                if(uritask.isSuccessful){
                    val hashMap = HashMap<String,Any>()
                    hashMap["id"] = "$+timestamp"
                    hashMap["title"] ="$tittle"
                    hashMap["timestamp"] ="$timestamp"
                    hashMap["videouri"]="$downloaduri"

                    val ref = FirebaseDatabase.getInstance().getReference("videos")
                    ref.child("$timestamp").setValue(hashMap)
                        .addOnSuccessListener {
                            progressbardilog.dismiss()
                            Toast.makeText(this,"Video Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,MainActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            progressbardilog.dismiss()
                            Toast.makeText(this,"${e.message}", Toast.LENGTH_SHORT).show()

                        }

                }
            }

            .addOnFailureListener {e ->
                progressbardilog.dismiss()
                Toast.makeText(this,"${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setvideotovideoview() {
        val mediacontroller  = MediaController(this)
        mediacontroller.setAnchorView(videoview)

        videoview.setMediaController(mediacontroller)
        videoview.setVideoURI(videoUri)
        videoview.requestFocus()

        binding.viewvideo.setOnPreparedListener{
            videoview.pause()
        }

    }

    private fun videoPiclDiloag() {
        val options = arrayOf("Camera", "Gallery")

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Choose Video")
            .setItems(options) { dialogInterface, i ->
                if (i == 0) {
                    if (cheakcameraPermission()!!) {
                        requestcameraPermission()
                    } else {
                        videopickcamera()
                    }
                } else {
                    videopickgallary()
                }
            }.show()
    }

    private fun requestcameraPermission() {
        ActivityCompat.requestPermissions(this, camerapermission, CAMRA_REQUEST_CODE)
    }

    private fun cheakcameraPermission(): Boolean? {
        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == (PackageManager.PERMISSION_GRANTED)

        val result2 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)

        return result1 && result2
    }

    private fun videopickgallary() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun videopickcamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMRA_REQUEST_CODE -> {
                if (grantResults.size > 0) {

                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val StorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && StorageAccepted) {
                        videopickcamera()

                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data!!.data
                setvideotovideoview()
                AlertDialog.Builder(this).setTitle("Video Selected")
            }
            else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                videoUri = data!!.data
                setvideotovideoview()
                AlertDialog.Builder(this).setTitle("Video Selected")
            }
        }else{
            Toast.makeText(this, "Canclled", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}