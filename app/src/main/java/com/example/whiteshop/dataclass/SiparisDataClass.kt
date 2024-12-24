package com.sadikgunay.whiteshop.dataclass

data class SiparisDataClass(
    var userId: String? = null,
    var siparisId: String? = null,
    var urunIsim: String? = null,
    var urunFiyat: Double? = null,
    var urunAdet: Int? = null,
    var siparisTarihi: Long = System.currentTimeMillis()
) {
    constructor() : this(null, null, null, null, null)
}