package com.example.arkoda

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class ActivityOgrenciDetay : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databasetalep: DatabaseReference
    private val storage = Firebase.storage
    private lateinit var storageRef: StorageReference

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ogrenci_detay)

        database = FirebaseDatabase.getInstance()
        databasetalep = FirebaseDatabase.getInstance().getReference("talepler")
        storageRef = storage.reference

        val ogrenci = intent.getSerializableExtra("ogrenci", item_OgrenciEdit::class.java)
        val ogrenciDetay = ogrenci as item_OgrenciEdit

        val image = findViewById<ImageView>(R.id.detayImage)
        val textViewAdSoyad = findViewById<TextView>(R.id.textViewAdSoyad)
        val textViewUid = findViewById<TextView>(R.id.textViewUid)
        val textViewBolum = findViewById<TextView>(R.id.textViewBolum)
        val textViewSinif = findViewById<TextView>(R.id.textViewSinif)
        val textViewDurum = findViewById<TextView>(R.id.textViewDurum)
        val textViewUzaklik = findViewById<TextView>(R.id.textViewUzaklik)
        val textViewSure = findViewById<TextView>(R.id.textViewSure)
        val tdurumSonuc = findViewById<TextView>(R.id.tdurumSonuc)
        val textViewIletisimMail = findViewById<TextView>(R.id.textViewIletisimMail)
        val textViewIletisimTelNo = findViewById<TextView>(R.id.textViewIletisimTelNo)
        val tuid = findViewById<TextView>(R.id.tuid)
        val sendmail = findViewById<Button>(R.id.sendmail)
        val sendtalep = findViewById<Button>(R.id.sendtalep)
        val sendwhatsapp = findViewById<Button>(R.id.sendwhatsapp)
        var url:String?
        var durumreal:String?

        tdurumSonuc.visibility = View.GONE
        textViewUid.visibility = View.GONE
        tuid.visibility = View.GONE
        sendtalep.visibility = View.GONE

        val realcurrentUser = FirebaseAuth.getInstance().currentUser
        if (realcurrentUser != null) {
            val userId = realcurrentUser.uid
            val userRef = database.reference.child("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    durumreal=dataSnapshot.child("durum").value as String?
                    if ((ogrenciDetay.durumSonuc == "Beklemede")&&(ogrenciDetay.durum == "Ev/Oda Arkadaşı Arıyor") && (durumreal == "Kalacak Ev/Oda Arıyor")) {
                        sendtalep.visibility = View.VISIBLE

                        databasetalep.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userId = FirebaseAuth.getInstance().uid

                                for (userSnapshot in snapshot.children) {
                                    val talepGonderen = userSnapshot.child("talepGonderen").value.toString()
                                    val talepAlan = userSnapshot.child("talepAlan").value.toString()
                                    val talepDurum = userSnapshot.child("talepDurum").value.toString()

                                    if (talepDurum=="Beklemede" && talepGonderen == userId && talepAlan==ogrenciDetay.uid) {
                                        sendtalep.text="TALEP GÖNDERİLDİ"
                                        sendtalep.isEnabled=false
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //Toast.makeText(ActivityOgrenciDetay, "Veritabanından veriler okunamadı.", Toast.LENGTH_SHORT).show()
                            }
                        })


                    }
                    if ((ogrenciDetay.durumSonuc == "Sonuçlandı")) {
                        tdurumSonuc.visibility = View.VISIBLE
                        sendmail.visibility = View.GONE
                        sendwhatsapp.visibility = View.GONE
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }

        // TextView'lara verileri yerleştirme
        ogrenciDetay?.let {

            textViewAdSoyad.text = it.adSoyad
            textViewUid.text=it.uid
            textViewBolum.text = it.bolum
            textViewSinif.text = it.sinif
            textViewDurum.text = it.durum
            textViewUzaklik.text=it.uzaklik
            textViewSure.text = it.sure
            textViewIletisimMail.text = it.iletisimMail
            textViewIletisimTelNo.text = it.iletisimTelNo
            url=it.imageUrl
            if (!url.isNullOrEmpty()) {
                Picasso.get().load(url).into(image)
            }
        }

        val recipientEmailAddress = textViewIletisimMail.text.toString()
        sendmail.setOnClickListener(){
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmailAddress))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Konu")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Mesaj")
            emailIntent.setPackage("com.google.android.gm")
            startActivity(Intent.createChooser(emailIntent, "E-posta uygulaması seçin"))
        }

        sendwhatsapp.setOnClickListener {
            val telno = "+9" + textViewIletisimTelNo.text.toString() // Telefon numarası
            val url = "https://api.whatsapp.com/send?phone=$telno"
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }


        sendtalep.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Talep gönderme işlemini onaylıyor musunuz?")
            alertDialogBuilder.setPositiveButton("Evet") { _, _ ->
                val talepGonderen = FirebaseAuth.getInstance().currentUser!!.uid
                val talepAlan= textViewUid.text.toString()
                sendMatchRequest(talepGonderen,talepAlan)
                Toast.makeText(this, "Talep Gönderme İşlemi başarı ile tamamlandı", Toast.LENGTH_SHORT).show()
                sendtalep.text="TALEP GÖNDERİLDİ"
                sendtalep.isEnabled=false
            }
            alertDialogBuilder.setNegativeButton("Hayır") { _, _ ->
                // Do nothing or show a message that the sign-up was canceled
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    }
    fun sendMatchRequest(talepGonderen: String, talepAlan: String) {

        val matchRequest = hashMapOf(
            "talepGonderen" to talepGonderen,
            "talepAlan" to talepAlan,
            "talepDurum" to "Beklemede",
            "talepGorulme" to "Gorulmedi"
        )
        val matchRequestsRef = FirebaseDatabase.getInstance().getReference("talepler")
        matchRequestsRef.push().setValue(matchRequest)
            .addOnSuccessListener {
                Log.d(TAG, "Eşleşme isteği gönderildi.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Eşleşme isteği gönderilemedi.", e)
            }
    }
}