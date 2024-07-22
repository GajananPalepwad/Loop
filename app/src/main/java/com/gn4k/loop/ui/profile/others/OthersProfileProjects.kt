package com.gn4k.loop.ui.profile.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gn4k.loop.databinding.FragmentProfileProjectsBinding

class OthersProfileProjects: Fragment() {

    lateinit var binding: FragmentProfileProjectsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileProjectsBinding.inflate(inflater, container, false)



        return binding.root

    }

    companion object {

    }

}