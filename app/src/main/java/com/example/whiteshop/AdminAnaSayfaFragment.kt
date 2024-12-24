package com.sadikgunay.whiteshop.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.sadikgunay.whiteshop.R
import com.sadikgunay.whiteshop.adapter.SiparisAdapter
import com.sadikgunay.whiteshop.dataclass.SiparisDataClass

class AdminAnaSayfaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var siparisAdapter: SiparisAdapter
    private val siparisList = mutableListOf<SiparisDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_ana_sayfa, container, false)

        setupViews(view)
        getSiparisler()

        return view
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.rvSiparisler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        siparisAdapter = SiparisAdapter(requireContext(), siparisList)
        recyclerView.adapter = siparisAdapter

        view.findViewById<Button>(R.id.btnTumSiparisleriOnayla).setOnClickListener {
            onaylaTumSiparisler()
        }
    }

    private fun onaylaTumSiparisler() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val siparislerRef = firebaseDatabase.reference.child("Siparisler")

        siparislerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key

                        for (siparisSnapshot in userSnapshot.children) {
                            val siparisId = siparisSnapshot.key
                            val siparisData = siparisSnapshot.getValue(SiparisDataClass::class.java)

                            val onayliSiparisRef = firebaseDatabase.reference
                                .child("OnayliSiparisler")
                                .child(userId ?: "")
                                .child(siparisId ?: "")

                            onayliSiparisRef.setValue(siparisData)
                        }

                        userSnapshot.ref.removeValue()
                    }
                    Toast.makeText(context, "Tüm siparişler onaylandı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getSiparisler() {
        val dbRef = FirebaseDatabase.getInstance().reference.child("Siparisler")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                siparisList.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key

                    for (siparisSnapshot in userSnapshot.children) {
                        val siparis = siparisSnapshot.getValue(SiparisDataClass::class.java)
                        siparis?.let {
                            it.userId = userId
                            it.siparisId = siparisSnapshot.key
                            siparisList.add(it)
                        }
                    }
                }
                siparisAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
    