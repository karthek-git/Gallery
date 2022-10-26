package com.karthek.android.s.gallery.ui.folders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.a.MediaFolder
import com.karthek.android.s.gallery.state.SMediaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFolderFragment : Fragment(), View.OnClickListener {

    private lateinit var recyclerView: RecyclerView
    private var adapter = MediaFolderRecyclerViewAdapter(this::onClick)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView =
            inflater.inflate(R.layout.fragment_media_folder_list, container, false) as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel: SMediaViewModel by activityViewModels()
        viewModel.folderList.observe(viewLifecycleOwner, { mediaFolders: List<MediaFolder>? ->
            if (mediaFolders != null) {
                adapter.mediaFolders = mediaFolders
                recyclerView.adapter = adapter
            }
        })
    }

    override fun onClick(v: View) {
        val mediaFolder = v.tag as Int
        val toNavigationPhotos =
            MediaFolderFragmentDirections.actionNavigationMediaFoldersToNavigationPhotos()
        toNavigationPhotos.dirIndex = mediaFolder
        findNavController(v).navigate(toNavigationPhotos)
    }


}