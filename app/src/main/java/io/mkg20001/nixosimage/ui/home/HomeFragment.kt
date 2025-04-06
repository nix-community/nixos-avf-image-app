package io.mkg20001.nixosimage.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.mkg20001.nixosimage.Install
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.ui.DropdownItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        val installBtn = binding.installBtn
        val refreshBtn = binding.refreshBtn

        val methodsDropdown = binding.installMethod
        val versionsDropdown = binding.nixosVersion

        homeViewModel.state.observe(viewLifecycleOwner) {
            val str = when(it) {
                ImageViewState.LOADING -> R.string.introduction_loading
                ImageViewState.ERROR -> R.string.introduction_error
                ImageViewState.READY -> R.string.introduction
            }
            textView.text = getString(str)
            installBtn.isEnabled = it == ImageViewState.READY
            refreshBtn.isEnabled = it != ImageViewState.LOADING

            methodsDropdown.isEnabled = it === ImageViewState.READY
            versionsDropdown.isEnabled = it === ImageViewState.READY
        }

        homeViewModel.installMethods.observe(viewLifecycleOwner) {
            var items = it.map { DropdownItem(it.id, it.displayString) }

            if (items.isEmpty()) {
                items = listOf(DropdownItem(true, resources.getString(R.string.no_method)))
            }

            DropdownItem.setItems(requireContext(), items, methodsDropdown)
        }

        homeViewModel.imageRelease.observe(viewLifecycleOwner) {
            var items = it.map { DropdownItem(it.tagName, it.nixosVersion) }

            if (!items.isEmpty()) {
                items = listOf(DropdownItem(true, resources.getString(R.string.loading)))
            }

            DropdownItem.setItems(requireContext(), items, versionsDropdown)
        }

        fun updateInstall() {
            installBtn.isEnabled = homeViewModel.state.value == ImageViewState.READY &&
                    DropdownItem.selectedAndNotPlaceholder(methodsDropdown) &&
                    DropdownItem.selectedAndNotPlaceholder(versionsDropdown)
        }

        DropdownItem.onChange(versionsDropdown) { updateInstall() }
        DropdownItem.onChange(methodsDropdown) { updateInstall() }

        CoroutineScope(Dispatchers.IO).launch {
            homeViewModel.refresh()
        }

        return root
    }

    private fun install(r: GitHubReleaseAsset, m: ImageInstallMethod) {
        val intent: Intent = Intent(
            this.context,
            Install::class.java
        )
        val b = Bundle()
        b.putSerializable("image", r)
        b.putString("method", m.id)
        intent.putExtras(b)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}