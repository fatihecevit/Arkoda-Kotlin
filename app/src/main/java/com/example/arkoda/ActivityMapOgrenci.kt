package com.example.arkoda

import android.content.Intent
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class ActivityMapOgrenci : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private var userDataList: ArrayList<item_OgrenciEdit> =ArrayList<item_OgrenciEdit>()

    private var allMarkers: MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_ogrenci)

        val receivedIntent = intent
        val extras = receivedIntent.extras
        if (extras != null) {
            val res = extras.getSerializable("ogrenciler") as? ArrayList<item_OgrenciEdit>
            if(res!=null){
                Log.v("RES","not null")
                userDataList=res as ArrayList<item_OgrenciEdit>
            }else{
                Log.v("RES","null")
            }
        }

        if(userDataList!=null){
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
            mapFragment.getMapAsync(this@ActivityMapOgrenci)
        }

        database = FirebaseDatabase.getInstance().reference


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.v("OGR2",userDataList.size.toString())
        // Kullanıcının konum verilerini alın
        for (userData in userDataList) {
            database.child("konumlar").child(userData.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(locationSnapshot: DataSnapshot) {
                        val latitude = locationSnapshot.child("latitude").value as? Double
                        val longitude = locationSnapshot.child("longitude").value as? Double
                        Log.v("AdSoyad",userData.adSoyad.toString())
                        Log.v("uid",userData.uid.toString())
                        Log.v("latitude",latitude.toString())
                        Log.v("longitude",longitude.toString())
                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)
                            val markerOptions = MarkerOptions().position(location).title(userData.adSoyad)
                            val marker = mMap.addMarker(markerOptions)!!
                            marker.tag = userData

                            allMarkers.add(marker)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Hata durumunda yapılacak işlemleri burada tanımlayabilirsiniz
                    }
                })
        }

        mMap.setOnMapLongClickListener { latLng ->
            // Tıklanan konumu kontrol edin ve uygun markerı bulun
            for (marker in allMarkers) {
                val position = marker.position
                if (position.latitude == latLng.latitude && position.longitude == latLng.longitude) {
                    val userData = marker.tag as? item_OgrenciEdit
                    if (userData != null) {
                        showUserInfoDialog(userData.adSoyad, userData.bolum)
                    }
                    break
                }
            }
        }

        mMap.setOnMarkerClickListener { clickedMarker ->
            val userData = clickedMarker.tag as? item_OgrenciEdit
            /*  if (userData != null) {
                  showUserInfoDialog(userData.adSoyad, userData.bolum)
              }*/
            val intent = Intent(this@ActivityMapOgrenci, ActivityOgrenciDetay::class.java)
            intent.putExtra("ogrenci", userData)
            this@ActivityMapOgrenci.startActivity(intent)
            true
        }

    }


    private fun showUserInfoDialog(name: String?, bolum: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Kullanıcı Bilgileri")
        dialogBuilder.setMessage("Ad: $name\nBölüm: $bolum")
        dialogBuilder.setPositiveButton("Tamam") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
