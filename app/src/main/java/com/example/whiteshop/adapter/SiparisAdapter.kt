package com.sadikgunay.whiteshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sadikgunay.whiteshop.R
import com.sadikgunay.whiteshop.dataclass.SiparisDataClass
import com.google.firebase.database.FirebaseDatabase

class SiparisAdapter(private val context: Context, private val siparisList: List<SiparisDataClass>) :
    RecyclerView.Adapter<SiparisAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val onaylaButon: Button = itemView.findViewById(R.id.onaylaButon)
        val txtUrunAdi: TextView = itemView.findViewById(R.id.txtUrunAdi)
        val txtUrunFiyati: TextView = itemView.findViewById(R.id.txtUrunFiyati)
        val txtAdet: TextView = itemView.findViewById(R.id.txtAdet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_siparis_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = siparisList[position]

        holder.txtUrunAdi.text = currentItem.urunIsim
        holder.txtUrunFiyati.text = "${currentItem.urunFiyat?.toString()} TL"
        holder.txtAdet.text = "Adet: ${currentItem.urunAdet?.toString()}"

        holder.onaylaButon.setOnClickListener {
            onaylaTekSiparis(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return siparisList.size
    }

    private fun onaylaTekSiparis(siparis: SiparisDataClass) {
        val firebaseDatabase = FirebaseDatabase.getInstance()

        val siparisRef = firebaseDatabase.reference
            .child("Siparisler")
            .child(siparis.userId ?: "")
            .child(siparis.siparisId ?: "")

        val onayliSiparisRef = firebaseDatabase.reference
            .child("OnayliSiparisler")
            .child(siparis.userId ?: "")
            .child(siparis.siparisId ?: "")

        onayliSiparisRef.setValue(siparis).addOnSuccessListener {
            siparisRef.removeValue().addOnSuccessListener {
                Toast.makeText(context, "Sipariş onaylandı", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}