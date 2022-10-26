package com.karthek.android.s.gallery.ui.pager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.print.PrintHelper
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.karthek.android.s.gallery.databinding.FragmentMediaPagerBinding
import com.karthek.android.s.gallery.state.SMediaViewModel
import com.karthek.android.s.gallery.state.db.SMedia


class MediaPagerFragment : Fragment() {

    lateinit var sMediaList: List<SMedia>
    private var dirIndex = 0
    private var position = 0

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {

        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        val activity: Activity? = activity
        if (activity != null
            && activity.window != null
        ) {
            activity.window.decorView.systemUiVisibility = flags
        }
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val mDelayHideTouchListener = OnTouchListener { view, motionEvent ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    //private ImageView mContentView;
    private lateinit var mControlsView: View
    private lateinit var viewPager: ViewPager2
    private val mShowPart2Runnable = Runnable { // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
        mControlsView.visibility = View.VISIBLE
    }
    private var mVisible = false
    private val mHideRunnable = Runnable { hide() }
    private lateinit var binding: FragmentMediaPagerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            dirIndex = getInt("dirIndex")
            position = getInt("position")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mVisible = true
        mControlsView = binding.fullscreenContentControls
        val sMediaViewModel by activityViewModels<SMediaViewModel>()
        sMediaList = if (dirIndex == -1) {
            sMediaViewModel.sMediaList.value!!
        } else {
            sMediaViewModel.getFolderContents(dirIndex).value!!.get()!!
        }
        viewPager = binding.viewpagerView
        viewPager.adapter = MediaSlidePagerAdapter(this)
        binding.sMedia = sMediaList[position]
        viewPager.setCurrentItem(position, false)
        binding.handler = Handlers()
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.sMedia = sMediaList[position]
            }
        })


        // Set up the user interaction to manually show or hide the system UI.
        //mContentView.setOnClickListener(view1 -> toggle());

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.buttonActionShare.setOnTouchListener(mDelayHideTouchListener)
    }

    override fun onResume() {
        super.onResume()

/*		if (getActivity() != null && getActivity().getWindow() != null) {
			int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			getActivity().getWindow().addFlags(flags);
		}*/mhide()

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    override fun onPause() {
        super.onPause()
        /*		if (getActivity() != null && getActivity().getWindow() != null) {
			getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			// Clear the systemUiVisibility flag
			getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
		}*/
        //getSupportActionBar().show();
        //viewPager.setSystemUiVisibility(0);
        //show();
    }

    private fun toggle() = if (mVisible) mhide() else mshow()

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mControlsView.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun mhide() {
        mControlsView.visibility = View.GONE
        binding.viewpagerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        mVisible = false
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
/*		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);*/
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
        val actionBar = supportActionBar
        actionBar?.show()
    }

    private fun mshow() {
        binding.viewpagerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        mControlsView.visibility = View.VISIBLE
        mVisible = true
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private val supportActionBar: ActionBar?
        get() {
            val activity = activity as AppCompatActivity?
            return activity!!.supportActionBar
        }

    inner class Handlers {
        fun shareHandler(sMedia: SMedia) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, sMedia.uri)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, null))
        }

        fun useAsHandler(sMedia: SMedia) {
            val intent = Intent(Intent.ACTION_ATTACH_DATA)
            intent.setDataAndType(sMedia.uri, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, null))
        }

        fun printHandler(sMedia: SMedia) {
            val photoPrinter = PrintHelper(requireActivity())
            photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FILL
            val bitmap = BitmapFactory.decodeFile(sMedia.path)
            photoPrinter.printBitmap("print", bitmap)
        }
    }

    private inner class MediaSlidePagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment =
            MPagerContentFragment(sMediaList[position]) { toggle() }

        override fun getItemCount(): Int = sMediaList.size
    }

    companion object {

        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}