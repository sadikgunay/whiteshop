package com.sadikgunay.whiteshop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.sadikgunay.whiteshop.databinding.FragmentAyarlarBinding
import kotlinx.coroutines.tasks.await


private val Nothing?.text: Any
    get() {
        TODO("Not yet implemented")
    }

class AyarlarFragment : Fragment() {

    private lateinit var binding: FragmentAyarlarBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAyarlarBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        binding.emailGuncelleButon.setOnClickListener {
            guncelleEmail()
        }
        binding.changePassButon.setOnClickListener {
            guncelleSifre()
        }

        return view
    }

    private fun guncelleEmail() {
        val newEmail = binding.editTextTextEmailAddress.text.toString()

        if (newEmail.isNotEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser

            // Kullanıcıyı yeniden kimlik doğrulamak gerekebilir
            val currentEmail = user?.email ?: ""
            val password = binding.editTextCurrentPassword.text.toString()

            if (password.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(currentEmail, password)
                user?.reauthenticate(credential)
                    ?.addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Kimlik doğrulama başarılı, e-posta güncellenebilir
                            user.updateEmail(newEmail)
                                .addOnCompleteListener { updateEmailTask ->
                                    if (updateEmailTask.isSuccessful) {
                                        val database = FirebaseDatabase.getInstance()
                                        val myRef = database.getReference("Kullanicilar")

                                        myRef.child(user.uid).child("email").setValue(newEmail)
                                            .addOnCompleteListener { databaseTask ->
                                                if (databaseTask.isSuccessful) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "E-posta başarıyla güncellendi.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Log.d(
                                                        "AyarlarFragment",
                                                        "Realtime Database hata: ${databaseTask.exception?.message}"
                                                    )
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "E-posta veritabanında güncellenemedi.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Log.d(
                                            "AyarlarFragment",
                                            "Firebase Auth hata: ${updateEmailTask.exception?.message}"
                                        )
                                        Toast.makeText(
                                            requireContext(),
                                            "E-posta güncelleme sırasında hata oluştu.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Log.d(
                                "AyarlarFragment",
                                "Kimlik doğrulama hata: ${reauthTask.exception?.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Kimlik doğrulama başarısız. Lütfen şifrenizi kontrol edin.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Şifrenizi girmeniz gerekiyor.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Lütfen yeni bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun guncelleSifre() {
        val mevcutSifre = binding.editTextCurrentPassword1.text.toString() // Mevcut şifre alanı
        val yeniSifre = binding.editTextTextPassword.text.toString() // Yeni şifre alanı

        if (mevcutSifre.isNotEmpty() && yeniSifre.isNotEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            val currentEmail = user?.email ?: ""

            // Kullanıcıyı yeniden kimlik doğrula
            val credential = EmailAuthProvider.getCredential(currentEmail, mevcutSifre)
            user?.reauthenticate(credential)
                ?.addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Şifre güncelleme işlemi
                        user.updatePassword(yeniSifre)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Şifre başarıyla güncellendi
                                    Toast.makeText(
                                        requireContext(),
                                        "Şifre başarıyla güncellendi.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Şifre güncellenirken hata oluştu
                                    Log.d(
                                        "AyarlarFragment",
                                        "Şifre güncelleme hata: ${updateTask.exception?.message}"
                                    )
                                    Toast.makeText(
                                        requireContext(),
                                        "Şifre güncellenirken hata oluştu.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // Yeniden kimlik doğrulama başarısız
                        Log.d(
                            "AyarlarFragment",
                            "Kimlik doğrulama hata: ${reauthTask.exception?.message}"
                        )
                        Toast.makeText(
                            requireContext(),
                            "Kimlik doğrulama başarısız. Mevcut şifrenizi kontrol edin.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // Gerekli alanların doldurulmadığı durumda kullanıcıyı bilgilendir
            Toast.makeText(
                requireContext(),
                "Lütfen mevcut ve yeni şifrenizi girin.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
