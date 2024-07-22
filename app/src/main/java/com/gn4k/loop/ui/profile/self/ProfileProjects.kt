package com.gn4k.loop.ui.profile.self

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gn4k.loop.databinding.FragmentProfileProjectsBinding


class ProfileProjects : Fragment() {

    lateinit var binding: FragmentProfileProjectsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileProjectsBinding.inflate(inflater, container, false)



        return binding.root

    }

    companion object {

    }
}