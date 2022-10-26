package com.karthek.android.s.gallery.ui.pager

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.karthek.android.s.gallery.databinding.FragmentMPagerContentBinding
import com.karthek.android.s.gallery.state.ImageInfoViewModel
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MPagerContentFragment(val sMedia: SMedia, val callback: (View) -> Unit) : Fragment() {

    private lateinit var binding: FragmentMPagerContentBinding
    private val infoViewModel: ImageInfoViewModel by viewModels()
    private var simpleExoPlayer: SimpleExoPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMPagerContentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener(callback)
        val size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display?.getRealSize(size)
        } else {
            requireActivity().windowManager.defaultDisplay.getRealSize(size)
        }
        binding.mediaContainer.layoutParams.height = size.y
        binding.bottomControls.viewModel = infoViewModel
    }

    override fun onStart() {
        super.onStart()
        if (sMedia.isVideo) {
            binding.videoView.visibility = View.VISIBLE
            binding.videoView.setOnClickListener(callback)
            simpleExoPlayer = SimpleExoPlayer.Builder(requireActivity()).build().apply {
                binding.videoView.player = simpleExoPlayer
                val mediaItem = MediaItem.fromUri(sMedia.uri?.toString() ?: "file://${sMedia.path}")
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        } else {
            infoViewModel.setImage(sMedia.path)
            //binding.imageView.setImage(ImageSource.uri(sMedia.uri))
        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer?.release()
        simpleExoPlayer = null
    }
}