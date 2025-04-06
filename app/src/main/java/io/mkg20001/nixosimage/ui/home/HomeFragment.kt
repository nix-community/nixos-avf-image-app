package io.mkg20001.nixosimage.ui.home

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.mkg20001.nixosimage.Install
import io.mkg20001.nixosimage.MainActivity
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import android.widget.ArrayAdapter


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
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val methodsDropdown = binding.installMethod
        homeViewModel.installMethods.observe(viewLifecycleOwner) {
            var items = it.map { it.displayString }

            if (items.isEmpty()) {
                items = listOf("No install methods found")
            }

            val adapter = ArrayAdapter(this.requireContext(), R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            methodsDropdown.adapter = adapter
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