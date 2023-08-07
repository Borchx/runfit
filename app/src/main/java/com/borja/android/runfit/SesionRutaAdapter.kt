package com.borja.android.runfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.borja.android.runfit.databinding.ItemSesionRutaBinding
import java.util.concurrent.TimeUnit

class SesionRutaAdapter(private val onDeleteClickListener: OnDeleteClickListener) :
    ListAdapter<SesionRuta, SesionRutaAdapter.SesionRutaViewHolder>(SesionRutaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionRutaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sesion_ruta, parent, false)
        return SesionRutaViewHolder(view, onDeleteClickListener)
    }

    override fun onBindViewHolder(holder: SesionRutaViewHolder, position: Int) {
        val sesionRuta = getItem(position)
        holder.bind(sesionRuta)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(sesionRuta: SesionRuta)
    }

    inner class SesionRutaViewHolder(
        itemView: View,
        private val onDeleteClickListener: OnDeleteClickListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvId: TextView = itemView.findViewById(R.id.tvId)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvDistancia: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvVelocidad: TextView = itemView.findViewById(R.id.tvSpeed)
        private val tvTiempo: TextView = itemView.findViewById(R.id.tvTime)
        private val tvCalorias: TextView = itemView.findViewById(R.id.tvCalories)

        fun bind(sesionRuta: SesionRuta) {
            tvId.text = "ID: ${sesionRuta.id}"
            tvFecha.text = "Fecha: ${sesionRuta.fecha} - Hora: ${sesionRuta.hora}"
            tvDistancia.text = "Distancia: ${sesionRuta.distancia} Metros"
            tvVelocidad.text = "Velocidad: ${formatSpeed(sesionRuta.velocidadPromedio)}\" Km/h"
            tvTiempo.text = "Tiempo: ${formatTime(sesionRuta.tiempo)}"
            tvCalorias.text = "Calorías: ${sesionRuta.calorias} Kcal"
        }

        private fun formatTime(timeInSeconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(timeInSeconds)
            val minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds) % 60
            val seconds = timeInSeconds % 60

            return String.format("%02dh : %02dm : %02ds", hours, minutes, seconds)
        }

        private fun formatSpeed(speed: Double): String {
            return String.format("%.2f", speed)
        }

        init {
            itemView.findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val sesionRuta = getItem(position)
                    onDeleteClickListener.onDeleteClick(sesionRuta)
                }
            }
        }
    }

    class SesionRutaDiffCallback : DiffUtil.ItemCallback<SesionRuta>() {
        override fun areItemsTheSame(oldItem: SesionRuta, newItem: SesionRuta): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SesionRuta, newItem: SesionRuta): Boolean {
            return oldItem == newItem
        }
    }
}
