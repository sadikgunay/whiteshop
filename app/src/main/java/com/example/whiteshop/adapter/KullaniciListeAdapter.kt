package com.sadikgunay.whiteshop.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sadikgunay.whiteshop.databinding.RvUserBinding
import com.sadikgunay.whiteshop.dataclass.KullaniciBilgi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class KullaniciAdapter(
    private val context: Context,
    private val kullaniciListesi: List<KullaniciBilgi>
) :
    RecyclerView.Adapter<KullaniciAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RvUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // ViewHolder içinde bir View referansı tanımlayın

        fun bind(kullanici: KullaniciBilgi) {
            binding.KullaniciEmail.text = kullanici.email





        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RvUserBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(kullaniciListesi[position])


    }

    override fun getItemCount(): Int {
        return kullaniciListesi.size
    }


}

