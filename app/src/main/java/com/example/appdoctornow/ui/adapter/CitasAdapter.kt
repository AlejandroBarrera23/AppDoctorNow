package com.example.appdoctornow.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Cita
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CitasAdapter(
    private var citas: List<Cita>,
    private val database: AppDatabase, // Pasamos la base de datos al adaptador
    private val onCancelarCita: (Cita) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidad)
        private val tvMedico: TextView = itemView.findViewById(R.id.tvMedico)
        private val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        private val btnCancelarCita: Button = itemView.findViewById(R.id.btnCancelarCita)

        fun bind(cita: Cita) {
            // Mostrar la fecha y hora de la cita
            tvFechaHora.text = "Fecha: ${cita.fecha} - Hora: ${cita.hora}"

            // Obtener el médico y su especialidad desde la base de datos
            CoroutineScope(Dispatchers.IO).launch {
                val medico = database.medicoDao().getMedicoById(cita.idMedico)
                withContext(Dispatchers.Main) {
                    // Mostrar la especialidad y el nombre del médico
                    tvEspecialidad.text = "Especialidad: ${medico?.especialidad}"
                    tvMedico.text = "Médico: ${medico?.nombres} ${medico?.apellidos}"
                }
            }

            // Configurar el botón de cancelar cita
            btnCancelarCita.setOnClickListener {
                onCancelarCita(cita)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(citas[position])
    }

    override fun getItemCount(): Int {
        return citas.size
    }

    fun actualizarLista(nuevasCitas: List<Cita>) {
        this.citas = nuevasCitas
        notifyDataSetChanged()
    }
}