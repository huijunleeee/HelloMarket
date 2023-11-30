package com.example.hellomarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hellomarket.DBKey.Companion.CHILD_CHAT
import com.example.hellomarket.DBKey.Companion.DB_ARTICLES
import com.example.hellomarket.DBKey.Companion.DB_USERS
import com.example.hellomarket.R
import com.example.hellomarket.ChatListItem
import com.example.hellomarket.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database

import com.google.firebase.ktx.Firebase

// Fragment에서는 ContentView를 다음과 같이 파라미터로 넣어서 설정한다.
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference




    private val articleList = mutableListOf<ArticleModel>()


    //     액티비티의 경우 재사용하지 않기 때문에 문제 없지만
//     Fragment의 경우 재사용되기 때문에
//     .addChildEventListener()와 같은 이벤트 메소드를 한번 붙여놓고 onViewCreated() 될 때마다 붙이게 된다면
//     중복해서 이벤트 리스너가 붙여질 가능성이 있다.
//     따라서 이벤트 리스너를 전역으로 정의해놓고 onViewCreated()가 될 때마다(즉, Fragment가 생성될 때마다) 붙여주고, Fragment가 Destroy될 때마다 remove를 해주는 방식을 사용해야 한다.
    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

//      Data Model을 만들어 DB에 한번에 올리고 가져옴
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    fun loadArticles(status: String? = null) {
        val query = if (status == null) {
            // 전체 판매글을 불러오는 쿼리
            Firebase.database.reference.child(DB_ARTICLES)
        } else {
            // 특정 상태의 판매글만 불러오는 쿼리
            Firebase.database.reference.child(DB_ARTICLES).orderByChild("status").equalTo(status)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                articleList.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val article = postSnapshot.getValue(ArticleModel::class.java)
                    article?.let { articleList.add(it) }
                }
                articleAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        val spinner: Spinner = fragmentHomeBinding.spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.my_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position) as String
                when (selected) {
                    "전체" -> loadArticles()
                    "판매 중" -> loadArticles("판매 중")
                    "판매 완료" -> loadArticles("판매 완료")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // 기존 코드


//         Fragment를 옮겨 다닐 경우, View는 초기화되지만 HomeFragment 자체가 초기화된 것이 아니기 때문에 데이터가 쌓이게 된다.
//         따라서 View가 초기화될 때 다음과 같이 데이터도 초기화되도록 만들어줘야 한다.
        articleList.clear()



        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel->
            if (auth.currentUser != null){
                // todo 로그인을 한 상태


                startActivity(Intent(requireContext(), ArticleDetailActivity::class.java))

            } else {
                // todo 로그인을 안한 상태
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()

            }

        } )

        // Fragment는 context를 가지고 있지 않으므로 context키워드를 통해 가져옴
        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingButton.setOnClickListener{

            if(auth.currentUser != null){
                val intent = Intent(requireContext(), AddArticleActivity::class.java)
                startActivity(intent)
            }else{
                // Toast와 동일한 기능, Toast와 인터페이스가 약간 다름
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }

        // onViewCreated()가 될 때마다(즉, Fragment가 생성될 때마다) 이벤트 리스너를 붙여줌
        articleDB.addChildEventListener(listener)

    }

    override fun onResume() {
        super.onResume()

        // View가( Fragment가 ) 다시 보일 때마다 View를 다시 그리는 것으로 리스트의 값을 최신으로 갱신
        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Fragment의 View가 Destroy될 때마다 이벤트 리스너가 remove를 해주는 방식을 사용해야 한다.
        articleDB.removeEventListener(listener)
    }
}