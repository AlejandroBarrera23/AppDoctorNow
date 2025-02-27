package com.example.appdoctornow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Cita
import kotlinx.coroutines.launch

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var calendarView: CustomCalendarView
    private lateinit var rvCitas: RecyclerView
    private lateinit var citasAdapter: CitasAdminAdapter
    private lateinit var btnAgendarCita: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        // Inicializar vistas
        calendarView = findViewById(R.id.calendarView)
        rvCitas = findViewById(R.id.rvCitas)
        btnAgendarCita = findViewById(R.id.btnAgendarCita)

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Configurar el calendario
        configurarCalendario()

        // Configurar la lista de citas
        citasAdapter = CitasAdminAdapter(mutableListOf()) { cita ->
            mostrarDialogoEditarCancelarCita(cita)
        }
        rvCitas.layoutManager = LinearLayoutManager(this)
        rvCitas.adapter = citasAdapter

        // Botón para agendar cita
        btnAgendarCita.setOnClickListener {
            val intent = Intent(this, AgendarCitaAdminActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configurarCalendario() {
        calendarView.setOnDateChangeListener { _, year, month, day ->
            val fechaSeleccionada = "$day/${month + 1}/$year"
            cargarCitasPorFecha(fechaSeleccionada)
        }
    }

    private fun cargarCitasPorFecha(fecha: String) {
        lifecycleScope.launch {
            val citas = database.citaDao().getCitasByUsuarioFechaEspecialidad(-1, fecha, "") // -1 para obtener todas las citas
            citasAdapter.actualizarLista(citas)
        }
    }

    private fun mostrarDialogoEditarCancelarCita(cita: Cita) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Opciones de Cita")
            .setMessage("¿Qué desea hacer con esta cita?")
            .setPositiveButton("Editar") { _, _ ->
                val intent = Intent(this, EditarCitaActivity::class.java).apply {
                    putExtra("cita_id", cita.id)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                lifecycleScope.launch {
                    cita.estado = "Cancelada"
                    database.citaDao().update(cita)
                    cargarCitasPorFecha(cita.fecha)
                }
            }
            .setNeutralButton("Cerrar", null)
            .create()
        dialog.show()
    }
}