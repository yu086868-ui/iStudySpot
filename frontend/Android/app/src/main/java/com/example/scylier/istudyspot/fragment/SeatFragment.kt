package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.customview.SeatMapView
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeatFragment : Fragment() {
    private lateinit var seatMapView: SeatMapView
    private lateinit var tvStudyRoomName: TextView
    private lateinit var repository: MainRepository
    private var studyRoomId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_seat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seatMapView = view.findViewById(R.id.seat_map_view)
        tvStudyRoomName = view.findViewById(R.id.tv_study_room_name)
        repository = MainRepository(requireContext())

        // 获取从StudyRoomFragment传递的自习室ID
        studyRoomId = arguments?.getString("studyRoomId")
        val studyRoomName = arguments?.getString("studyRoomName")

        tvStudyRoomName.text = studyRoomName

        // 加载座位图
        studyRoomId?.let { loadSeatMap(it) }

        // 设置座位点击事件
        seatMapView.setOnSeatClickListener {
            if (it.status == "available") {
                // 跳转到预订页面
                val bookingFragment = BookingFragment()
                val bundle = Bundle()
                bundle.putString("seatId", it.id)
                bundle.putString("studyRoomId", studyRoomId)
                bundle.putString("studyRoomName", studyRoomName)
                bundle.putString("seatPosition", "${it.row}-${it.col}")
                bundle.putDouble("pricePerHour", it.pricePerHour)
                bookingFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, bookingFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "该座位不可预订", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSeatMap(studyRoomId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = repository.getStudyRoomSeats(studyRoomId)
            when (response) {
                is ApiResponse.Success -> {
                    val seatMap = response.data
                    seatMapView.setSeats(seatMap.seats)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
