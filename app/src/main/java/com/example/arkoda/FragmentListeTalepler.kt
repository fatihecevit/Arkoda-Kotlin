package com.example.arkoda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentListeTalepler : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var databasetalep: DatabaseReference
    private lateinit var adapter: AdapterTalepler
    val talepGonderenler = mutableListOf<String>()
    var itemOgrenciList = ArrayList<item_OgrenciEdit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_liste_talepler, container, false)

        databasetalep = FirebaseDatabase.getInstance().getReference("talepler")
        database = FirebaseDatabase.getInstance().getReference("users")

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = AdapterTalepler(requireContext())
        recyclerView.adapter = adapter

        fetchDataFromDatabase()

        return view
    }
    override fun onResume() {
        super.onResume()
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        val userId = FirebaseAuth.getInstance().uid
        databasetalep.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                talepGonderenler.clear()
                for (userSnapshot in snapshot.children) {
                    val talepGonderen = userSnapshot.child("talepGonderen").value.toString()
                    val talepAlan = userSnapshot.child("talepAlan").value.toString()
                    val talepDurum = userSnapshot.child("talepDurum").value.toString()

                    if (talepDurum=="Beklemede" && talepAlan == userId) {
                        talepGonderenler.add(talepGonderen)
                    }
                }
                fetchUserDetails()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Veritabanından veriler okunamadı.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserDetails() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemOgrenciList.clear()

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

                    if (uid in talepGonderenler) {
                        val itemOgrenci = item_OgrenciEdit(
                            adSoyad, mail, sifre, imageUrl, bolum,
                            sinif, durum,uzaklik, sure, iletisimMail, iletisimTelNo,uid
                        )
                        itemOgrenciList.add(itemOgrenci)
                    }
                }

                // RecyclerView'i güncelle
                adapter.setOgrenciList(itemOgrenciList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Veri alınırken hata oluştuğunda yapılacak işlemler
            }
        })
    }

}
