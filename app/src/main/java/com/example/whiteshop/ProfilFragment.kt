package com.sadikgunay.whiteshop

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sadikgunay.whiteshop.GirisActivity
import com.sadikgunay.whiteshop.R

class ProfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Kullanıcıyı ve veritabanı referansını başlat
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

        // Fragment'in layout dosyasını inflate et
        val view = inflater.inflate(R.layout.fragment_profil, container, false)

        // Kullanıcı ad ve soyadını çek ve textView'lara set et
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val adSoyad = snapshot.child("ad ve soyad").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    val textViewAdSoyad: TextView = view.findViewById(R.id.adSoyadTextView)
                    val textViewEmail: TextView = view.findViewById(R.id.emailBilgiTextView)

                    textViewAdSoyad.text = adSoyad
                    textViewEmail.text = email
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfilFragment", "Veri okuma işlemi iptal edildi.", error.toException())
            }
        })

        // AdminPanelView simgesine tıklanma işlemi
        val adminPanelView: ImageView = view.findViewById(R.id.adminPanelView)
        adminPanelView.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null) {
                val databaseReference =
                    FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val kullaniciTuru =
                                snapshot.child("kullaniciTuru").getValue(String::class.java)

                            if (kullaniciTuru == "Admin") {
                                val navController =
                                    NavHostFragment.findNavController(this@ProfilFragment)
                                navController.navigate(R.id.action_profilFragment_to_kullaniciListeleme)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Bu işlemi gerçekleştirmek için yetkiniz yok.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Veritabanı hatası durumunda
                        Log.d("ProfilFragment", "Veritabanı hatası: ${error.message}")
                    }
                })
            }
        }


        // Exit butonunu bul ve click listener ekle
        val exitButon: Button = view.findViewById(R.id.exitButon)
        exitButon.setOnClickListener {
            exit()
        }

        // Diğer butonu bul ve click listener ekle
        val changeMail: Button = view.findViewById(R.id.email_guncelle)
        changeMail.setOnClickListener {
            ayarlarSayfaGec()
        }
        val changePass: Button = view.findViewById(R.id.sifreDegistir)
        changePass.setOnClickListener {
            ayarlarSayfaGec()
        }
        val hesapSilButon: Button = view.findViewById(R.id.hesapSilButon)
        hesapSilButon.setOnClickListener {
            hesapSil()
        }

        return view
    }

    private fun exit() {
        Log.d("ProfilFragment", "Exit fonksiyonu çağrıldı.")
        auth.signOut()
        val intent = Intent(requireContext(), GirisActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun ayarlarSayfaGec() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.action_profilFragment_to_ayarlarFragment)
    }

    private fun kullaniciListelemeSayfaGec() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.action_profilFragment_to_kullaniciListeleme)
    }

    private fun hesapSil() {
        // Kullanıcıdan yeniden kimlik doğrulaması iste
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Şifrenizi Girin")
            .setMessage("Güvenlik için lütfen şifrenizi girin")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        alertDialog.setView(input)

        alertDialog.setPositiveButton("Onayla") { _, _ ->
            val password = input.text.toString()

            if (password.isNotEmpty()) {
                // Kullanıcının kimlik bilgilerini yeniden doğrula
                val credential = EmailAuthProvider.getCredential(user.email!!, password)

                user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // Kimlik doğrulama başarılı, hesap silme onayı iste
                        AlertDialog.Builder(requireContext())
                            .setTitle("Hesap Silme")
                            .setMessage("Hesabınızı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                            .setPositiveButton("Evet") { _, _ ->
                                // Önce Realtime Database'den kullanıcı verilerini sil
                                databaseReference.removeValue().addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        // Sonra Firebase Authentication hesabını sil
                                        user.delete().addOnCompleteListener { deleteTask ->
                                            if (deleteTask.isSuccessful) {
                                                Toast.makeText(requireContext(), "Hesabınız başarıyla silindi.", Toast.LENGTH_LONG).show()
                                                // Giriş ekranına yönlendir
                                                val intent = Intent(requireContext(), GirisActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                requireActivity().finish()
                                            } else {
                                                Toast.makeText(requireContext(), "Hesap silme hatası: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "Veritabanı silme hatası: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            .setNegativeButton("Hayır") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Şifre yanlış!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Lütfen şifrenizi girin", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        alertDialog.show()
    }
}
