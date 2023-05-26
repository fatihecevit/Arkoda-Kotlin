package com.example.arkoda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mezunapp.FragmentProfilDuzenle
import com.google.firebase.auth.FirebaseAuth


class ActivityGiris : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var login_Mail: EditText
    private lateinit var login_Password: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giris)

        val login_Button = findViewById<Button>(R.id.login_button)
        val signupRedirectText = findViewById<TextView>(R.id.signupRedirectText)


        //val textView = findViewById<TextView>(R.id.signupRedirectText)
        val text = "Henüz Değil misin? Kayıt Ol"

        val spannableString = SpannableString(text)
        val color = ContextCompat.getColor(this, R.color.red)
        val startIndex = text.indexOf("Kayıt Ol")
        val endIndex = startIndex + "Kayıt Ol".length

        spannableString.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signupRedirectText.text = spannableString

        login_Mail = findViewById(R.id.login_email)
        login_Password = findViewById(R.id.login_password)

        login_Mail.setText("")
        login_Password.setText("")
        firebaseAuth = FirebaseAuth.getInstance()

        signupRedirectText.setOnClickListener  {
            val intent = Intent(this, ActivityKayitOl::class.java)
            startActivity(intent)
        }
        login_Mail.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_Q && event.isAltPressed) {
                login_Mail.text?.append("@")
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        login_Button.setOnClickListener  {

            val email = login_Mail.text.toString()
            val password =login_Password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val intent = Intent(this, ActivityAnaEkran::class.java)
                            startActivity(intent)
                            /*if (user!!.isEmailVerified) {
                                // Kullanıcı giriş yaptı, hesabı doğrulanmış
                                val intent = Intent(this, ActivityAnaEkran::class.java)
                                startActivity(intent)
                            } else {
                                // Kullanıcının hesabı doğrulanmadı, uyarı mesajı göster
                                Toast.makeText(this, "Hesabınız doğrulanmamış. Lütfen e-postanızı kontrol edin.", Toast.LENGTH_LONG).show()
                                firebaseAuth.signOut()
                            }*/
                        } else {
                            // Kullanıcı giriş yapamadı, hata mesajı göster
                            Toast.makeText(this, "Giriş yapılamadı: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }
}