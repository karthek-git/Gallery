package com.karthek.android.s.gallery.ui.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.karthek.android.s.gallery.a.MediaFolder
import com.karthek.android.s.gallery.databinding.FragmentMediaFolderItemBinding

class MediaFolderRecyclerViewAdapter(private val callback: (View) -> Unit) :
    RecyclerView.Adapter<MediaFolderRecyclerViewAdapter.ViewHolder>() {

    lateinit var mediaFolders: List<MediaFolder>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val fragmentMediaFolderItemBinding = FragmentMediaFolderItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        fragmentMediaFolderItemBinding.root.setOnClickListener(callback)
        return ViewHolder(fragmentMediaFolderItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            mItem = mediaFolders[position]
            root.tag = position
            imageView.load(mItem.previewImage)
            nameView.text = mItem.name
        }
    }

    override fun getItemCount(): Int = mediaFolders.size

    class ViewHolder(binding: FragmentMediaFolderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root: View = binding.root
        val imageView: ImageView = binding.folderImageView
        val nameView: TextView = binding.folderName
        lateinit var mItem: MediaFolder
    }

}