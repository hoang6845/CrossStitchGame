package com.example.crossstitch.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.crossstitch.R
import com.example.crossstitch.databinding.FragmentHomePageBinding

private lateinit var homeBinding: FragmentHomePageBinding
class HomePage : Fragment() {
    private var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentHomePageBinding.inflate(layoutInflater, container, false)
        homeBinding.btnStart.setOnClickListener {
            navController?.navigate(R.id.menuPatternContainer)
        }
        homeBinding.btnCreate.setOnClickListener {
            navController?.navigate(R.id.createOwnPattern)
        }
        homeBinding.btnInProgress.setOnClickListener {

        }
        return homeBinding.root
    }


}