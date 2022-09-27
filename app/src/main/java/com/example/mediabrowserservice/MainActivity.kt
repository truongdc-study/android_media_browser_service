package com.example.mediabrowserservice

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediabrowserservice.adapter.SongAdapter
import com.example.mediabrowserservice.connection.MediaServiceConnection
import com.example.mediabrowserservice.model.Song
import com.example.mediabrowserservice.utils.execute
import com.example.mediabrowserservice.utils.setAlphaAnimation
import com.example.mediabrowserservice.viewmodel.MediaViewModel
import com.example.mediabrowserservice.viewmodel.MediaViewModelFactory
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), SongAdapter.Callback {

    private lateinit var viewModel: MediaViewModel
    private lateinit var containerListSong: RecyclerView
    private lateinit var progressLoading: ProgressBar
    private lateinit var tvNameOfSong: TextView
    private lateinit var seekBarMedia: SeekBar
    private lateinit var imgSkipPrevious: ImageView
    private lateinit var imgSkipNext: ImageView
    private lateinit var imgPlayOrPause: ImageView
    private lateinit var tvRunTimeStart: TextView
    private lateinit var tvRunTimeEnd: TextView
    private val songAdapter : SongAdapter by lazy { SongAdapter(this) }
    private var isUpdateSeeBar = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initViewModel()
        setViewContent()
        controllerMediaPlayer()
        setUpSeekBar()
    }

    private fun setViewContent() {
        viewModel.apply {
            mediaItems.observe(this@MainActivity) { resources ->
                resources.execute(
                    onSuccess = { listSong ->
                        progressLoading.isVisible = false
                        songAdapter.setListSong(listSong)
                    },
                    onError = { message ->
                        progressLoading.isVisible = false
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    },
                    onLoading = {
                        progressLoading.isVisible = true
                    }
                )
            }

            curPlayingSong.observe(this@MainActivity) { metadataItemSong ->
                tvNameOfSong.text = metadataItemSong?.description?.title
            }

            playbackState.observe(this@MainActivity) { state ->
                if (state?.state == PlaybackStateCompat.STATE_PLAYING)
                    imgPlayOrPause.setBackgroundResource(R.drawable.ic_pause)
                else imgPlayOrPause.setBackgroundResource(R.drawable.ic_play)
                state?.position?.toInt()?.let { statePos ->
                    seekBarMedia.progress = statePos
                    tvRunTimeStart.text = setCurPlayerTimeToTextView(statePos.toLong())
                }
            }

        }
    }

    private fun setUpSeekBar() {
        seekBarMedia.apply {
            viewModel.durationSong.observe(this@MainActivity) { duration ->
                if (isUpdateSeeBar) duration?.let {
                    max = it
                    tvRunTimeEnd.text = setCurPlayerTimeToTextView(it.toLong())
                }
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    /**
                     * Y can set text time of song
                     */
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUpdateSeeBar = false
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        viewModel.seekToSong(it.progress.toLong())
                        isUpdateSeeBar = true
                    }
                }
            })
        }
    }

    private fun initView() {
        containerListSong = findViewById(R.id.containerListSong)
        progressLoading = findViewById(R.id.progress_Loading)
        tvNameOfSong  = findViewById(R.id.tvNameSong)
        seekBarMedia = findViewById(R.id.seekBarMedia)
        imgSkipPrevious = findViewById(R.id.imgSkipPrevious)
        imgSkipNext = findViewById(R.id.imgSkipNext)
        imgPlayOrPause = findViewById(R.id.imgPlayOrPause)
        tvRunTimeStart = findViewById(R.id.tvRunTimeStart)
        tvRunTimeEnd = findViewById(R.id.tvRunTimeEnd)
        containerListSong.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songAdapter
        }
    }

    private fun initViewModel() {
        val mediaServiceConnection = MediaServiceConnection(this)
        val viewModelFactory = MediaViewModelFactory(mediaServiceConnection)
        viewModel = ViewModelProvider(this, viewModelFactory)[MediaViewModel::class.java]
    }

    private fun controllerMediaPlayer() =
        viewModel.apply {
            imgSkipPrevious.setOnClickListener {
                it.setAlphaAnimation()
                skipToPreviousSong()
            }
            imgSkipNext.setOnClickListener {
                it.setAlphaAnimation()
                skipToNextSong()
            }
            imgPlayOrPause.setOnClickListener {
                it.setAlphaAnimation()
                playOrPauseSong()
            }
        }

    private fun setCurPlayerTimeToTextView(ms: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(ms)
    }

    override fun onClickSong(song: Song) {
        viewModel.onPlayFromMediaId(song.mediaId, null)
    }
}
