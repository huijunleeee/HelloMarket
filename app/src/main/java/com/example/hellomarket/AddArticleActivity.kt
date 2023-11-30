package com.example.hellomarket


import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hellomarket.DBKey.Companion.DB_ARTICLES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AddArticleActivity : AppCompatActivity() {


    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }
    private var articleModel: ArticleModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)


        // push().setValue() 를 통해 해당 DB위치에 ArticleModel을 통으로 올려서 저장
        // 이렇게 Data Model을 통으로 push()하여 넣으면, Realtime Database 상에는
        // 헤당 DB 위치에 임의의 식별용 Key를 만들고 그 안에 해당 DataModel의 데이터를 넣게 된다.
        // ( 즉, 데이터를 새롭게 넣을 때마다 해당 위치에 임의의 Key가 만들어지고, 그 Key의 Value로서 Data Model이 통으로 들어가게 된다. )
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString().orEmpty()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString().orEmpty()
            val sellerId = auth.currentUser?.email.orEmpty().replace(".", "_")
            val status = "판매 중"
                /*if (findViewById<RadioButton>(R.id.saleRadioButton).isChecked) {
                "판매 중"
            } else if (findViewById<RadioButton>(R.id.soldOutRadioButton).isChecked) {
                "판매 완료"
            } else {
                ""
            }*/

            val content = findViewById<EditText>(R.id.contentEditText).text.toString().orEmpty()





            uploadArticle(sellerId, title, price, status,content)



        }

    }

    // firebase Storage에 데이터 넣기 + 데이터 가져오기 를 구현한 메소드 생성


    // 위의 코드에서 이 메소드가 사용된 부분을 보면 if로 image를 다루는 넣는 영역은 비동기이고 else로 빠진 영역은 동기이므로,
    // 메소드를 사용하여 각각의 영역에서 데이터를 넣는 식으로 코딩해주어야 한다.
    private fun uploadArticle(sellerId: String, title: String, price: String, status: String, content: String) {
        val newArticleRef = articleDB.push()
        val postId = newArticleRef.key ?: ""
        val model = ArticleModel(sellerId, title, System.currentTimeMillis(), "$price 원", status, content, postId)
        newArticleRef.setValue(model)

        finish()
    }

}