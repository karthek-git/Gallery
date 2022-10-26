package com.karthek.android.s.gallery.ui.photos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.db.SMedia
import com.karthek.android.s.gallery.ui.photos.PhotosFragment.Companion.getBitmapFromMemCache
import java.text.SimpleDateFormat
import java.util.*

class PhotosRecyclerViewAdapter(private val callback: (View) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var sMediaList: MutableList<SMedia>
    lateinit var selectionTracker: SelectionTracker<Long>
    var inActionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == 0) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_photos, parent, false)
            view.setOnClickListener(callback)
            ViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_photo_items_header, parent, false)
            val layoutParams = StaggeredGridLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.isFullSpan = true
            view.layoutParams = layoutParams
            HeaderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.pos = position
            holder.mItem = sMediaList[position]
            holder.view.tag = holder
            val bitmap = getBitmapFromMemCache(holder.mItem.path)
            holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap)
            } else {
                holder.imageView.setImageResource(androidx.cardview.R.color.cardview_dark_background)
                PhotosFragment.executorService.execute(MediaPreview(holder, position))
            }
            holder.imageView.isActivated = selectionTracker.isSelected(position.toLong())
        } else {
            val viewHolder = holder as HeaderViewHolder
            viewHolder.textView.text = SimpleDateFormat("MMMM", Locale.US).format(
                Date(sMediaList[position].date)
            )
        }
    }

    override fun getItemCount(): Int = sMediaList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int {
        val sMedia = sMediaList[position]
        if (sMedia.isHeader != -1) {
            return sMedia.isHeader
        }
        return if (position != 0) {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.timeInMillis = sMedia.date
            cal2.timeInMillis = sMediaList[position - 1].date
            if (cal1[Calendar.MONTH] == cal2[Calendar.MONTH]) {
                sMedia.isHeader = 0
                0
            } else {
                sMediaList.add(position, SMedia(1, sMediaList[position].date))
                1
            }
        } else {
            sMediaList.add(position, SMedia(1, sMediaList[position].date))
            1
        }
    }

    class HeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.date_header_textView)
    }

    data class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.photo)
        var pos = 0
        lateinit var mItem: SMedia

        val itemDetails: ItemDetails<Long>
            get() = object : ItemDetails<Long>() {
                override fun getPosition(): Int = pos
                override fun getSelectionKey(): Long = pos.toLong()
            }
    }

    init {
        setHasStableIds(true)
    }
}