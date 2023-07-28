package com.borja.android.runfit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borja.android.runfit.databinding.FragmentSesionesGuardadasBinding
import com.borja.android.runfit.databinding.ItemSesionRutaBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
class SesionesGuardadasFragment : Fragment() {

    private var _binding: FragmentSesionesGuardadasBinding? = null
    private val binding get() = _binding!!

    private lateinit var sesionesAdapter: SesionesRutaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSesionesGuardadasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sesionesAdapter = SesionesRutaAdapter()

        binding.recyclerViewSesiones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sesionesAdapter
        }

        loadSesionesRuta()
    }

    private fun loadSesionesRuta() {
        val dao = AppDatabase.getInstance(requireContext()).sesionRutaDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sesionesRuta = dao.getAllSesionesRuta().sortedByDescending { it.fecha }
            withContext(Dispatchers.Main) {
                sesionesAdapter.setData(sesionesRuta)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter para el RecyclerView
    private inner class SesionesRutaAdapter : RecyclerView.Adapter<SesionesRutaAdapter.SesionRutaViewHolder>() {
        private var sesionesRutaList: List<SesionRuta> = emptyList()

        fun setData(sesiones: List<SesionRuta>) {
            sesionesRutaList = sesiones
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionRutaViewHolder {
            val itemBinding = ItemSesionRutaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SesionRutaViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: SesionRutaViewHolder, position: Int) {
            val sesionRuta = sesionesRutaList[position]
            holder.bind(sesionRuta)
        }

        override fun getItemCount(): Int = sesionesRutaList.size

        inner class SesionRutaViewHolder(private val itemBinding: ItemSesionRutaBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(sesionRuta: SesionRuta) {
                // Aquí puedes asignar los datos de la sesionRuta a las vistas dentro de itemBinding
                itemBinding.tvFecha.text = sesionRuta.fecha
                itemBinding.tvDistance.text = sesionRuta.distancia.toString()
                itemBinding.tvSpeed.text = sesionRuta.velocidadPromedio.toString()
                itemBinding.tvTime.text = sesionRuta.tiempo.toString()
                itemBinding.tvCalories.text = sesionRuta.calorias.toString()
            }
        }
    }
}*/

class SesionesGuardadasFragment : Fragment() {

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

        sesionRutaAdapter = SesionRutaAdapter()
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
}