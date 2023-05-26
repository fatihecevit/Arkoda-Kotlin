package com.example.arkoda

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*


class ActivityKonumKaydetme : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude:Double=0.0
    private var longitude:Double=0.0
    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konum_kaydetme)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissionsForOpen()
        checkLocationPermissionsForSave()
        val currentUser = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance().reference
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("konumlar").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(locationSnapshot: DataSnapshot) {
                        val latitude = locationSnapshot.child("latitude").value as? Double
                        val longitude = locationSnapshot.child("longitude").value as? Double
                        if (latitude != null && longitude != null) {
                            val location = LatLng(latitude, longitude)
                            val markerOptions = MarkerOptions().position(location).title(userId)
                            val marker = mMap.addMarker(markerOptions)!!
                            marker.tag = userId
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Hata durumunda yapılacak işlemleri burada tanımlayabilirsiniz
                    }
                })
        }

        val firebaseyeKaydetButton: Button = findViewById(R.id.firebaseyeKaydetButton)
        firebaseyeKaydetButton.setOnClickListener {
            saveLocationToFirebase()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermissionsForOpen() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener{ latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("My Position"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            longitude=latLng.longitude
            latitude=latLng.latitude
        }

    }

    private fun checkLocationPermissionsForSave() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun saveLocationToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val location = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
            )

            val locationRef = FirebaseDatabase.getInstance().getReference("konumlar")
                .child(userId)
            locationRef.setValue(location)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Konum Başarılı Bir Şekilde Kaydedildi.")
                    Toast.makeText(this, "Konum Kaydetme İşlemi başarı ile tamamlandı", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Konum Kaydedilemedi.", e)
                }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Konum izni verildi, ilgili işlemi gerçekleştirin
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        when {
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) -> {
                                // Kullanıcı izni reddetti, açıklama yapabilirsiniz
                                showPermissionRationaleDialog()
                            }
                            else -> {
                                // Kullanıcı izni kalıcı olarak reddetti, ayarlara yönlendirin
                                openAppSettings()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Konum izni reddedildi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konum İzni Gerekli")
            .setMessage("Uygulamanın konumunuzu kullanabilmesi için konum izni vermeniz gerekmektedir.")
            .setPositiveButton("İzin Ver") { _, _ ->
                // Konum izni istemek için yeniden kontrol yapın
                checkLocationPermissionsForOpen()
            }
            .setNegativeButton("Reddet") { _, _ ->
                Toast.makeText(this, "Konum izni reddedildi.", Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
