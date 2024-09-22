package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.media.audiofx.*
import android.util.Log
import androidx.media3.common.*
import androidx.media3.common.Player.Listener
import androidx.media3.exoplayer.ExoPlayer


class ExoPlayerMedia (private val context: Context ){

    private var isRelease: Boolean? = true;
    var player: ExoPlayer? =null;
    private var listener : Listener?=null;

    init {
        initial()
    }


    fun initial() {

        if(context!=null){
            player = ExoPlayer.Builder(context).build()
            setAttributes(null)
        }else{
            Log.e("ExoPlayerMedia","the ExoPlayer  initial is Wrong because the context is null")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun setAttributes(audioAttributes:AudioAttributes?=null){
      var _audioAttributes:AudioAttributes?=null
        if(audioAttributes==null){
            _audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build()
        }else{
            _audioAttributes=audioAttributes
        }

        player?.setAudioAttributes(_audioAttributes, true)
    }
    @SuppressLint("UnsafeOptInUsageError", "Range")
    fun playMedia(url: String) {
        if(isRelease==true)
            initial()

       val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.AUDIO_WAV)
            .build()
//    // تحميل الملف الصوتي
//        val dataSourceFactory = DefaultDataSourceFactory(context!!, "your_app_name")
//        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(mediaItem)


        player?.volume = 1.0f  // 1.0 هو الحد الأقصى لمستوى الصوت
        player?.setMediaItem(mediaItem)
        player?.setAudioAttributes(audioAttributes,true)
        player?.prepare()
        player?.playWhenReady = true

    }


    fun onDestroy() {
        isRelease=true
        if(player!=null)
            player!!.release()
    }
    fun isPlayer():Boolean {
       return player?.isPlaying?:false
    }
    fun stop() {
        try{
            isRelease=true
            if( player?.isPlaying==true){
                player?.stop()
            }
            player?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
