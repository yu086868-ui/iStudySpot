package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyRoomFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudyRoomAdapter
    private lateinit var repository: MainRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_study_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        repository = MainRepository(requireContext())

        loadStudyRooms()
    }

    private fun loadStudyRooms() {
        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getStudyRooms()
            when (response) {
                is ApiResponse.Success -> {
                    val studyRooms = response.data.list
                    adapter = StudyRoomAdapter(studyRooms) {
                        // 跳转到座位图页面
                        val seatFragment = SeatFragment()
                        val bundle = Bundle()
                        bundle.putString("studyRoomId", it.id)
                        bundle.putString("studyRoomName", it.name)
                        seatFragment.arguments = bundle
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, seatFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    recyclerView.adapter = adapter
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class StudyRoomAdapter(
        private val studyRooms: List<StudyRoomItem>,
        private val onItemClickListener: (StudyRoomItem) -> Unit
    ) : RecyclerView.Adapter<StudyRoomAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_study_room, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val studyRoom = studyRooms[position]
            holder.bind(studyRoom)
        }

        override fun getItemCount(): Int = studyRooms.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
            private val tvAddress = itemView.findViewById<TextView>(R.id.tv_address)
            private val tvOpeningHours = itemView.findViewById<TextView>(R.id.tv_opening_hours)
            private val tvOccupancyRate = itemView.findViewById<TextView>(R.id.tv_occupancy_rate)

            fun bind(studyRoom: StudyRoomItem) {
                tvName.text = studyRoom.name
                tvAddress.text = studyRoom.address
                tvOpeningHours.text = studyRoom.openingHours
                tvOccupancyRate.text = "上座率: ${(studyRoom.occupancyRate * 100).toInt()}%"

                itemView.setOnClickListener {
                    onItemClickListener(studyRoom)
                }
            }
        }
    }
}
