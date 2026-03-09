package com.sugarmunch.sugarbeats.activities

import android.os.Bundle
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.interfaces.RefreshRecyclerViewListener
import com.sugarmunch.sugarbeats.adapters.ExcludedFoldersAdapter
import com.sugarmunch.sugarbeats.databinding.ActivityExcludedFoldersBinding
import com.sugarmunch.sugarbeats.extensions.config

class ExcludedFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityExcludedFoldersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.excludedFoldersList))
        setupMaterialScrollListener(binding.excludedFoldersList, binding.excludedFoldersAppbar)
        updateFolders()
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.excludedFoldersAppbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        val folders = config.excludedFolders.toMutableList() as ArrayList<String>
        binding.excludedFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())
        }

        val adapter = ExcludedFoldersAdapter(this, folders, this, binding.excludedFoldersList) {}
        binding.excludedFoldersList.adapter = adapter
    }

    override fun refreshItems() {
        updateFolders()
    }
}
