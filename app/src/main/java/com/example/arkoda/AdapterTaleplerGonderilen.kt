package com.example.arkoda
import android.content.Intent
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdapterTaleplerGonderilen (private val context: Context) :RecyclerView.Adapter<AdapterTaleplerGonderilen.OgrenciViewHolder>(){
    private var itemOgrenciList = ArrayList<item_OgrenciEdit>()
    private lateinit var database: FirebaseDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OgrenciViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_talepgonderilen, parent, false)
        return OgrenciViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OgrenciViewHolder, position: Int) {
        val ogrenci = itemOgrenciList[position]
        database = FirebaseDatabase.getInstance()

        holder.talepAdSoyad.text = ogrenci.adSoyad
        holder.talepbolum.text = ogrenci.bolum
        holder.talepsınıf.text = ogrenci.sinif
        holder.talepsure.text = ogrenci.sure
        if(ogrenci.bolum.toString()!= "hazırlık"){
            holder.talepsınıf.text = "${ogrenci.sinif}.sınıf öğrencisi"
        }else{
            holder.talepsınıf.text = ogrenci.sinif
        }
        holder.talepsure.text =  "En Fazla ${ogrenci.sure} Ay Evinde Kalabilirsin"


        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityOgrenciDetay::class.java)
            intent.putExtra("ogrenci", ogrenci)
            context.startActivity(intent)
        }

        val useridgonderen = FirebaseAuth.getInstance().uid
        val userIdalan=ogrenci.uid

        holder.talepsil.setOnClickListener  {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage("Talebi Silme İşlemini Onaylıyor musunuz?")
            alertDialogBuilder.setPositiveButton("Evet") { _, _ ->
                val databaseTalep = FirebaseDatabase.getInstance().getReference("talepler")
                databaseTalep.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (talepSnapshot in snapshot.children) {
                            val gonderen = talepSnapshot.child("talepGonderen").value.toString()
                            val alan = talepSnapshot.child("talepAlan").value.toString()
                            if (gonderen == useridgonderen && alan == userIdalan) {
                                databaseTalep.child(talepSnapshot.key.toString()).removeValue()
                                //holder.talepsil.visibility= View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Veri alınırken hata oluştuğunda yapılacak işlemler
                    }
                })
                itemOgrenciList.removeAt(position) // Talebi listeden kaldır
                setOgrenciList(itemOgrenciList)
            }

            alertDialogBuilder.setNegativeButton("Hayır") { _, _ ->
                // Do nothing or show a message that the sign-up was canceled
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return itemOgrenciList.size
    }

    fun setOgrenciList(itemMezunList: ArrayList<item_OgrenciEdit>) {
        this.itemOgrenciList = itemMezunList
        notifyDataSetChanged()
    }
    inner class OgrenciViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val talepAdSoyad: TextView = itemView.findViewById(R.id.talepAdSoyad)
        val talepbolum: TextView = itemView.findViewById(R.id.talepbolum)
        val talepsınıf: TextView = itemView.findViewById(R.id.talepsınıf)
        val talepsure: TextView = itemView.findViewById(R.id.talepsure)
        val talepsil: Button = itemView.findViewById(R.id.talepsil)
    }
}

