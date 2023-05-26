package com.example.arkoda
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class item_OgrenciEdit(
    val adSoyad: String? = null,
    val mail: String? = null,
    val sifre: String? = null,
    var imageUrl: String? = null,
    val bolum: String? = null,
    val sinif: String? = null,
    val durum: String? = null,
    val uzaklik: String? = null,
    val sure: String? = null,
    val iletisimMail: String? = null,
    val iletisimTelNo: String? = null,
    val uid: String? = null,
    val durumSonuc: String? = null,
): Serializable {
    // ...
}
