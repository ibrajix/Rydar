package com.ibrajix.rydar.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.FragmentConfirmBottomDialogSheetBinding
import com.ibrajix.rydar.databinding.FragmentHomeBinding

class ConfirmBottomDialogSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentConfirmBottomDialogSheetBinding? = null
    private val binding get() = _binding!!
    private var bottomSheet: View? = null
    private var mListener: ItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConfirmBottomDialogSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = dialog?.findViewById(R.id.lyt_container)

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val dialog = dialog as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                val behavior = BottomSheetBehavior.from(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                val newHeight = activity?.window?.decorView?.measuredHeight
                val viewGroupLayoutParams = bottomSheet.layoutParams
                viewGroupLayoutParams.height = newHeight ?: 0
                bottomSheet.layoutParams = viewGroupLayoutParams
            }
        })

        bottomSheet = view

        setUpViews()

    }

    private fun setUpViews() {

        // We can have cross button on the top right corner for providing elemnet to dismiss the bottom sheet
        //iv_close.setOnClickListener { dismissAllowingStateLoss() }
        //respond to click

        binding.lytNormalRide.setOnClickListener {
            Toast.makeText(requireContext(), "Selected", Toast.LENGTH_LONG).show()
        }

        binding.lytPremiumRide.setOnClickListener {
            Toast.makeText(requireContext(), "Selected", Toast.LENGTH_LONG).show()
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemClickListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement ItemClickListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface ItemClickListener {
        fun onItemClick(item: String)

    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ConfirmBottomDialogSheetFragment {
            val fragment = ConfirmBottomDialogSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}