package com.karthek.android.s.gallery.ui.photos

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.selection.*
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.karthek.android.s.gallery.MobileNavigationDirections
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.SMediaViewModel
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class PhotosFragment : Fragment(), View.OnClickListener, OnItemActivatedListener<Long?>,
    ActionMode.Callback, Observer<MutableList<SMedia>?> {

    @JvmField
    var sMediaList: MutableList<SMedia>? = null
    private var dirPath = -1
    private lateinit var recyclerView: RecyclerView
    var adapter: PhotosRecyclerViewAdapter? = null
    var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long>
    var selectedList = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt("dirIndex")?.let { dirPath = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView =
            inflater.inflate(R.layout.fragment_photo_item, container, false) as RecyclerView
        recyclerView.layoutManager = StaggeredGridLayoutManager(
            3,
            StaggeredGridLayoutManager.VERTICAL
        )
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sMediaViewModel: SMediaViewModel by activityViewModels()
        if (dirPath == -1) {
            sMediaViewModel.sMediaList.observe(viewLifecycleOwner, this)
        } else {
            sMediaViewModel.getFolderContents(dirPath)
                .observe(viewLifecycleOwner) { onChanged(it?.get()) }
        }
    }

    override fun onClick(v: View) {
        val viewHolder = v.tag as PhotosRecyclerViewAdapter.ViewHolder
        Log.v("img", "photo:" + viewHolder.mItem.path)
        val action = MobileNavigationDirections.actionGlobalMediaPagerFragment()
        action.dirIndex = dirPath
        action.position = viewHolder.mItem.origPos
        findNavController(v).navigate(action)
    }

    override fun onItemActivated(item: ItemDetails<Long?>, e: MotionEvent): Boolean {
        Log.v("yes", "item activated")
        return false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        adapter!!.inActionMode = true
        mode.menuInflater.inflate(R.menu.cab, menu)
        mode.title = "1"
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.cab_share) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.putExtra(Intent.EXTRA_STREAM, selectedList)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, null))
            return true
        } else if (id == R.id.cab_select_all) {
            for (s in sMediaList!!.indices) {
                selectionTracker.select(s.toLong())
            }
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter!!.inActionMode = false
        selectionTracker.clearSelection()
        //adapter!!.notifyDataSetChanged()
        actionMode = null
    }

    override fun onChanged(sMedia: MutableList<SMedia>?) {
        sMediaList = sMedia
        if (sMedia != null) {
            if (adapter == null) adapter = PhotosRecyclerViewAdapter(this::onClick)
            adapter?.sMediaList = sMedia
            recyclerView.adapter = adapter
            selectionTracker = SelectionTracker.Builder(
                dirPath.toString(),
                recyclerView,
                StableIdKeyProvider(recyclerView),
                MDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
            )
                .withOnItemActivatedListener(this)
                .build()
            selectionTracker.addObserver(SObserver())
            adapter?.selectionTracker = selectionTracker
        }
    }


    private inner class SObserver : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            if (!adapter!!.inActionMode) {
                //adapter.notifyDataSetChanged();
                requireActivity().startActionMode(this@PhotosFragment)
            } else {
                if (actionMode != null) {
                    val s = selectedList.size
                    if (s != 0) actionMode!!.title = s.toString() else actionMode!!.finish()
                }
            }
        }

        override fun onItemStateChanged(key: Long, selected: Boolean) {
            val uri = sMediaList!![key.toInt()].uri
            if (uri != null) {
                if (selected) {
                    if (!selectedList.contains(uri)) selectedList.add(uri)
                } else {
                    selectedList.remove(uri)
                }
            }
        }

    }

    class MDetailsLookup(private var recyclerView: RecyclerView?) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView!!.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val holder = recyclerView!!.getChildViewHolder(view)
                if (holder is PhotosRecyclerViewAdapter.ViewHolder) {
                    return holder.itemDetails
                }
            }
            return null
        }
    }

    companion object {
        val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private var bitmapLruCache: LruCache<String, Bitmap> =
            object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }

        @JvmField
        var executorService: ExecutorService =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2)

        @JvmStatic
        fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
            bitmapLruCache.put(key, bitmap)
        }

        @JvmStatic
        fun getBitmapFromMemCache(key: String): Bitmap? {
            return bitmapLruCache[key]
        }
    }
}