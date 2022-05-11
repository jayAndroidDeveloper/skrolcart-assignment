package com.example.testapp.adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.dataclass.VideoClass
import com.example.testapp_koltin.R




class VideoAdapter(private val context: Context, private var videoArraylist: ArrayList<VideoClass>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view  = LayoutInflater.from(context).inflate(R.layout.video_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videomodel = videoArraylist[position]

        holder.videotittle.text = videomodel.title
        setVideo(videomodel,holder)

    }

    private fun setVideo(videomodel: VideoClass, holder: ViewHolder) {

        val videourl: String = videomodel.videouri!!

        val mediacontroller = MediaController(context)
        mediacontroller.setAnchorView(holder.videoview)
        val videouri = Uri.parse(videourl)

        holder.videoview.setMediaController(mediacontroller)
        holder.videoview.setVideoURI(videouri)
        holder.videoview.requestFocus()
        holder.videoview.setOnPreparedListener {mediaplayer ->
            mediaplayer.start()

        }
        holder.videoview.setOnInfoListener(MediaPlayer.OnInfoListener{mp,what,extra ->
            when(what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                   return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    return@OnInfoListener true
                }
            }
            false
        })

        holder.videoview.setOnCompletionListener {mediaplayer ->
            mediaplayer.start()

        }

    }

    override fun getItemCount(): Int {
        return videoArraylist.size
    }

    class ViewHolder(iteamView: View) : RecyclerView.ViewHolder(iteamView) {

        var videotittle = iteamView.findViewById<TextView>(R.id.playerviewtittle)
        var videoview = iteamView.findViewById<VideoView>(R.id.playerView)

    }

    fun updatelist(templist: ArrayList<VideoClass>) {
       this.videoArraylist = templist
        notifyDataSetChanged()
    }

}

