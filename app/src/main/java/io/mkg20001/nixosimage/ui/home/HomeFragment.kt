package io.mkg20001.nixosimage.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.mkg20001.nixosimage.BuildConfig
import io.mkg20001.nixosimage.Install
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods
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

        refreshBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                homeViewModel.refresh()
            }
        }

        installBtn.setOnClickListener { // is only enabled when everything is ok
            val method = DropdownItem.getItem(methodsDropdown)!!
            val release = DropdownItem.getItem(versionsDropdown)!!
            install(
                homeViewModel.imageRelease.value!!.filter { it.id == release.id }.getOrNull(0)!!,
                InstallMethods.getMethod(method.id)!!
            )
        }

        homeViewModel.installMethods.observe(viewLifecycleOwner) {
            var items = it.map {
                if (BuildConfig.ALLOW_ANY_METHOD && !it.isAvailable()) {
                    DropdownItem(it.id, "#DEBUG# " + getString(it.display))
                } else {
                    DropdownItem(it.id, getString(it.display))
                }
            }

            if (items.isEmpty()) {
                items = listOf(DropdownItem(true, resources.getString(R.string.no_method)))
            }

            DropdownItem.setItems(requireContext(), items, methodsDropdown)
        }

        homeViewModel.imageRelease.observe(viewLifecycleOwner) {
            var items = it.map {
                DropdownItem(it.id, it.version + " (" + it.updatedAt + ", " + it.arch + ")")
            }

            if (items.isEmpty()) {
                items = listOf(DropdownItem(true, getString(R.string.no_compat)))
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
        if (m.needsExternalStorage) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                requireContext().startActivity(intent)
                Toast.makeText(context, getString(R.string.toast_external_storage), Toast.LENGTH_LONG).show()
                return
            }
        }

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