package com.example.arkoda

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentListeOgrenci : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: AdapterOgrenci
    private lateinit var filtre_durum: Spinner
    private lateinit var txt_Uzaklık: TextView
    private lateinit var txt_Sure: TextView
    private lateinit var konumGoster: Button
    val itemOgrenciList = ArrayList<item_OgrenciEdit>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_liste_ogrenci, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        database = FirebaseDatabase.getInstance().getReference("users")
        adapter = AdapterOgrenci(requireContext())
        recyclerView.adapter = adapter
        val filtresec = view.findViewById<Button>(R.id.filtresec)
        val listele = view.findViewById<Button>(R.id.listele)
        val filtre_uzaklik = view.findViewById<TextView>(R.id.filtre_uzaklik)
        val filtre_sure = view.findViewById<TextView>(R.id.filtre_sure)
        val currentUser = FirebaseAuth.getInstance().currentUser

        filtre_durum = view.findViewById(R.id.filtre_spinner_durum)
        txt_Uzaklık= view.findViewById(R.id.txtUzaklik)
        txt_Sure= view.findViewById(R.id.txtSure)
        konumGoster= view.findViewById(R.id.konumGoster)

        val filtreLinearLayout = view.findViewById<LinearLayout>(R.id.filtreLinearLayout)

        filtreLinearLayout.visibility = View.GONE

        filtresec.setOnClickListener {
            if (filtreLinearLayout.visibility == View.GONE) {
                filtreLinearLayout.visibility = View.VISIBLE
            } else {
                filtreLinearLayout.visibility = View.GONE
            }
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemOgrenciList.clear()
                val userId = FirebaseAuth.getInstance().uid
                for (userSnapshot in snapshot.children) {

                    val adSoyad = userSnapshot.child("adSoyad").value.toString()
                    val mail = userSnapshot.child("mail").value.toString()
                    val sifre = userSnapshot.child("sifre").value.toString()
                    val imageUrl = userSnapshot.child("imageUrl").value.toString()
                    val bolum = userSnapshot.child("bolum").value.toString()
                    val sinif = userSnapshot.child("sinif").value.toString()
                    val durum = userSnapshot.child("durum").value.toString()
                    val uzaklik = userSnapshot.child("uzaklik").value.toString()
                    val sure = userSnapshot.child("sure").value.toString()
                    val iletisimMail = userSnapshot.child("iletisimMail").value.toString()
                    val iletisimTelNo = userSnapshot.child("iletisimTelNo").value.toString()
                    val uid = userSnapshot.child("uid").value.toString()
                    val durumSonuc = userSnapshot.child("durumSonuc").value.toString()

                    if (uid != userId) {
                        Log.v("uid",uid)
                        Log.v("userId",userId!!)
                        val itemOgrenci = item_OgrenciEdit(
                            adSoyad, mail, sifre, imageUrl, bolum,
                            sinif, durum,uzaklik, sure, iletisimMail, iletisimTelNo,uid,durumSonuc
                        )
                        itemOgrenciList.add(itemOgrenci)
                    }

                }

                adapter.setOgrenciList(itemOgrenciList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Veri alınırken hata oluştuğunda yapılacak işlemler
            }
        })


        filtre_durum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Seçili öğe değiştiğinde yapılacak işlemler burada yer alır.
                val selectedOption = parent?.getItemAtPosition(position).toString()
                if (selectedOption == "Herkesi Listele") {
                    filtre_uzaklik.visibility = View.GONE
                    filtre_sure.visibility = View.GONE
                    txt_Uzaklık.visibility = View.GONE
                    txt_Sure.visibility = View.GONE
                } else if (selectedOption == "Ev/Oda Sahiplerini Listele") {
                    filtre_uzaklik.visibility = View.VISIBLE
                    filtre_sure.visibility = View.VISIBLE
                    txt_Uzaklık.visibility = View.VISIBLE
                    txt_Sure.visibility = View.VISIBLE

                    filtre_uzaklik.hint = "Ev kampüse en fazla kaç km uzaklıkta olsun ?"
                    filtre_sure.hint = "En az kaç ay evi paylaşabilecek ev sahibi arıyorsunuz ?"

                    txt_Uzaklık.text = "Maximum Ev Uzaklığı (KM)"
                    txt_Sure.text = " Minimum Evde Misafir Etme Süresi (Ay)"

                } else if (selectedOption == "Ev/Oda Arayanları Listele") {
                    filtre_uzaklik.visibility = View.VISIBLE
                    filtre_sure.visibility = View.VISIBLE
                    txt_Uzaklık.visibility = View.VISIBLE
                    txt_Sure.visibility = View.VISIBLE

                    filtre_uzaklik.hint = "Evin uzaklığını maximum kaç km kabul edenler listelensin ?"
                    filtre_sure.hint = "Evde maximum kaç ay kalmayı düşünenler listelensin?"

                    txt_Uzaklık.text = "Maximum Ev Uzaklığı Talebi (KM)"
                    txt_Sure.text = "Maximum Evde Misafir Kalma Süresi  (Ay)"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir öğe seçilmediğinde yapılacak işlemler burada yer alır.
            }
        }

        konumGoster.setOnClickListener {

            val intent = Intent(requireContext(), ActivityMapOgrenci::class.java)
            intent.putExtra("ogrenciler", itemOgrenciList)
            startActivity(intent)

        }

        listele.setOnClickListener  {

            filtreLinearLayout.visibility = View.GONE

            var filtreUzaklik = filtre_uzaklik.text.toString().toDoubleOrNull()
            var filtre_sure=filtre_sure.text.toString().toDoubleOrNull()
            var filtre_durum = filtre_durum.selectedItem.toString()

            if(filtreUzaklik == null){
                filtreUzaklik=9999.0
            }
            if(filtre_sure == null){
                if(filtre_durum =="Ev/Oda Sahiplerini Listele"){
                    filtre_sure=0.0
                }
                else if(filtre_durum =="Ev/Oda Arayanları Listele"){
                    filtre_sure=9999.0
                }

            }

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemOgrenciList.clear();
                    val userId = FirebaseAuth.getInstance().uid
                    for (userSnapshot in snapshot.children) {

                        val adSoyad = userSnapshot.child("adSoyad").value.toString()
                        val mail = userSnapshot.child("mail").value.toString()
                        val sifre = userSnapshot.child("sifre").value.toString()
                        val imageUrl = userSnapshot.child("imageUrl").value.toString()
                        val bolum = userSnapshot.child("bolum").value.toString()
                        val sinif = userSnapshot.child("sinif").value.toString()
                        val durum = userSnapshot.child("durum").value.toString()
                        val uzaklik = userSnapshot.child("uzaklik").value.toString()
                        val sure = userSnapshot.child("sure").value.toString()
                        val iletisimMail = userSnapshot.child("iletisimMail").value.toString()
                        val iletisimTelNo = userSnapshot.child("iletisimTelNo").value.toString()
                        val uid = userSnapshot.child("uid").value.toString()
                        val durumSonuc = userSnapshot.child("durumSonuc").value.toString()


                        val uzakliks = userSnapshot.child("uzaklik").value.toString().toDouble()
                        val sures = userSnapshot.child("sure").value.toString().toDouble()

                        if(filtre_durum =="Herkesi Listele"){
                            if (uid != userId) {
                                val itemOgrenci = item_OgrenciEdit(
                                    adSoyad, mail, sifre, imageUrl, bolum,
                                    sinif, durum,uzaklik, sure, iletisimMail, iletisimTelNo,uid,durumSonuc
                                )
                                itemOgrenciList.add(itemOgrenci)
                            }
                        }
                        else if(filtre_durum =="Ev/Oda Sahiplerini Listele"){
                            if (filtre_sure != null) {
                                if(durum=="Ev/Oda Arkadaşı Arıyor" && uzakliks <= filtreUzaklik && filtre_sure <= sures  ) {
                                    if (uid != userId) {
                                        val itemOgrenci = item_OgrenciEdit(
                                            adSoyad, mail, sifre, imageUrl, bolum,
                                            sinif, durum,uzaklik, sure, iletisimMail, iletisimTelNo,uid,durumSonuc
                                        )
                                        itemOgrenciList.add(itemOgrenci)
                                    }

                                }
                            }
                        }
                        else if(filtre_durum =="Ev/Oda Arayanları Listele"){
                            if(durum=="Kalacak Ev/Oda Arıyor" && uzakliks < filtreUzaklik && sures < filtre_sure!!) {
                                if (uid != userId) {
                                    val itemOgrenci = item_OgrenciEdit(
                                        adSoyad, mail, sifre, imageUrl, bolum,
                                        sinif, durum,uzaklik, sure, iletisimMail, iletisimTelNo,uid,durumSonuc
                                    )
                                    itemOgrenciList.add(itemOgrenci)
                                }
                            }
                        }

                    }
                    adapter.setOgrenciList(itemOgrenciList)
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
        return view
    }

}
