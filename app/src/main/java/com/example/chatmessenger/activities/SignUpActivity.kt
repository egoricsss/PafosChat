@file:Suppress("DEPRECATION")

package com.example.chatmessenger.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatmessenger.R
import com.example.chatmessenger.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var pd: ProgressDialog
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        pd = ProgressDialog(this)

        // Переход на экран входа
        binding.signUpTextToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Обработка регистрации
        binding.signUpBtn.setOnClickListener {
            validateAndRegister()
        }
    }

    /**
     * Валидация полей и запуск регистрации
     */
    private fun validateAndRegister() {
        val name = binding.signUpEtName.text.toString().trim()
        val email = binding.signUpEmail.text.toString().trim()
        val password = binding.signUpPassword.text.toString()

        // Валидация имени
        if (name.isEmpty()) {
            binding.signUpEtName.error = "Введите имя"
            binding.signUpEtName.requestFocus()
            return
        }

        // Валидация email (формат)
        if (email.isEmpty()) {
            binding.signUpEmail.error = "Введите email"
            binding.signUpEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signUpEmail.error = "Введите корректный email"
            binding.signUpEmail.requestFocus()
            return
        }

        // Валидация пароля (минимум 8 символов)
        if (password.isEmpty()) {
            binding.signUpPassword.error = "Введите пароль"
            binding.signUpPassword.requestFocus()
            return
        }

        if (password.length < 8) {
            binding.signUpPassword.error = "Пароль должен содержать минимум 8 символов"
            binding.signUpPassword.requestFocus()
            return
        }

        // Если все валидации пройдены — регистрируем
        createAnAccount(name, password, email)
    }

    private fun createAnAccount(name: String, password: String, email: String) {
        pd.show()
        pd.setMessage("Registering User")


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                pd.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val dataHashMap = hashMapOf(
                            "userid" to it.uid,
                            "username" to name,
                            "useremail" to email,
                            "status" to "default",
                            "imageUrl" to "https://openclipart.org/image/2000px/247320"
                        )

                        firestore.collection("Users")
                            .document(it.uid)
                            .set(dataHashMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, SignInActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Ошибка сохранения данных: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
