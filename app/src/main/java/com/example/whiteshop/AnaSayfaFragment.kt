package com.sadikgunay.whiteshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sadikgunay.whiteshop.adapter.UrunAdapter
import com.sadikgunay.whiteshop.dataclass.UrunDataClass

class AnaSayfaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var urunAdapter: UrunAdapter
    private val urunList = mutableListOf<UrunDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ana_sayfa, container, false)

        setupRecyclerView(view)
        setupFirebaseListener()
        setupAdminButtons(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.urunListe)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        urunAdapter = UrunAdapter(requireContext(), urunList)
        recyclerView.adapter = urunAdapter
    }

    private fun setupFirebaseListener() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Urunler")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                urunList.clear()
                for (dataSnapshot in snapshot.children) {
                    dataSnapshot.getValue(UrunDataClass::class.java)?.let {
                        urunList.add(it)
                    }
                }
                urunAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnaSayfaFragment", "Veritabanı hatası: ${error.message}")
            }
        })
    }

    private fun setupAdminButtons(view: View) {
        val adminPanelButton: MaterialButton = view.findViewById(R.id.homeAdminPanel)
        val urunUploadButton: MaterialButton = view.findViewById(R.id.urunUpload)

        adminPanelButton.setOnClickListener {
            checkAdminAndPerformAction { navigateToAdminPanel() }
        }

        urunUploadButton.setOnClickListener {
            checkAdminAndPerformAction { navigateToUrunYukleme() }
        }


    }

    private fun checkAdminAndPerformAction(action: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let { currentUser ->
            val databaseReference = FirebaseDatabase.getInstance()
                .reference
                .child("Kullanicilar")
                .child(currentUser.uid)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() &&
                        snapshot.child("kullaniciTuru").getValue(String::class.java) == "Admin") {
                        action.invoke()
                    } else {
                        showAdminRequiredMessage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AnaSayfaFragment", "Veritabanı hatası: ${error.message}")
                }
            })
        }
    }

    private fun navigateToAdminPanel() {
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_anaSayfaFragment_to_adminAnaSayfaFragment)
    }

    private fun navigateToUrunYukleme() {
        val intent = Intent(context, UrunYuklemeActivity::class.java)
        context?.startActivity(intent)
    }

    private fun showAdminRequiredMessage() {
        Toast.makeText(
            requireContext(),
            "Bu işlemi gerçekleştirmek için yetkiniz yok.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
