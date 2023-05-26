package com.example.arkoda
import android.content.Intent
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AdapterOgrenci(private val context: Context) :RecyclerView.Adapter<AdapterOgrenci.OgrenciViewHolder>(){

    private var itemOgrenciList = ArrayList<item_OgrenciEdit>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OgrenciViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ogrenci, parent, false)
        return OgrenciViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OgrenciViewHolder, position: Int) {
        val ogrenci = itemOgrenciList[position]

        holder.adSoyadTextView.text = ogrenci.adSoyad
        holder.durumTextView.text = ogrenci.durum
        val imageUrl = ogrenci.imageUrl
        print(imageUrl)

        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.fotoImageView)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityOgrenciDetay::class.java)
            intent.putExtra("ogrenci", ogrenci)
            context.startActivity(intent)
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
        val adSoyadTextView: TextView = itemView.findViewById(R.id.adSoyadTextView)
        val durumTextView: TextView = itemView.findViewById(R.id.durumTextView)
        val fotoImageView: ImageView = itemView.findViewById(R.id.fotoImageView)
    }
}