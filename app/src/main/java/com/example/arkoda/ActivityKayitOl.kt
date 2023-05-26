package com.example.arkoda

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityKayitOl : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signup_Adsoyad: EditText
    private lateinit var signup_Email: EditText
    private lateinit var signup_Password: EditText
    private lateinit var signup_Confirm: EditText
    private lateinit var signup_Button: Button
    private lateinit var openGalery: Button
    private lateinit var openCamera: Button
    private lateinit var mImageView: ImageView
    private lateinit var imageUri: Uri
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var requestPermission: ActivityResultLauncher<String>
    private lateinit var requestGalleryPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kayit_ol)

        signup_Adsoyad = findViewById(R.id.signup_adsoyad)
        signup_Email = findViewById(R.id.signup_email)
        signup_Password = findViewById(R.id.signup_password)
        signup_Confirm = findViewById(R.id.signup_confirm)
        signup_Button = findViewById(R.id.signup_button)
        openGalery = findViewById(R.id.openGalery)
        openCamera = findViewById(R.id.openCamera)
        mImageView = findViewById(R.id.uploadImage)

        dbRef = FirebaseDatabase.getInstance().getReference("users")
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
        signup_Email.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        openGalery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                requestGalleryPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        requestGalleryPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                selectImage()
            } else {
                Toast.makeText(this, "Galeri izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }



        signup_Button.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Kaydı tamamlamak istiyor musunuz?")
            alertDialogBuilder.setPositiveButton("Evet") { _, _ ->
                saveUser()
            }
            alertDialogBuilder.setNegativeButton("Hayır") { _, _ ->
                // Do nothing or show a message that the sign-up was canceled
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        openCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }

        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                mImageView.setImageURI(imageUri)
            }
        }

    }


    private fun openCamera() {
        val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(this, "com.example.arkoda.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        takePicture.launch(imageUri)
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        private var currentPhotoPath: String? = null
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                imageUri = data.data!!
                try {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, imageUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    mImageView.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        selectImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun uploadImage(userId: String, onSuccess: (imageUrl: String) -> Unit) {
        val fileName = userId + ".jpg" // .jpg uzantısını resim adına ekle
        val ref: StorageReference = storageReference.child("users/$fileName")
        ref.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { uri ->
                    // Handle successful upload and get download URL
                    val imageUrl = uri.toString() // resim url'sini al
                    onSuccess(imageUrl)
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
            }
    }
    private fun saveUser() {
        //getting values
        val Adsoyad = signup_Adsoyad.text.toString()
        val Email = signup_Email.text.toString()
        val Password = signup_Password.text.toString()
        val Confirm = signup_Confirm.text.toString()
        val imageUrl=" "
        val bolum = " "
        val sinif = " "
        val durum = " "
        val uzaklik= "0"
        val sure = "0"
        val iletisimMail = " "
        val iletisimTelNo = " "
        val durumSonuc="Beklemede"
        if (Adsoyad.isEmpty()) {
            signup_Adsoyad.error = "Lüften isim-soyisminizi giriniz!"
        } else if (Email.isEmpty()) {
            signup_Email.error = "Lüften mail hesabı giriniz!"
        } else if (Password.isEmpty()) {
            signup_Password.error = "Lüften şifre giriniz!"
        } else if (Confirm.isEmpty()) {
            signup_Confirm.error = "Lüften şifrenizi tekrar girin kısmını doldurun!"
        } else {
            if (Password == Confirm) {
                firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnSuccessListener(this) { authResult ->
                        val user = authResult.user
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        val users = item_OgrenciEdit(Adsoyad,Email, Password,imageUrl,bolum,sinif,durum,uzaklik,sure ,iletisimMail,iletisimTelNo,userId,durumSonuc)
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val location = hashMapOf(
                                        "latitude" to 0.0000000000001,
                                        "longitude" to 0.0000000000001
                                    )

                                    val locationRef = FirebaseDatabase.getInstance().getReference("konumlar")
                                        .child(userId) // Kullanıcı kimliğiyle alt düğüm oluşturuluyor
                                    locationRef.setValue(location)
                                        .addOnSuccessListener {
                                            Log.d(ContentValues.TAG, "Eşleşme isteği gönderildi.")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(ContentValues.TAG, "Eşleşme isteği gönderilemedi.", e)
                                        }
                                    Toast.makeText(this, "Doğrulama e-postası gönderildi. Lütfen e-postanızı kontrol edin.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Doğrulama e-postası gönderilirken bir hata oluştu.", Toast.LENGTH_LONG).show()
                                }
                            }
                        uploadImage(userId) { imageUrl ->
                            users.imageUrl = imageUrl // resim URL'sini kaydet
                            dbRef.child(userId).setValue(users).addOnCompleteListener {
                                val intent = Intent(this, ActivityGiris::class.java)
                                startActivity(intent)
                            }.addOnFailureListener { err ->
                                //
                            }
                        }
                        updateUI(user)
                    }
                    .addOnFailureListener(this) { exception ->
                        Toast.makeText(this, "Kayıt işlemi başarısız oldu: ${exception.message}", Toast.LENGTH_LONG).show()
                        updateUI(null)
                    }
            } else {
                Toast.makeText(this, "Password does not matched", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            Toast.makeText(this, "Kayıt işlemi başarıyla tamamlandı.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Kayıt işlemi tamamlandı, ancak hesabınızı doğrulamak için lütfen e-postanızı kontrol edin.", Toast.LENGTH_LONG).show()
        }
    }

}
