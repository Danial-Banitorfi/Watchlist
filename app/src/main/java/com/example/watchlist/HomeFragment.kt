package com.example.watchlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.watchlist.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Hier wird später die Suche und die Liste implementiert
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}