package com.example.hellomarket

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hellomarket.databinding.ActivityArticleDetailBinding
import com.example.hellomarket.ArticleModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.hellomarket.DBKey.Companion.CHILD_CHAT
import com.example.hellomarket.DBKey.Companion.DB_ARTICLES
import com.example.hellomarket.DBKey.Companion.DB_USERS
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private lateinit var userDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userDB = Firebase.database.reference.child(DB_USERS)

        val articleModel: ArticleModel? = intent.getParcelableExtra("articleModel")!!
        if (articleModel == null) {
            Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }else {


            binding.titleTextView.text = articleModel.title
            binding.priceTextView.text = "가격: ${articleModel.price}"
            binding.contentTextView.text = "소개글: ${articleModel.content}"
            binding.sellerEmailTextView.text = "판매자 아이디: ${articleModel.sellerId.replace("_", ".")}"
            binding.saleStatusTextView.text = articleModel.status

        }
        binding.chatButton.setOnClickListener {
            // 채팅창 시작 버튼 클릭 시 채팅창을 시작하도록 추가 구현
            if (auth.currentUser != null && articleModel != null) {
                // TODO: 채팅창 시작 로직 추가
                if(auth.currentUser!!.email != articleModel.sellerId.replace("_", ".")){

                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser!!.email?.replace(".", "_") ?: "",
                        sellerId = articleModel.sellerId,
                        key = System.currentTimeMillis()
                    )


                    val buyerChatRef = userDB.child(auth.currentUser!!.email?.replace(".", "_") ?: "")
                        .child(CHILD_CHAT)
                        .child(chatRoom.key.toString())

                    buyerChatRef.setValue(chatRoom)

                    val sellerChatRef = userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .child(chatRoom.key.toString())

                    sellerChatRef.setValue(chatRoom)

                    val chatKey = chatRoom.key // 또는 sellerChatRef.key 둘 중 하나를 사용
                    if (chatKey != null) {
                        val intent = Intent(this, ChatRoomActivity::class.java)
                        intent.putExtra("chatKey", chatKey)
                        startActivity(intent)
                    }

                    // 채팅방이 생성되었으므로 해당 채팅방으로 이동
                    Snackbar.make(binding.root,"채팅방이 생성되었습니다.", Snackbar.LENGTH_LONG).show()
                } else {
                    // todo 내가 올린 아이템일 때
                    Snackbar.make(binding.root,"내가 올린 아이템 입니다.", Snackbar.LENGTH_LONG).show()

                }
            }
        }
        binding.editButton.setOnClickListener {
            if (auth.currentUser != null && articleModel != null) {
                // TODO: 채팅창 시작 로직 추가
                if(auth.currentUser!!.email != articleModel.sellerId.replace("_", ".")){
                    Snackbar.make(binding.root,"본인의 판매글이 아닙니다", Snackbar.LENGTH_LONG).show()


                } else {
                    val intent = Intent(this, UpdateActivity::class.java)
                    intent.putExtra("articleModel", articleModel)
                    startActivity(intent)
                    // todo 내가 올린 아이템일 때


                }
            }

        }

        if (auth.currentUser == null || auth.currentUser!!.email != articleModel?.sellerId?.replace("_", ".")) {
            val editButtonContainer = findViewById<ViewGroup>(R.id.editButtonContainer)
            editButtonContainer.visibility = View.GONE
        }else{
            val chatButtonContainer= findViewById<ViewGroup>(R.id.chatButtonContainer)
            chatButtonContainer.visibility = View.GONE
        }


    }
}