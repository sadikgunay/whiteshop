package com.sadikgunay.whiteshop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sadikgunay.whiteshop.databinding.ActivityUrunDetayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class UrunDetayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUrunDetayBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var urunId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrunDetayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Başlangıçta admin butonunu gizle
        binding.adminUrunSil.visibility = View.GONE

        // Intent'ten verileri al ve UI'ı ayarla
        setupUI()

        // Admin kontrolü yap
        checkAdminStatus()

        // Click listeners'ları ayarla
        setupClickListeners()
    }

    private fun setupUI() {
        intent.extras?.let { bundle ->
            urunId = bundle.getString("urunId", "")

            binding.apply {
                urunDetayIsim.text = bundle.getString("urunIsim", "")
                urunDetayAciklama.text = bundle.getString("urunAciklama", "")
                urunDetayFiyat.text = "${bundle.getInt("urunFiyat", 0)} TL"
                urunDetayStok.text = "Stok: ${bundle.getInt("urunStok", 0)}"

                // Resmi yükle
                val imageUrl = bundle.getString("newimageurl")
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(imageUrl).into(urunDetayImage)
                }
            }
        }
    }

    private fun checkAdminStatus() {
        auth.currentUser?.let { user ->
            database.getReference("Kullanicilar")
                .child(user.uid)
                .child("kullaniciTuru")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.getValue(String::class.java) == "Admin") {
                            binding.adminUrunSil.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@UrunDetayActivity,
                            "Yetki kontrolü yapılamadı",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            adminUrunSil.setOnClickListener {
                urunuSil()
            }

            sepeteEkleButon.setOnClickListener {
                urunuSepeteEkle()
            }

            anaSayfaDonButon.setOnClickListener {
                finish()
            }
        }
    }

    private fun urunuSepeteEkle() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val sepetRef = database.getReference("Kullanicilar/$uid/Sepet")
            val urunRef = database.getReference("Urunler/$urunId")

            urunRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Ürün bilgilerini al
                        val urunMap = hashMapOf<String, Any>(
                            "urunId" to urunId,
                            "urunIsim" to (intent.getStringExtra("urunIsim") ?: ""),
                            "urunFiyat" to (intent.getIntExtra("urunFiyat", 0)),
                            "urunAciklama" to (intent.getStringExtra("urunAciklama") ?: ""),
                            "newimageurl" to (intent.getStringExtra("newimageurl") ?: ""),
                            "urunStok" to (intent.getIntExtra("urunStok", 0)),
                            "urunAdet" to 1
                        )

                        // Sepete ekle
                        sepetRef.child(urunId).setValue(urunMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@UrunDetayActivity,
                                    "Ürün sepete eklendi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@UrunDetayActivity,
                                    "Ürün eklenirken hata oluştu: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@UrunDetayActivity,
                            "Ürün bulunamadı",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@UrunDetayActivity,
                        "İşlem iptal edildi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(
                this@UrunDetayActivity,
                "Lütfen önce giriş yapın",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun urunuSil() {
        if (urunId.isNotEmpty()) {
            auth.currentUser?.let { user ->
                database.getReference("Kullanicilar")
                    .child(user.uid)
                    .child("kullaniciTuru")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.getValue(String::class.java) == "Admin") {
                                database.getReference("Urunler")
                                    .child(urunId)
                                    .removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@UrunDetayActivity,
                                            "Ürün başarıyla silindi",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this@UrunDetayActivity,
                                            "Ürün silinirken hata oluştu",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@UrunDetayActivity,
                                "İşlem iptal edildi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }

    // ... diğer fonksiyonlar (urunuSepeteEkle vb.) aynı kalabilir ...
}


