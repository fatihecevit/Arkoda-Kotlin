package com.example.mezunapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.arkoda.ActivityKonumKaydetme
import com.example.arkoda.ActivityMapOgrenci
import com.example.arkoda.ActivitySifreDegistir
import com.example.arkoda.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class FragmentProfilDuzenle : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databasek: DatabaseReference
    private val storage = Firebase.storage

    private lateinit var storageRef: StorageReference
    private lateinit var edit_Adsoyad: EditText
    private lateinit var edit_Bolum: EditText
    private lateinit var edit_Sınıf: Spinner
    private lateinit var edit_Durum: Spinner
    private lateinit var edit_Uzaklık: EditText
    private lateinit var edit_Sure: EditText
    private lateinit var edit_MailBilgi: EditText
    private lateinit var edit_TelnoBilgi: EditText

    private lateinit var edit_Button: Button
    private lateinit var konumAc: Button
    private lateinit var changePassword: Button
    private lateinit var mImageView: ImageView

    private lateinit var txt_Uzaklık: TextView
    private lateinit var txt_Sure: TextView

    private lateinit var edit_latitudebilgi: TextView
    private lateinit var edit_longitudebilgi: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profil_duzenle, container, false)

        database = FirebaseDatabase.getInstance()
        storageRef = storage.reference

        edit_Adsoyad = view.findViewById(R.id.edit_adsoyad)
        edit_Bolum = view.findViewById(R.id.edit_bolum)
        edit_Sınıf = view.findViewById(R.id.edit_spinner_sınıflar)
        edit_Durum = view.findViewById(R.id.edit_spinner_durum)
        edit_Uzaklık = view.findViewById(R.id.edit_uzaklik)
        edit_Sure = view.findViewById(R.id.edit_sure)
        edit_MailBilgi = view.findViewById(R.id.edit_mailbilgi)
        edit_TelnoBilgi = view.findViewById(R.id.edit_telnobilgi)

        edit_Button = view.findViewById(R.id.edit_button)
        konumAc= view.findViewById(R.id.konumAc)
        changePassword = view.findViewById(R.id.Password)
        mImageView = view.findViewById(R.id.editImage)

        txt_Uzaklık= view.findViewById(R.id.txtUzaklik)
        txt_Sure= view.findViewById(R.id.txtSure)

        edit_latitudebilgi= view.findViewById(R.id.edit_latitudebilgi)
        edit_longitudebilgi= view.findViewById(R.id.edit_longitudebilgi)


        edit_Uzaklık.visibility = View.VISIBLE
        txt_Uzaklık.visibility = View.VISIBLE
        txt_Sure.visibility = View.VISIBLE
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.reference.child("users").child(userId)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    edit_Adsoyad.setText(dataSnapshot.child("adSoyad").value as String?)
                    edit_Bolum.setText(dataSnapshot.child("bolum").value as String?)
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    databasek = FirebaseDatabase.getInstance().reference
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        databasek.child("konumlar").child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(locationSnapshot: DataSnapshot) {
                                    val latitude = locationSnapshot.child("latitude").value as? Double
                                    val longitude = locationSnapshot.child("longitude").value as? Double

                                    val decimalFormat = DecimalFormat("#.#####") // En fazla 5 basamaklı ondalık sayı
                                    edit_latitudebilgi.text = decimalFormat.format(latitude)
                                    edit_longitudebilgi.text = decimalFormat.format(longitude)

                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Hata durumunda yapılacak işlemleri burada tanımlayabilirsiniz
                                }
                            })
                    }
                    if (context != null) {
                        val spinnerSınıf= resources.getStringArray(R.array.sınıflar)
                        val PositionSınıf = spinnerSınıf.indexOf(dataSnapshot.child("sinif").value as String?)
                        edit_Sınıf.setSelection(PositionSınıf)
                        val spinnerDurum = resources.getStringArray(R.array.durum)
                        val PositionDurum = spinnerDurum.indexOf(dataSnapshot.child("durum").value as String?)
                        edit_Durum.setSelection(PositionDurum)

                        if (PositionDurum == spinnerDurum.indexOf("Aramıyor")) {
                            edit_Uzaklık.visibility = View.GONE
                            edit_Sure.visibility = View.GONE
                            txt_Uzaklık.visibility = View.GONE
                            txt_Sure.visibility = View.GONE
                        } else if(PositionDurum == spinnerDurum.indexOf("Ev/Oda Arkadaşı Arıyor")) {
                            edit_Uzaklık.visibility = View.VISIBLE
                            edit_Sure.visibility = View.VISIBLE
                            txt_Uzaklık.visibility = View.VISIBLE
                            txt_Sure.visibility = View.VISIBLE

                            edit_Uzaklık.hint = "Kampüse Olan Ev Uzaklığı (KM)"
                            edit_Sure.hint = "Evde Paylaşabileceği Süre (Ay)"
                            txt_Uzaklık.text = "Kampüse Olan Ev Uzaklığı (KM)"
                            txt_Sure.text = "Evde Paylaşabileceği Süre (Ay)"

                            edit_Uzaklık.setText(dataSnapshot.child("uzaklik").value?.toString())
                            edit_Sure.setText(dataSnapshot.child("sure").value?.toString())
                            if(edit_Sure.text.toString() =="0"){
                                edit_Sure.setText("")
                            }
                            if(edit_Uzaklık.text.toString() =="0"){
                                edit_Uzaklık.setText("")
                            }
                        }else if(PositionDurum == spinnerDurum.indexOf("Kalacak Ev/Oda Arıyor")) {
                            edit_Uzaklık.visibility = View.VISIBLE
                            edit_Sure.visibility = View.VISIBLE
                            txt_Uzaklık.visibility = View.VISIBLE
                            txt_Sure.visibility = View.VISIBLE

                            edit_Uzaklık.hint = "İstenen Maximum Uzaklık (KM)"
                            edit_Sure.hint = "Evde Kalacağı Süre (Ay)"
                            txt_Uzaklık.text = "Kampüse İstenen Ev Uzaklığı (KM)"
                            txt_Sure.text = "Evde Kalacağı Süre (Ay)"

                            edit_Uzaklık.setText(dataSnapshot.child("uzaklik").value?.toString())
                            edit_Sure.setText(dataSnapshot.child("sure").value?.toString())
                            if(edit_Sure.text.toString() =="0"){
                                edit_Sure.setText("")
                            }
                            if(edit_Uzaklık.text.toString() =="0"){
                                edit_Uzaklık.setText("")
                            }
                        }
                    }


                    edit_TelnoBilgi.setText(dataSnapshot.child("iletisimTelNo").value?.toString())
                    edit_MailBilgi.setText(dataSnapshot.child("iletisimMail").value as String?)
                    val imageUrl = dataSnapshot.child("imageUrl").value as String?


                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(mImageView)
                    }

                    if(edit_Bolum.text.toString() ==" "){
                        edit_Bolum.setText("")
                    }
                    if(edit_Sınıf.selectedItem.toString() ==" "){
                        edit_Bolum.setSelection(0)
                    }
                    if(edit_Durum.selectedItem.toString() ==" "){
                        edit_Bolum.setSelection(0)
                    }
                    if(edit_MailBilgi.text.toString() ==" "){
                        edit_MailBilgi.setText("")
                    }
                    if(edit_TelnoBilgi.text.toString() ==" "){
                        edit_TelnoBilgi.setText("")
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }
        edit_Durum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Seçili öğe değiştiğinde yapılacak işlemler burada yer alır.
                val selectedOption = parent?.getItemAtPosition(position).toString()
                if (selectedOption == "Aramıyor") {
                    edit_Uzaklık.visibility = View.GONE
                    edit_Sure.visibility = View.GONE
                    txt_Uzaklık.visibility = View.GONE
                    txt_Sure.visibility = View.GONE
                    edit_Sure.setText(" ")
                    edit_Uzaklık.setText("")
                } else if (selectedOption == "Ev/Oda Arkadaşı Arıyor") {
                    edit_Uzaklık.visibility = View.VISIBLE
                    edit_Sure.visibility = View.VISIBLE
                    txt_Uzaklık.visibility = View.VISIBLE
                    txt_Sure.visibility = View.VISIBLE

                    edit_Uzaklık.hint = "Kampüse Olan Ev Uzaklığı (KM)"
                    edit_Sure.hint = "Evde Paylaşabileceği Süre (Ay)"
                    txt_Uzaklık.text = "Kampüse Olan Ev Uzaklığı (KM)"
                    txt_Sure.text = "Evde Paylaşabileceği Süre (Ay)"

                } else if (selectedOption == "Kalacak Ev/Oda Arıyor") {
                    edit_Uzaklık.visibility = View.VISIBLE
                    edit_Sure.visibility = View.VISIBLE
                    txt_Uzaklık.visibility = View.VISIBLE
                    txt_Sure.visibility = View.VISIBLE

                    edit_Uzaklık.hint = "İstenen Maximum Uzaklık (KM)"
                    edit_Sure.hint = "Evde Kalacağı Süre (Ay)"
                    txt_Uzaklık.text = "Kampüse İstenen Ev Uzaklığı (KM)"
                    txt_Sure.text = "Evde Kalacağı Süre (Ay)"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir öğe seçilmediğinde yapılacak işlemler burada yer alır.
            }

        }
        konumAc.setOnClickListener {
            val intent = Intent(activity, ActivityKonumKaydetme::class.java)
            startActivity(intent)
        }

        edit_Button.setOnClickListener {
            if (currentUser != null) {
                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setMessage("Profilinizi güncellemek üzeresiniz. İşlemi onaylıyor musunuz?")
                alertDialogBuilder.setPositiveButton("Evet") { _, _ ->
                    val userId = currentUser.uid
                    val userRef = database.reference.child("users").child(userId)

                    edit_Adsoyad = view.findViewById(R.id.edit_adsoyad)
                    edit_Bolum = view.findViewById(R.id.edit_bolum)
                    edit_Sınıf = view.findViewById(R.id.edit_spinner_sınıflar)
                    edit_Durum = view.findViewById(R.id.edit_spinner_durum)
                    edit_Uzaklık = view.findViewById(R.id.edit_uzaklik)
                    edit_Sure = view.findViewById(R.id.edit_sure)
                    edit_MailBilgi = view.findViewById(R.id.edit_mailbilgi)
                    edit_TelnoBilgi = view.findViewById(R.id.edit_telnobilgi)

                    var adSoyad = edit_Adsoyad.text.toString()
                    var bolum = edit_Bolum.text.toString()
                    var sınıf = edit_Sınıf.selectedItem.toString()
                    var durum = edit_Durum.selectedItem.toString()
                    var Uzaklık = edit_Uzaklık.text.toString()
                    var sure = edit_Sure.text.toString()
                    var mailBilgi = edit_MailBilgi.text.toString()
                    var telnoBilgi = edit_TelnoBilgi.text.toString()

                    if (adSoyad.isEmpty()) {
                        edit_Adsoyad.error = "Ad Soyad Alanı Boş Bırakılamaz!"
                    } else if (bolum.isEmpty()) {
                        edit_Bolum.error = "Bölüm Alanı Boş Bırakılamaz!"
                    }else if (durum != "Aramıyor" && Uzaklık.isEmpty()) {
                        edit_Uzaklık.error = "Minimum Uzaklık Alanı Boş Bırakılamaz!"
                    }else if (durum != "Aramıyor" && sure.isEmpty()) {
                        edit_Sure.error = "Minimum Uzaklık Alanı Boş Bırakılamaz!"
                    }else {
                        if (durum == "Aramıyor") {
                            Uzaklık="0"
                            sure="0"
                        }
                        userRef.child("adSoyad").setValue(adSoyad)
                        userRef.child("bolum").setValue(bolum)
                        userRef.child("sinif").setValue(sınıf)
                        userRef.child("durum").setValue(durum)
                        userRef.child("uzaklik").setValue(Uzaklık)
                        userRef.child("sure").setValue(sure)
                        userRef.child("iletisimMail").setValue(mailBilgi)
                        userRef.child("iletisimTelNo").setValue(telnoBilgi)
                    }
                }
                alertDialogBuilder.setNegativeButton("Hayır") { _, _ ->
                    // Do nothing or show a message that the update was canceled
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
            if (currentUser != null) {

            }
        }
        changePassword.setOnClickListener {
            val intent = Intent(activity, ActivitySifreDegistir::class.java)
            startActivity(intent)
        }

        return view
    }
    override fun onResume() {
        super.onResume()
        // Veritabanından güncel verileri çekmek için onDataChange metodu içerisindeki kodları buraya taşıyabilirsin
        val currentUser = FirebaseAuth.getInstance().currentUser
        databasek = FirebaseDatabase.getInstance().reference
        if (currentUser != null) {
            val userId = currentUser.uid
            databasek.child("konumlar").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(locationSnapshot: DataSnapshot) {
                        val latitude = locationSnapshot.child("latitude").value as? Double
                        val longitude = locationSnapshot.child("longitude").value as? Double

                        val decimalFormat = DecimalFormat("#.#####") // En fazla 5 basamaklı ondalık sayı
                        edit_latitudebilgi.text = decimalFormat.format(latitude)
                        edit_longitudebilgi.text = decimalFormat.format(longitude)
                        /*edit_latitudebilgi.text = latitude.toString()
                        edit_longitudebilgi.text = longitude.toString()*/

                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Hata durumunda yapılacak işlemleri burada tanımlayabilirsiniz
                    }
                })
        }
    }

}
