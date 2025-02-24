package com.example.appdoctornow.ui.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Cita
import com.example.appdoctornow.model.Medico
import com.example.appdoctornow.model.Usuario
import com.example.appdoctornow.ui.adapter.CitasAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardPacienteActivity : AppCompatActivity() {

    private lateinit var tvSaludo: TextView
    private lateinit var spinnerEspecialidades: Spinner
    private lateinit var spinnerMedicos: Spinner
    private lateinit var etFecha: EditText
    private lateinit var spinnerHoras: Spinner
    private lateinit var btnAgendarCita: Button
    private lateinit var rvCitas: RecyclerView
    private lateinit var btnCerrarSesion: Button

    private lateinit var database: AppDatabase
    private lateinit var usuario: Usuario
    private lateinit var citasAdapter: CitasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_paciente)

        // Inicializar vistas
        tvSaludo = findViewById(R.id.tvSaludo)
        spinnerEspecialidades = findViewById(R.id.spinnerEspecialidades)
        spinnerMedicos = findViewById(R.id.spinnerMedicos)
        etFecha = findViewById(R.id.etFecha)
        spinnerHoras = findViewById(R.id.spinnerHoras)
        btnAgendarCita = findViewById(R.id.btnAgendarCita)
        rvCitas = findViewById(R.id.rvCitas)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Deshabilitar controles al inicio
        spinnerMedicos.isEnabled = false
        etFecha.isEnabled = false
        spinnerHoras.isEnabled = false
        btnAgendarCita.isEnabled = false

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Obtener el usuario actual usando corrutinas
        lifecycleScope.launch {
            usuario = obtenerUsuarioActual()
            tvSaludo.text = "¡Hola, ${usuario.nombres} ${usuario.apellidos}!"
            cargarCitasConfirmadas() // Llamar aquí, después de inicializar `usuario`
        }

        // Configurar el botón de cerrar sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        // Configurar el selector de fecha
        etFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Cargar especialidades disponibles
        cargarEspecialidades()

        // Configurar el botón de agendar cita
        btnAgendarCita.setOnClickListener {
            if (validarCampos()) {
                agendarCita()
            } else {
                Toast.makeText(this, "Seleccione todas las opciones", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar la lista de citas
        citasAdapter = CitasAdapter(mutableListOf(), database) { cita ->
            confirmarCancelarCita(cita)
        }
        rvCitas.layoutManager = LinearLayoutManager(this)
        rvCitas.adapter = citasAdapter

        // Configurar listeners para actualizar los horarios y el estado del botón
        configurarListeners()
    }

    private suspend fun obtenerUsuarioActual(): Usuario {
        // Obtener el ID del usuario desde SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1) // -1 es un valor por defecto si no existe

        if (userId == -1) {
            // Si no hay un ID de usuario, redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            throw IllegalStateException("Usuario no encontrado")
        }

        // Obtener el usuario desde la base de datos
        return database.usuarioDao().getUsuarioById(userId) ?: throw IllegalStateException("Usuario no encontrado")
    }

    private fun cerrarSesion() {
        // Crear un diálogo de confirmación
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Está seguro de que desea cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                // Limpiar SharedPreferences
                val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear() // Borrar todos los datos
                editor.apply()

                // Redirigir a la pantalla de login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Cerrar la actividad actual
            }
            .setNegativeButton("No", null) // No hacer nada si el usuario selecciona "No"
            .create()

        // Mostrar el diálogo
        dialog.show()
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                // Crear un nuevo Calendar con la fecha seleccionada
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val fechaSeleccionada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.time)
                etFecha.setText(fechaSeleccionada)
                spinnerHoras.isEnabled = true // Habilitar el Spinner de horarios
                cargarHorasDisponibles()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000 // Evitar fechas pasadas
        datePicker.show()
    }

    private fun cargarEspecialidades() {
        lifecycleScope.launch {
            // Obtener todas las especialidades únicas desde la base de datos
            val especialidades = database.medicoDao().getEspecialidades()

            // Agregar un placeholder al inicio de la lista
            val especialidadesConPlaceholder = mutableListOf("Seleccione una especialidad")
            especialidadesConPlaceholder.addAll(especialidades)

            // Configurar el adaptador para el spinner de especialidades
            val adapter = ArrayAdapter(this@DashboardPacienteActivity, android.R.layout.simple_spinner_item, especialidadesConPlaceholder)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEspecialidades.adapter = adapter

            // Configurar el listener para cargar médicos cuando se seleccione una especialidad
            spinnerEspecialidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) { // Ignorar el placeholder
                        val especialidadSeleccionada = especialidades[position - 1]
                        cargarMedicos(especialidadSeleccionada)
                        spinnerMedicos.isEnabled = true // Habilitar el Spinner de médicos
                    } else {
                        // Restablecer placeholders y bloquear controles
                        limpiarYBloquearControles()
                    }
                    verificarCamposSeleccionados()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    verificarCamposSeleccionados()
                }
            }
        }
    }

    private fun cargarMedicos(especialidad: String) {
        lifecycleScope.launch {
            // Obtener los médicos de la especialidad seleccionada
            val medicos = database.medicoDao().getMedicosPorEspecialidad(especialidad)

            // Agregar un placeholder al inicio de la lista
            val medicosConPlaceholder = mutableListOf<Any>("Seleccione un médico")
            medicosConPlaceholder.addAll(medicos)

            // Configurar el adaptador para el spinner de médicos
            val adapter = object : ArrayAdapter<Any>(this@DashboardPacienteActivity, android.R.layout.simple_spinner_item, medicosConPlaceholder) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    val item = getItem(position)
                    textView.text = when (item) {
                        is Medico -> item.toString() // Mostrar el nombre y apellido del médico
                        else -> item.toString() // Mostrar el placeholder
                    }
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    val item = getItem(position)
                    textView.text = when (item) {
                        is Medico -> item.toString() // Mostrar el nombre y apellido del médico
                        else -> item.toString() // Mostrar el placeholder
                    }
                    return view
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMedicos.adapter = adapter

            // Configurar el listener para habilitar el selector de fecha cuando se seleccione un médico
            spinnerMedicos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) { // Ignorar el placeholder
                        etFecha.isEnabled = true // Habilitar el selector de fecha
                    } else {
                        etFecha.isEnabled = false // Deshabilitar el selector de fecha
                    }
                    verificarCamposSeleccionados()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    verificarCamposSeleccionados()
                }
            }
        }
    }

    private fun cargarHorasDisponibles() {
        val fechaSeleccionada = etFecha.text.toString()
        if (fechaSeleccionada.isEmpty()) {
            spinnerHoras.setSelection(0) // Restablecer placeholder "Seleccione un horario"
            spinnerHoras.isEnabled = false // Deshabilitar el Spinner de horarios
            return
        }

        val medico = spinnerMedicos.selectedItem
        if (medico !is Medico) { // Si no es un Medico, es el placeholder
            spinnerHoras.setSelection(0) // Restablecer placeholder "Seleccione un horario"
            spinnerHoras.isEnabled = false // Deshabilitar el Spinner de horarios
            return
        }

        lifecycleScope.launch {
            // Obtener las horas ocupadas para la fecha y el médico seleccionado
            val horasOcupadas = database.citaDao().getHorasOcupadas(fechaSeleccionada, medico.id)

            // Definir todos los horarios disponibles con un placeholder
            val todasLasHoras = listOf("Seleccione un horario", "09:00", "10:00", "11:00", "12:00")

            // Filtrar las horas disponibles
            val horasDisponibles = todasLasHoras.filter { it == "Seleccione un horario" || !horasOcupadas.contains(it) }

            // Actualizar el spinner de horas
            val adapter = ArrayAdapter(this@DashboardPacienteActivity, android.R.layout.simple_spinner_item, horasDisponibles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerHoras.adapter = adapter

            // Habilitar el Spinner de horarios después de cargar las horas
            spinnerHoras.isEnabled = true
        }
    }

    private fun agendarCita() {
        val medico = spinnerMedicos.selectedItem as? Medico
        val fecha = etFecha.text.toString()
        val hora = spinnerHoras.selectedItem as? String

        if (medico == null || fecha.isEmpty() || hora == null || hora == "Seleccione un horario") {
            Toast.makeText(this, "Seleccione todas las opciones", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que la fecha y hora sean futuras
        val fechaHoraSeleccionada = "$fecha $hora"
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHoraActual = Calendar.getInstance().time

        try {
            val fechaSeleccionada = formato.parse(fechaHoraSeleccionada)
            if (fechaSeleccionada.before(fechaHoraActual)) {
                Toast.makeText(this, "No puede agendar citas en fechas u horas pasadas", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Formato de fecha u hora inválido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Verificar si ya tiene una cita para la misma especialidad en esta fecha
            val citasExistentes = database.citaDao().getCitasByUsuarioFechaEspecialidad(
                usuario.id,
                fecha,
                medico.especialidad
            )

            if (citasExistentes.isNotEmpty()) {
                Toast.makeText(
                    this@DashboardPacienteActivity,
                    "Ya tiene una cita para ${medico.especialidad} en esta fecha",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Verificar si la hora está ocupada para el médico seleccionado
            val citaMedicoExistente = database.citaDao().getCitaByFechaHora(fecha, hora)
            if (citaMedicoExistente != null) {
                Toast.makeText(this@DashboardPacienteActivity, "La hora seleccionada ya está ocupada", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Crear la cita
            val cita = Cita(
                idUsuario = usuario.id,
                idMedico = medico.id,
                fecha = fecha,
                hora = hora,
                estado = "Confirmada"
            )

            // Guardar la cita en la base de datos
            database.citaDao().insert(cita)
            Toast.makeText(this@DashboardPacienteActivity, "Cita agendada exitosamente", Toast.LENGTH_SHORT).show()

            // Limpiar campos después de agendar
            limpiarCampos()

            // Actualizar la lista de citas
            cargarCitasConfirmadas()
        }
    }

    private fun limpiarCampos() {
        // Restablecer placeholders
        spinnerEspecialidades.setSelection(0) // Restablecer placeholder "Seleccione una especialidad"
        spinnerMedicos.setSelection(0) // Restablecer placeholder "Seleccione un médico"
        etFecha.text.clear() // Limpiar el campo de fecha
        spinnerHoras.setSelection(0) // Restablecer placeholder "Seleccione un horario"

        // Deshabilitar controles después de restablecer placeholders
        spinnerMedicos.isEnabled = false
        etFecha.isEnabled = false
        spinnerHoras.isEnabled = false
        btnAgendarCita.isEnabled = false
    }

    private fun limpiarYBloquearControles() {
        // Restablecer placeholders
        spinnerMedicos.setSelection(0) // Restablecer placeholder "Seleccione un médico"
        etFecha.text.clear() // Limpiar el campo de fecha
        spinnerHoras.setSelection(0) // Restablecer placeholder "Seleccione un horario"

        // Deshabilitar controles después de restablecer placeholders
        spinnerMedicos.isEnabled = false
        etFecha.isEnabled = false
        spinnerHoras.isEnabled = false
    }

    private fun cargarCitasConfirmadas() {
        if (!::usuario.isInitialized) return // Verificar si `usuario` está inicializado
        lifecycleScope.launch {
            val citas = database.citaDao().getCitasByUsuario(usuario.id)
            citasAdapter.actualizarLista(citas)
        }
    }

    private fun confirmarCancelarCita(cita: Cita) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Está seguro de que desea cancelar esta cita?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    cita.estado = "Cancelada"
                    database.citaDao().update(cita)
                    Toast.makeText(this@DashboardPacienteActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    cargarCitasConfirmadas()
                    cargarHorasDisponibles() // Volver a cargar las horas disponibles
                }
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    private fun configurarListeners() {
        // Configurar listeners para actualizar los horarios y el estado del botón
        spinnerMedicos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Ignorar el placeholder
                    etFecha.isEnabled = true // Habilitar el selector de fecha
                } else {
                    // Restablecer placeholders y bloquear controles
                    etFecha.text.clear() // Limpiar el campo de fecha
                    spinnerHoras.setSelection(0) // Restablecer placeholder "Seleccione un horario"
                    etFecha.isEnabled = false // Deshabilitar el selector de fecha
                    spinnerHoras.isEnabled = false // Deshabilitar el Spinner de horarios
                }
                verificarCamposSeleccionados()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                verificarCamposSeleccionados()
            }
        }

        etFecha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                cargarHorasDisponibles()
                verificarCamposSeleccionados()
            }
        })

        spinnerHoras.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                verificarCamposSeleccionados()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                verificarCamposSeleccionados()
            }
        }
    }

    private fun validarCampos(): Boolean {
        // Verificar si se seleccionó una especialidad válida
        val especialidadSeleccionada = spinnerEspecialidades.selectedItem as? String
        if (especialidadSeleccionada == null || especialidadSeleccionada == "Seleccione una especialidad") {
            return false
        }

        // Verificar si se seleccionó un médico válido
        val medicoSeleccionado = spinnerMedicos.selectedItem
        if (medicoSeleccionado !is Medico) { // Si no es un Medico, es el placeholder
            return false
        }

        // Verificar si se seleccionó una fecha
        val fechaSeleccionada = etFecha.text.toString()
        if (fechaSeleccionada.isEmpty()) {
            return false
        }

        // Verificar si se seleccionó un horario válido
        val horaSeleccionada = spinnerHoras.selectedItem as? String
        if (horaSeleccionada == null || horaSeleccionada == "Seleccione un horario") {
            return false
        }

        // Si todos los campos son válidos, retornar true
        return true
    }

    private fun verificarCamposSeleccionados() {
        btnAgendarCita.isEnabled = validarCampos()
    }
}