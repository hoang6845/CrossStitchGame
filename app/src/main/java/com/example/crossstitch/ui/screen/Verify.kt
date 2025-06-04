package com.example.crossstitch.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.crossstitch.R
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.FragmentVerifyBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.example.crossstitch.utils.saveBitmapToGallery
import com.example.crossstitch.viewmodel.PatternViewModel
import com.example.crossstitch.viewmodel.VerifyViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Verify : Fragment() {
    private lateinit var verifyMainBinding: FragmentVerifyBinding
    private lateinit var verifyViewModel: VerifyViewModel
    private var handleSaveImage:View.OnClickListener? = null
    private var handleGoBack:View.OnClickListener? = null
    private var handleShare: View.OnClickListener? = null
    private var navController: NavController? = null
    var bitmap: Bitmap? = null

    private var adapter:PatternAdapter? = null
    private var patternViewModel: PatternViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        val factory = PatternViewModel.providerFactory(PatternRepository.getInstance(requireContext()), GameProgressRepository.getInstance(requireContext()))
        patternViewModel = ViewModelProvider(requireActivity(), factory).get(PatternViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        verifyMainBinding = FragmentVerifyBinding.inflate(layoutInflater)
        verifyViewModel = ViewModelProvider(requireActivity()).get(VerifyViewModel::class.java)
        bitmap =  verifyViewModel.bitmap.value
        verifyMainBinding.img.setImageBitmap(bitmap)
        prepareHandle()
        verifyMainBinding.btnDownload.background = null
        verifyMainBinding.btnDownload.setOnClickListener(handleSaveImage)
//        verifyMainBinding.btnHome.background = null
//        verifyMainBinding.btnHome.setOnClickListener(handleGoBack)
        verifyMainBinding.btnShare.background = null
        verifyMainBinding.btnShare.setOnClickListener(handleShare)

        adapter = PatternAdapter(object : IPatternRv {
            override fun onClickItem(position: Int) {
                val bundle= Bundle()
                bundle.putInt("patternId", this@Verify.adapter?.listPattern?.get(position)?.id!!)
                bundle.putString("type", "App")
                navController!!.navigate(R.id.gameManager, bundle)
            }

        }, listOf(), listOf(), mutableListOf())
        verifyMainBinding.rv.adapter = adapter
        verifyMainBinding.rv.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        verifyMainBinding.rv.post{
            verifyMainBinding.rv.scrollToPosition(verifyViewModel.position.value?:0)
        }
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(verifyMainBinding.rv)

        patternViewModel?.listPatternLiveData?.observe(viewLifecycleOwner, {list->
            updateAdapter(list, patternViewModel?.listGameProgressLiveData?.value?: emptyList())
        })

        patternViewModel?.listGameProgressLiveData?.observe(viewLifecycleOwner, {list ->
            updateAdapter(patternViewModel?.listPatternLiveData?.value?: emptyList(), list)
        })

        return verifyMainBinding.root
    }

    private fun getPatternApp(listPattern: List<PatternData>): List<PatternData> {
        return listPattern.filter { it.Category!=null }
    }

    private fun updateAdapter(allPaterns: List<PatternData>, allProgress: List<GameProgress>){
        var listPattern:List<PatternData>? = null
        if (verifyViewModel.category.value != null){
            listPattern = getPatternApp(allPaterns)
        }else{
            listPattern = allPaterns
        }
        val listPatternId = listPattern.map { it.id }
        val listProgress = allProgress.filter { it.patternId in listPatternId }
        adapter?.apply {
            this.listPattern = listPattern
            this.listProgress = listProgress
            this.listState = MutableList(listPattern.size){true}
            notifyDataSetChanged()
        }

    }

    private fun prepareHandle(){
        handleSaveImage = View.OnClickListener {
            checkStoragePermissionAndSave()
        }
//        handleGoBack = View.OnClickListener {
//            verifyViewModel.clearBitMap()
//            navController?.navigate(R.id.menuPatternContainer)
//        }
        handleShare = View.OnClickListener {
            bitmap?.let { it1 -> shareImageBitmap(it1) }
        }
    }


    private fun checkStoragePermissionAndSave() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.REQUEST_CODE_WRITE_STORAGE
                )
            } else {
                bitmap?.let { saveBitmapToGallery(requireContext(), it) }
            }
        } else {
            bitmap?.let { saveBitmapToGallery(requireContext(), it) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bitmap?.let { saveBitmapToGallery(requireContext(), it) }
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareImageBitmap(bitmap: Bitmap){
        try{
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()

            val file = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")
            val fileOutputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            val imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TEXT, "Share from Cross Stitch")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }catch (e: IOException){
                e.printStackTrace()
        }
    }





}