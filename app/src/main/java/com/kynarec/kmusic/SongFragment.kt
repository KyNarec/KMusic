package com.kynarec.kmusic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class SongFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_song, container, false)

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SongFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val song = view.findViewById<ConstraintLayout>(R.id.constraint_layout)
//
//        song.setOnClickListener {
//            (activity as? MainActivity)?.playSong(requireView(), song.)
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}