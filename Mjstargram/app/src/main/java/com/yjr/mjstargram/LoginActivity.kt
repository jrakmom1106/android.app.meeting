package com.yjr.mjstargram

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity:AppCompatActivity(){

    lateinit var emailEt:EditText
    lateinit var passwordEt:EditText
    lateinit var loadingPb:ProgressBar
    lateinit var loginBtn:Button

    lateinit var auth:FirebaseAuth
    lateinit var firestore:FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEt=findViewById(R.id.email_et)
        passwordEt=findViewById(R.id.password_et)
        loadingPb=findViewById(R.id.loading_pb)
        loginBtn=findViewById(R.id.login_btn)

        auth= FirebaseAuth.getInstance() //auth 하기
        firestore= FirebaseFirestore.getInstance()

        moveMain(auth.currentUser)    //대부분의 인스타그램이나 그런 것들은 로그인을 매번하도록 하게 하지 않기 때문에 로그인을 한번 하고 나서는 로그아웃을 하기 전까지 로그인을 유지하도록 하기위해 auth.current에 저장
        


        //버튼이 눌렸을떄
        loginBtn.setOnClickListener {
            var email = emailEt.text.toString()
            var password = passwordEt.text.toString()

            if (email.equals("")) {
                //이메일이 빈값일 경우
                Toast.makeText(this, "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener //setonclick리쓰너의 리턴값
            }

            if (password.length < 6) {
                Toast.makeText(this, "비밀번호는 6글자 이상이여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener //setonclick리쓰너의 리턴값
            }


            // 파이어베이스에 대한 접속 ,네트워크를 통해서
            startLoading()
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if (it.isSuccessful) {
                        //이전에 있는 사용자가 있는데 로그인을 한 경우
                        stopLoading()
                        moveMain(auth.currentUser)
                    } else if (it.exception?.message.isNullOrEmpty()) {
                        stopLoading()
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()

                    }
                    else{
                        join(email,password)
                    }
                }


        }





    }


    fun join(email:String,password:String){
        startLoading()
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    //로그인이 성공했을 때  >메인엑티비티로
                   stopLoading()

                    //유저 인스턴스 생성
                    var user=User(auth.currentUser?.email!!)

                    //로그 메인뿐만 아니라 파이어 스토어에 유저 저장
                   firestore.collection("User")
                       .document(user.email!!)
                       .set(user)
                       .addOnSuccessListener {
                           // 잘 생성이 된 경우
                           stopLoading()
                           moveMain(auth.currentUser)
                       }

                }
                else if(it.exception?.message.isNullOrEmpty()){
                    stopLoading()
                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }


   fun moveMain(user:FirebaseUser?){
       //널이 아닐때만 처리
       if(user!=null){
           var intent=Intent(this,MainActivity::class.java)
           startActivity(intent)
           finish()
       }
   }

    //로딩이 시작될떄
    fun startLoading(){
        loadingPb.visibility=VISIBLE

    }
    fun stopLoading(){
        loadingPb.visibility=GONE
    }






}