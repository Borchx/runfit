package com.borja.android.runfit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borja.android.runfit.databinding.FragmentSesionesGuardadasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SesionesGuardadasFragment : Fragment(), SesionRutaAdapter.OnDeleteClickListener {

    private var _binding: FragmentSesionesGuardadasBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var sesionRutaAdapter: SesionRutaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sesiones_guardadas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewSesiones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sesionRutaAdapter = SesionRutaAdapter(this)
        recyclerView.adapter = sesionRutaAdapter

        // Cargar las sesiones guardadas desde la base de datos y actualizar el adaptador
        val dao = AppDatabase.getInstance(requireContext()).sesionRutaDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sesionesRuta = dao.getAllSesionesRuta()
            withContext(Dispatchers.Main) {
                sesionRutaAdapter.submitList(sesionesRuta)
            }
        }
    }

    override fun onDeleteClick(sesionRuta: SesionRuta) {
        showDeleteConfirmationDialog(sesionRuta)
    }

    private fun showDeleteConfirmationDialog(sesionRuta: SesionRuta) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this session?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSesionRuta(sesionRuta)
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }
    private fun deleteSesionRuta(sesionRuta: SesionRuta) {
        val dao = AppDatabase.getInstance(requireContext()).sesionRutaDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteSesionRuta(sesionRuta)
            val updatedSesionesRuta = dao.getAllSesionesRuta()
            withContext(Dispatchers.Main) {
                sesionRutaAdapter.submitList(updatedSesionesRuta)
            }
        }
    }
}