package com.example.crossstitch.ui.screen

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentVerifyBinding
import com.example.crossstitch.di.Constants
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.repository.GameProgressRepository
import com.example.crossstitch.repository.PatternRepository
import com.example.crossstitch.ui.adapter.PatternAdapter
import com.example.crossstitch.ui.adapter.irv.IRv
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
    private var handleShare: View.OnClickListener? = null
    private var navController: NavController? = null
    private var handlePlayAgain:View.OnClickListener? = null
    private var handleContinue:View.OnClickListener? = null
    private var handleDeleteArtWork: View.OnClickListener? = null
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
        verifyMainBinding.btnDownload.setOnClickListener(handleSaveImage)
        verifyMainBinding.btnPlayAgain.setOnClickListener(handlePlayAgain)
        verifyMainBinding.btnContinue.setOnClickListener(handleContinue)

        verifyMainBinding.btnShare.background = null
        verifyMainBinding.btnShare.setOnClickListener(handleShare)

        verifyMainBinding.btnContinue.isVisible = arguments?.getBoolean("isCompleted") != true

        adapter = PatternAdapter(object : IRv {
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

        binding.btnDelete.setOnClickListener(handleDeleteArtWork)

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
        handleShare = View.OnClickListener {
            bitmap?.let { it1 -> shareImageBitmap(it1) }
        }

        handlePlayAgain = View.OnClickListener {
            var patternId = arguments?.getInt("patternId")
            Log.d("TAG", "prepareHandle: ${patternId}")
            patternViewModel?.listGameProgressLiveData?.value?.find { gameProgress: GameProgress -> gameProgress.patternId == patternId }
                ?.let { it1 -> patternViewModel?.updateProgress(it1.copy(completedCells = 0, mistake = 0, myCrossStitchGrid = Array(Constants.NUMROWS) { IntArray(Constants.NUMCOLS) { Int.MIN_VALUE }})) }
            navController?.popBackStack(R.id.gameManager, true)
            Handler(Looper.getMainLooper()).postDelayed({
                val bundle = Bundle().apply {
                    if (patternId != null) {
                        putInt("patternId", patternId)
                    }
                }
                navController?.navigate(R.id.gameManager, bundle)
            }, 500)
        }

        handleDeleteArtWork = View.OnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_artwork, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialogView.findViewById<View>(R.id.btn_delete).setOnClickListener {
                var patternId = arguments?.getInt("patternId")
                patternViewModel?.listGameProgressLiveData?.value?.find { gameProgress: GameProgress -> gameProgress.patternId == patternId }
                    ?.let { it1 -> patternViewModel?.updateProgress(it1.copy(completedCells = 0, mistake = 0, myCrossStitchGrid = Array(Constants.NUMROWS) { IntArray(Constants.NUMCOLS) { Int.MIN_VALUE }})) }
                navController?.popBackStack(R.id.gameManager, true)
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        handleContinue = View.OnClickListener {
            navController!!.popBackStack()
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