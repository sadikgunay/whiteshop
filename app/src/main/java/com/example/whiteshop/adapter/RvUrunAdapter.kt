package com.sadikgunay.whiteshop.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sadikgunay.whiteshop.dataclass.UrunDataClass
import com.sadikgunay.whiteshop.UrunDetayActivity
import com.sadikgunay.whiteshop.databinding.RvItemBinding
import com.squareup.picasso.Picasso

// UrunAdapter.kt
class UrunAdapter(private val context: Context, private val urunList: List<UrunDataClass>) :
    RecyclerView.Adapter<UrunAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(urun: UrunDataClass) {
            binding.urunIsmi.text = urun.urunIsim
            binding.urunAciklama.text = urun.urunAciklama
            binding.urunFiyat.text = "${urun.urunFiyat} TL"
            binding.urunStok.text = "Stok: ${urun.urunStok}"

            // Resim yükleme
            if (!urun.newimageurl.isNullOrEmpty()) {
                Picasso.get()
                    .load(urun.newimageurl)
                    .into(binding.imgItem)
            }

            // Tıklama olayı
            binding.root.setOnClickListener {
                val intent = Intent(context, UrunDetayActivity::class.java).apply {
                    putExtra("urunId", urun.urunId)
                    putExtra("urunIsim", urun.urunIsim)
                    putExtra("urunAciklama", urun.urunAciklama)
                    putExtra("urunFiyat", urun.urunFiyat)
                    putExtra("urunStok", urun.urunStok)
                    putExtra("newimageurl", urun.newimageurl) // Resim URL'sini gönder
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val urun = urunList[position]
        holder.bind(urun)
    }

    override fun getItemCount(): Int = urunList.size
}

