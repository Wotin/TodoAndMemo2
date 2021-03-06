package com.simplepro.secondtodoandmemo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.simplepro.secondtodoandmemo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.simplepro.secondtodoandmemo.instance.UserInstance
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var authUid : String

    lateinit var docRef : DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loginButton.setOnClickListener {
            loginEmail()
        }

        GoSighUpActivityTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginEmail() {
        val email = emailEditTextLogin.text.toString()
        val password = passwordEditTextLogin.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty())
        {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    moveNextPage()
                    return@addOnCompleteListener
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "존재하지 않는 계정입니다.", Toast.LENGTH_LONG).show()
                    Log.d("TAG", it.toString())
                }
        }
    }

    private fun moveNextPage() {
        val successIntent = Intent(this, MainActivity::class.java)
        if(FirebaseAuth.getInstance().currentUser != null)
        {
            var todoIdCount : String? = "0"
            var memoIdCount : String? = "0"
            var userId : String? = null
            var userEmail : String? = null
            var user = UserInstance(userId.toString(), userEmail.toString(), todoIdCount.toString(), memoIdCount.toString())
            authUid = FirebaseAuth.getInstance().currentUser!!.uid
            docRef = FirebaseFirestore.getInstance().collection("users").document(authUid)
            docRef.get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful)
                    {
                        userEmail = task.result?.getString("email")
                        userId = task.result?.getString("id")
                        Toast.makeText(applicationContext, userId + "님 환영합니다", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Log.d("TAG", "no exist No such document")
                    }
                    todoIdCount = task.result!!.getString("todoIdCount")
                    memoIdCount = task.result!!.getString("memoIdCount")
                    if(todoIdCount == null || memoIdCount == null)
                    {
                        user = UserInstance(userId.toString(), userEmail.toString(), "0", "0")
                        docRef.set(user)
                            .addOnCompleteListener { task ->
                                if(task.isSuccessful)
                                {
                                    Log.d("TAG", "user set success Login null")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("TAG", "user set failure Login null")
                            }
                    }
                    else if(todoIdCount != null || memoIdCount != null)
                    {
                        user = UserInstance(userId.toString(), userEmail.toString(), todoIdCount.toString(), memoIdCount.toString())
                        docRef.set(user)
                            .addOnCompleteListener { task ->
                                if(task.isSuccessful)
                                {
                                    Log.d("TAG", "user set success Login not null")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("TAG", "user set failure Login not null")
                            }
                    }
                }
                .addOnFailureListener {Exception ->
                    Log.d("TAG", "error is $Exception")
                }
        }
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null)
        {
            startActivity(successIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if(intent.hasExtra("secession"))
        {
            val secessionBoolean = intent.getBooleanExtra("secession", false)
            if(!secessionBoolean)
            {
                moveNextPage()
            }
        }
        else {
            moveNextPage()
        }
    }
}