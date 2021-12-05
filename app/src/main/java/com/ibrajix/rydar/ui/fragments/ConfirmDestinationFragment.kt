package com.ibrajix.rydar.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.FragmentConfirmDestinationBinding
import com.ibrajix.rydar.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout


class ConfirmDestinationFragment : Fragment() {

    private var _binding: FragmentConfirmDestinationBinding? = null
    private val binding get() = _binding!!
    private var mBottomSheetLayout: ConstraintLayout? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConfirmDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        handleClicks()
    }

    private fun handleClicks(){

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}