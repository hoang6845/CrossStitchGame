package com.example.crossstitch.ui.screen

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentMenuPatternContainerBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.CategoryPagerAdapter
import com.example.crossstitch.utils.TakePhotoUtils
import com.example.crossstitch.viewmodel.ImageViewModel
import com.example.crossstitch.viewmodel.PatternViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class menuPatternContainer : Fragment() {
    private lateinit var menuBinding:FragmentMenuPatternContainerBinding
    private var adapter:CategoryPagerAdapter? = null
    private lateinit var viewModel: PatternViewModel
    var navController: NavController? = null
    private var handleAddImage:View.OnClickListener?=null
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var takePhotoUtils: TakePhotoUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePhotoUtils = TakePhotoUtils(
            this,
            onImagePicked = { bitmap: Bitmap ->
                imageViewModel.setBitmap(bitmap)
                val converter = ConverterPixel()
                val grid = converter.generatePatternFromBitmap(
                    bitmap,
                    Constants.NUMROWS,
                    Constants.NUMCOLS)
                val palette = converter.KMeansColor(grid, Constants.MAX_COLORS)
                imageViewModel.setPalette(palette)
                imageViewModel.setGrid(converter.quantizeColors(grid, Constants.MAX_COLORS, palette))
                Log.d("demm", "onCreate: ${palette.size}")
                navController?.navigate(R.id.createOwnPattern)
            },
            onError = { errorMessage: String ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navController = findNavController()
        menuBinding = FragmentMenuPatternContainerBinding.inflate(inflater, container, false)
        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)
        imageViewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)

        prepareHandle()
        menuBinding.btnAddPattern.setOnClickListener(handleAddImage)

        CoroutineScope(Dispatchers.IO).launch {
            val categories = (viewModel.findAllCategory())
            withContext(Dispatchers.Main){
                val currentList = mutableListOf("All") + categories
                viewModel.setCategories(currentList)
                adapter = viewModel.categories.value?.let {
                    CategoryPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle,
                        it
                    )
                }
                menuBinding.viewPager.adapter = adapter
                TabLayoutMediator(menuBinding.tabCategory, menuBinding.viewPager) {tab, position ->
                    val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab, null)
                    val tv = customView.findViewById<TextView>(R.id.tv)
                    tv.text = viewModel.categories.value?.get(position)
                    tab.customView = customView
                }.attach()
                menuBinding.tabCategory.tabRippleColor = null
                menuBinding.tabCategory.background = null
                setUpTabListener(currentList)
                val currentPosition = menuBinding.viewPager.currentItem
                updateTabAppearance(currentPosition, true, currentList)
            }
        }
        return menuBinding.root
    }

    private fun setUpTabListener(listCollectionType: List<String>){
        menuBinding.tabCategory.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { updateTabAppearance(it, true, listCollectionType) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.position?.let { updateTabAppearance(it, false, listCollectionType) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d("Tab", "Tab reselected: ${tab?.text}")
            }

        })
    }

    private fun updateTabAppearance(position: Int, isSelected: Boolean, listCollectionType:List<String>) {
        val tab = menuBinding.tabCategory.getTabAt(position)
        val layoutRes = if (isSelected) R.layout.custom_tab_indicator else R.layout.custom_tab

        val customView = LayoutInflater.from(requireContext()).inflate(layoutRes, null)
        val tv = customView.findViewById<TextView>(R.id.tv)
        tv.text = listCollectionType[position]

        tab?.customView = customView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun prepareHandle(){
        handleAddImage = View.OnClickListener {
            takePhotoUtils.showImagePickerDialog()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        takePhotoUtils.handlePermissionResult(requestCode, grantResults)

    }
}