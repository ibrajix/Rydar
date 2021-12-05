package com.ibrajix.rydar.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ibrajix.rydar.R
import com.ibrajix.rydar.ui.fragments.ConfirmBottomDialogSheetFragment
import com.ibrajix.rydar.utils.GeneralUtility.transparentStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContainerActivity : AppCompatActivity(), ConfirmBottomDialogSheetFragment.ItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        transparentStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
    }

    override fun onItemClick(item: String) {
        //respond to item click
    }

}