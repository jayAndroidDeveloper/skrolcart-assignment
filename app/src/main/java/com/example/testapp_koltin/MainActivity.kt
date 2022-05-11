package com.example.testapp_koltin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.testapp.adapter.VideoAdapter
import com.example.testapp.dataclass.VideoClass
import com.example.testapp_koltin.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var videoarraylist: ArrayList<VideoClass>
    private lateinit var videoadapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.floatAddbutton.setOnClickListener {
            startActivity(Intent(this, AddVideo::class.java))
        }
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filtersearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        loadVideosFromFirebase()


    }

    private fun filtersearch(filterIteam: String) {

        var templist: MutableList<VideoClass> = ArrayList()
        for (i in videoarraylist) {
            if (filterIteam in i.title.toString()) {
                templist.add(i)
            }
        }
        videoadapter.updatelist(templist as ArrayList<VideoClass>)
    }


    private fun loadVideosFromFirebase() {
        videoarraylist = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("videos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val video = ds.getValue(VideoClass::class.java)
                    videoarraylist.add(video!!)
                }
                videoadapter = VideoAdapter(this@MainActivity, videoarraylist)
                binding.videorycyclerview.adapter = videoadapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}
