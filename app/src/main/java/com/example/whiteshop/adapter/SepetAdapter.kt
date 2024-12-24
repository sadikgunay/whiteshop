package com.sadikgunay.whiteshop.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sadikgunay.whiteshop.R
import com.sadikgunay.whiteshop.SepetFragment
import com.sadikgunay.whiteshop.dataclass.UrunDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class SepetAdapter(private val context: Context, private val sepetList: List<UrunDataClass>) :
    RecyclerView.Adapter<SepetAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urunIsmi: TextView = itemView.findViewById(R.id.urunIsmi)
        val urunFiyat: TextView = itemView.findViewById(R.id.urunFiyat)
        val imgItem: ImageView = itemView.findViewById(R.id.imgItem)
        val btnArttir: Button = itemView.findViewById(R.id.btnArttir)
        val btnAzalt: Button = itemView.findViewById(R.id.btnAzalt)
        val txtMiktar: TextView = itemView.findViewById(R.id.txtMiktar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_sepet_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = sepetList[position]

        holder.urunIsmi.text = currentItem.urunIsim
        holder.urunFiyat.text = currentItem.urunFiyat.toString()
        holder.txtMiktar.text = currentItem.urunAdet.toString()
        Picasso.get().load(currentItem.newimageurl).into(holder.imgItem)

        holder.btnArttir.setOnClickListener {
            currentItem.urunAdet?.let { adet ->
                currentItem.urunId?.let { urunId ->
                    // Firebase'deki sepet "urunAdet" değerini güncelle
                    updateFirebaseSepetAdet(urunId, adet + 1)

                    // Adapter'deki mevcut ürünü güncelle
                    currentItem.urunAdet = adet + 1

                    // RecyclerView'e güncellenmiş veriyi yansıt
                    notifyItemChanged(holder.adapterPosition)

                }
            }
        }

        holder.btnAzalt.setOnClickListener {
            currentItem.urunAdet?.let { adet ->
                if (adet > 1) {
                    currentItem.urunId?.let { urunId ->
                        // Firebase'deki sepet "urunAdet" değerini azalt
                        updateFirebaseSepetAdet(urunId, adet - 1)

                        // Adapter'deki mevcut ürünü güncelle
                        currentItem.urunAdet = adet - 1

                        // RecyclerView'deki öğeyi güncelle
                        notifyItemChanged(holder.adapterPosition)

                    }
                } else {
                    Toast.makeText(context, "Adet 1'den az olamaz.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun getItemCount(): Int {
        return sepetList.size
    }

    fun updateFirebaseSepetAdet(urunId: String, yeniMiktar: Int) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.uid?.let { userId ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Kullanicilar")
                .child(userId)
                .child("Sepet")
                .child(urunId)

            // Sadece sepetin "urunAdet" değeri güncellenir
            databaseReference.child("urunAdet").setValue(yeniMiktar)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // SepetFragment'te toplam fiyatı güncellemek için
                        (context as? SepetFragment)?.verileriAl()
                    } else {
                        Toast.makeText(context, "Adet güncellenemedi.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun siparisVer() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.uid?.let { userId ->
            val sepetRef = FirebaseDatabase.getInstance().getReference("Kullanicilar")
                .child(userId)
                .child("Sepet")

            sepetRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (urunSnapshot in snapshot.children) {
                        val urunId = urunSnapshot.child("urunId").getValue(String::class.java)
                        val urunAdet = urunSnapshot.child("urunAdet").getValue(Int::class.java)

                        if (urunId != null && urunAdet != null) {
                            val urunlerRef = FirebaseDatabase.getInstance().getReference("Urunler")
                                .child(urunId)

                            // Stok miktarını güncelle
                            urunlerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val urunStok = snapshot.child("urunStok").getValue(Int::class.java)
                                    if (urunStok != null && urunStok >= urunAdet) {
                                        urunlerRef.child("urunStok").setValue(urunStok - urunAdet)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Hata durumu
                                }
                            })
                        }
                    }

                    // Sipariş verildikten sonra sepeti temizle
                    sepetRef.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Sipariş başarıyla tamamlandı!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Sipariş tamamlanırken bir hata oluştu!", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                }
            })
        }
    }
}

