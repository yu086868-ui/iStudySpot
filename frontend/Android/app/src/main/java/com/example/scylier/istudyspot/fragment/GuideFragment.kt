package com.example.scylier.istudyspot.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.viewmodel.GuideViewModel

/**
 * 场馆导览Fragment
 * 展示自习室的场馆地图、设施信息、使用指南等
 */
class GuideFragment : Fragment() {

    private lateinit var viewModel: GuideViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[GuideViewModel::class.java]
        
        initFacilityList(view)
        initGuideInfo(view)
    }

    private fun initFacilityList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.facility_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        val adapter = FacilityAdapter(viewModel.facilities)
        recyclerView.adapter = adapter
    }

    private fun initGuideInfo(view: View) {
        val tvLocation = view.findViewById<TextView>(R.id.tv_location)
        val tvHours = view.findViewById<TextView>(R.id.tv_hours)
        val tvContact = view.findViewById<TextView>(R.id.tv_contact)
        
        tvLocation.text = viewModel.location
        tvHours.text = viewModel.openingHours
        tvContact.text = viewModel.contact
    }

    inner class FacilityAdapter(
        private val facilities: List<GuideViewModel.Facility>
    ) : RecyclerView.Adapter<FacilityAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_facility, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(facilities[position])
        }

        override fun getItemCount(): Int = facilities.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName = itemView.findViewById<TextView>(R.id.tv_facility_name)
            private val tvDesc = itemView.findViewById<TextView>(R.id.tv_facility_desc)

            fun bind(facility: GuideViewModel.Facility) {
                tvName.text = facility.name
                tvDesc.text = facility.description
            }
        }
    }
}
