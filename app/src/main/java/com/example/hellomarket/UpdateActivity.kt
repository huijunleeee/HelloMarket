package com.example.hellomarket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hellomarket.databinding.ActivityUpdateBinding
import com.example.hellomarket.ArticleModel
import com.example.hellomarket.DBKey.Companion.DB_ARTICLES
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.database

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleModel: ArticleModel = intent.getParcelableExtra("articleModel")!!

        binding.priceEditText.setText(articleModel.price)

        // 선택된 라디오 버튼을 설정
        when (articleModel.status) {
            "판매 중" -> binding.radioSale.isChecked = true
            "판매 완료" -> binding.radioSold.isChecked = true
            // 다른 상태가 있다면 해당 상태에 맞게 처리
        }

        binding.updateButton.setOnClickListener {
            val price = binding.priceEditText.text.toString()
            // 선택된 라디오 버튼의 ID를 가져와서 해당 ID에 해당하는 문자열을 얻어옴
            val status = when (binding.statusRadioGroup.checkedRadioButtonId) {
                R.id.radioSale -> "판매 중"
                R.id.radioSold -> "판매 완료"
                // 다른 라디오 버튼이 추가된 경우 처리
                else -> ""
            }

            val postValues: Map<String, Any> = mapOf(
                "price" to price,
                "status" to status
            )

            Firebase.database.reference.child(DB_ARTICLES).child(articleModel.id)
                .updateChildren(postValues)
                .addOnSuccessListener {
                    Toast.makeText(this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "게시글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}