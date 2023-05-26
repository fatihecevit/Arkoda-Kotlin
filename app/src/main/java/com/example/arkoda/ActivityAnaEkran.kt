package com.example.arkoda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mezunapp.FragmentProfilDuzenle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ActivityAnaEkran : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var Profile: FragmentProfilDuzenle
    private lateinit var Ogrenciler: FragmentListeOgrenci
    private lateinit var Talepler: FragmentListeTalepler
    private lateinit var TaleplerGonderilen: FragmentListeTaleplerGonderilen
    private lateinit var databasetalep: DatabaseReference
    private lateinit var database: FirebaseDatabase
    var durum=""
    var a=5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ana_ekran)

        database = FirebaseDatabase.getInstance()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        fragmentContainer = findViewById(R.id.fragment_container)

        Profile = FragmentProfilDuzenle()
        Ogrenciler=FragmentListeOgrenci()
        Talepler=FragmentListeTalepler()
        TaleplerGonderilen=FragmentListeTaleplerGonderilen()
        databasetalep = FirebaseDatabase.getInstance().getReference("talepler")
        databasetalep.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = FirebaseAuth.getInstance().uid
                var talepSayisi = 0

                for (userSnapshot in snapshot.children) {
                    val talepAlan = userSnapshot.child("talepAlan").value.toString()
                    val talepGorulme = userSnapshot.child("talepGorulme").value.toString()
                    if (talepGorulme == "Gorulmedi" && talepAlan == userId) {
                        talepSayisi++
                        userSnapshot.ref.child("talepGorulme").setValue("Goruldu")
                        break
                    }
                }
                if (talepSayisi > 0) {
                    if (!isFinishing) {
                        val alertDialogBuilder = AlertDialog.Builder(this@ActivityAnaEkran)
                        alertDialogBuilder.setTitle("Yeni Talep")
                        alertDialogBuilder.setMessage("Yeni talepleriniz bulunmaktadır.")
                        alertDialogBuilder.setPositiveButton("Tamam") { _, _ ->
                            // Buraya tıklama işlemini ekleyebilirsiniz, istediğiniz aktiviteye yönlendirebilirsiniz.
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }


                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isFinishing) {
                    val alertDialogBuilder = AlertDialog.Builder(this@ActivityAnaEkran)
                    alertDialogBuilder.setTitle("Hata")
                    alertDialogBuilder.setMessage("Veritabanından veriler okunamadı.")
                    alertDialogBuilder.setPositiveButton("Tamam") { _, _ ->
                        // Buraya tıklama işlemini ekleyebilirsiniz, istediğiniz aktiviteye yönlendirebilirsiniz.
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

            }
        })

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    durum = dataSnapshot.child("durum").value as String
                    if(durum=="Kalacak Ev/Oda Arıyor"){
                    }else if(durum=="Ev/Oda Arkadaşı Arıyor"){
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })

        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Profile).commit()

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Profile).commit()
                    true
                }
                R.id.menu_ogrenciler-> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Ogrenciler).commit()
                    true
                }
                R.id.menu_talepler-> {
                    val fragment = if (durum == "Kalacak Ev/Oda Arıyor") TaleplerGonderilen else Talepler
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
                    true
                }
                else -> false
            }
        }
    }
}