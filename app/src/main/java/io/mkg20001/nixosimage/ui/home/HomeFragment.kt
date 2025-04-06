package io.mkg20001.nixosimage.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.mkg20001.nixosimage.Install
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.ui.DropdownItem


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
        homeViewModel.state.observe(viewLifecycleOwner) {
            val str = when(it) {
                ImageViewState.LOADING -> R.string.introduction_loading
                ImageViewState.ERROR -> R.string.introduction_error
                ImageViewState.READY -> R.string.introduction
            }
            textView.text = getString(str)
            installBtn.isEnabled = it == ImageViewState.READY
        }

        val methodsDropdown = binding.installMethod
        homeViewModel.installMethods.observe(viewLifecycleOwner) {
            var items = it.map { DropdownItem(it.id, it.displayString) }

            if (items.isEmpty()) {
                items = listOf(DropdownItem(true, resources.getString(R.string.no_method)))
            }

            DropdownItem.setItems(this.requireContext(), items, methodsDropdown)
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