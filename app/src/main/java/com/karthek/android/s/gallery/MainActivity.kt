package com.karthek.android.s.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior
import com.karthek.android.s.gallery.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var params: CoordinatorLayout.LayoutParams? = null
    private var behavior = ScrollingViewBehavior()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBar)
        val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissions(perms, 1)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_photos, R.id.navigation_explore, R.id.navigation_media_folders
        ).build()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        if (navHostFragment != null) {
            val navController: NavController = navHostFragment.navController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
            NavigationUI.setupWithNavController(binding.navView, navController)
            params = binding.navHostFragment.layoutParams as CoordinatorLayout.LayoutParams
            navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
                if (destination.id == R.id.mediaPagerFragment) {
                    params!!.behavior = null
                    binding.appBar.visibility = View.GONE
                    binding.navView.visibility = View.GONE
                } else {
                    params!!.behavior = behavior
                    binding.appBar.visibility = View.VISIBLE
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //ViewModelProvider(this).get("-1", SMediaViewModel::class.java).loadsMediaList()
        } else {
            onBackPressed()
        }
    }
}