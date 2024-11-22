package com.example.tripsetgo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tripsetgo.databinding.FragmentItineraryBinding

class ItineraryFragment : Fragment() {

    private lateinit var binding: FragmentItineraryBinding
    private lateinit var viewModel: ItineraryViewModel
    private lateinit var adapter: ItineraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItineraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel first
        setupViewModel()

        // Then setup UI components
        setupRecyclerView()
        setupSearchButton()

        // Finally observe the ViewModel
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, ItineraryViewModelFactory())
            .get(ItineraryViewModel::class.java)
    }

    private fun setupRecyclerView() {
        adapter = ItineraryAdapter()
        binding.itineraryRecyclerView.apply {
            this.adapter = this@ItineraryFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchLocation(query)
            } else {
                Toast.makeText(context, "Please enter a destination", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.itineraryState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItineraryState.Initial -> {
                    binding.progressBar.isVisible = false
                    binding.itineraryRecyclerView.isVisible = false
                }

                is ItineraryState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.itineraryRecyclerView.isVisible = false
                }

                is ItineraryState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.itineraryRecyclerView.isVisible = true
                    val allActivities = state.itinerary.days.flatMap { it.activities }
                    adapter.submitList(allActivities)
                }

                is ItineraryState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.itineraryRecyclerView.isVisible = false
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}