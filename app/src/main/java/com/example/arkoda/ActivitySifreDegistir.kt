package com.example.arkoda
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ActivitySifreDegistir : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private val storage = Firebase.storage
    private lateinit var storageRef: StorageReference
    private lateinit var et_current_password: EditText
    private lateinit var et_new_password: EditText
    private lateinit var et_new_password_confirm: EditText
    private lateinit var btn_change_password: Button
    var currentPassword=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sifre_degistir)

        database = FirebaseDatabase.getInstance()
        storageRef = storage.reference

        et_current_password = findViewById(R.id.et_current_password)
        et_new_password = findViewById(R.id.et_new_password)
        et_new_password_confirm = findViewById(R.id.et_new_password_confirm)
        btn_change_password = findViewById(R.id.btn_change_password)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    currentPassword = dataSnapshot.child("sifre").value as String
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }

        btn_change_password.setOnClickListener {
            if (currentUser != null) {

                val etcurrentPassword = et_current_password.text.toString()
                val newPassword = et_new_password.text.toString()
                val newPasswordConfirm = et_new_password_confirm.text.toString()

                if (etcurrentPassword.isEmpty()) {
                    et_current_password.error = "Lüften mevcut şifrenizi!"
                } else if (newPassword.isEmpty()) {
                    et_new_password.error = "Lüften yeni şifrenizi giriniz!"
                } else if (newPasswordConfirm.isEmpty()) {
                    et_new_password_confirm.error = "Lüften yeni şifrenizi tekrar giriniz!"
                }else{
                    // Mevcut şifre doğru girilmiş mi kontrol et
                    if (!etcurrentPassword.equals(currentPassword)) {
                        Toast.makeText(this, "Mevcut şifrenizi yanlış girdiniz!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Yeni şifreler aynı mı kontrol et
                    if (newPassword != newPasswordConfirm) {
                        Toast.makeText(this, "Yeni şifreler aynı değil!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val userId = currentUser.uid
                    val userRef = database.reference.child("users").child(userId)

                    currentUser!!.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //finish()
                                userRef.child("sifre").setValue(newPassword)
                                Toast.makeText(this, "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Şifre başarıyla güncellendi.")
                                val sıfırla = ""
                                val editable = Editable.Factory.getInstance().newEditable(sıfırla)
                                et_current_password.text= editable
                                et_new_password.text= editable
                                et_new_password_confirm.text= editable
                            } else {
                                Toast.makeText(this, "Şifre güncellenirken bir hata oluştu", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Şifre güncellenirken bir hata oluştu: ${task.exception}")
                            }
                        }
                }


            }
        }
    }

}