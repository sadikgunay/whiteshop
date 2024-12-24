package com.sadikgunay.whiteshop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sadikgunay.whiteshop.adapter.SepetAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sadikgunay.whiteshop.databinding.FragmentSepetBinding
import com.sadikgunay.whiteshop.dataclass.UrunDataClass
import java.text.NumberFormat
import java.util.Locale

class SepetFragment : Fragment() {
    private lateinit var binding: FragmentSepetBinding
    private lateinit var sepetAdapter: SepetAdapter
    private val sepetUrunList = mutableListOf<UrunDataClass>()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSepetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        verileriAl()
    }

    private fun setupRecyclerView() {
        sepetAdapter = SepetAdapter(requireContext(), sepetUrunList)
        binding.sepetList.apply {
            adapter = sepetAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnSepetiTemizle.setOnClickListener {
            sepetiTemizle()
        }

        binding.btnSiparisVer.setOnClickListener {
            siparisVer()
        }
    }

    fun verileriAl() {
        val kullaniciId = auth.currentUser?.uid
        if (kullaniciId != null) {
            val sepetRef = database.getReference("Kullanicilar/$kullaniciId/Sepet")
            sepetRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sepetUrunList.clear()
                    var toplamFiyat = 0

                    for (sepetSnapshot in snapshot.children) {
                        val urunIsim = sepetSnapshot.child("urunIsim").getValue(String::class.java)
                        val urunId = sepetSnapshot.child("urunId").getValue(String::class.java)
                        val urunAciklama = sepetSnapshot.child("urunAciklama").getValue(String::class.java)
                        val newimageurl = sepetSnapshot.child("newimageurl").getValue(String::class.java)
                        val urunStok = sepetSnapshot.child("urunStok").getValue(Int::class.java)
                        val urunAdet = sepetSnapshot.child("urunAdet").getValue(Int::class.java)
                        val urunFiyat = sepetSnapshot.child("urunFiyat").getValue(Int::class.java)

                        if (urunIsim != null && urunFiyat != null && urunAdet != null) {
                            val sepetUrun = UrunDataClass(
                                urunId,
                                urunIsim,
                                newimageurl ?: "",
                                urunAciklama,
                                urunFiyat,
                                urunStok,
                                urunAdet
                            )
                            sepetUrunList.add(sepetUrun)
                            toplamFiyat += urunFiyat * urunAdet
                        }
                    }
                    updateUI(toplamFiyat)
                    sepetAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Veri yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun siparisVer() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            val sepetRef = FirebaseDatabase.getInstance().reference
                .child("Kullanicilar")
                .child(uid)
                .child("Sepet")

            val siparislerRef = FirebaseDatabase.getInstance().reference
                .child("Siparisler")

            sepetRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Önce tüm ürünlerin stoklarını güncelle
                        snapshot.children.forEach { urunSnapshot ->
                            val urunId = urunSnapshot.child("urunId").getValue(String::class.java)
                            val siparisAdedi = urunSnapshot.child("urunAdet").getValue(Int::class.java)

                            if (urunId != null && siparisAdedi != null) {
                                val urunlerRef = FirebaseDatabase.getInstance().reference
                                    .child("Urunler")
                                    .child(urunId)

                                // Mevcut stok miktarını al ve güncelle
                                urunlerRef.child("urunStok").get().addOnSuccessListener { dataSnapshot ->
                                    val mevcutStok = dataSnapshot.getValue(Int::class.java) ?: 0
                                    val yeniStok = mevcutStok - siparisAdedi
                                    if (yeniStok >= 0) {
                                        urunlerRef.child("urunStok").setValue(yeniStok)
                                    }
                                }
                            }
                        }

                        // Sepet verilerini siparişlere ekle
                        siparislerRef.child(uid).setValue(snapshot.value) { databaseError, _ ->
                            if (databaseError == null) {
                                // Sepeti temizle
                                sepetRef.removeValue().addOnSuccessListener {
                                    sepetUrunList.clear()
                                    sepetAdapter.notifyDataSetChanged()
                                    Toast.makeText(context, "Sipariş verildi.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context,
                                    "Sipariş verilemedi: ${databaseError.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context,
                            "Sepetiniz boş. Önce ürün ekleyin.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,
                        "Veritabanı hatası: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            }
        }


    private fun sepetiTemizle() {
        val kullaniciId = auth.currentUser?.uid
        if (kullaniciId != null) {
            val sepetRef = database.getReference("Kullanicilar/$kullaniciId/Sepet")
            sepetRef.removeValue().addOnSuccessListener {
                sepetUrunList.clear()
                sepetAdapter.notifyDataSetChanged()
                updateUI(0)
                Toast.makeText(context, "Sepet temizlendi", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Sepet temizlenirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(toplamFiyat: Int) {
        val formattedFiyat = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(toplamFiyat)
        binding.toplamFiyat.text = formattedFiyat
        binding.btnSiparisVer.isEnabled = toplamFiyat > 0
        binding.btnSepetiTemizle.isEnabled = toplamFiyat > 0
        binding.btnSiparisVer.text = "Sipariş Ver ($formattedFiyat)"
    }
}




