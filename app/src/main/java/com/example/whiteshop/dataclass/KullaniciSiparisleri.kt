package com.sadikgunay.whiteshop.dataclass
import com.sadikgunay.whiteshop.dataclass.SiparisDataClass
data class KullaniciSiparisleri(
    val userId: String,
    val siparisler: MutableList<SiparisDataClass> = mutableListOf()
)
