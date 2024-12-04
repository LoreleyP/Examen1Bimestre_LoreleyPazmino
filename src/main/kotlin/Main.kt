import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/*Autor: Loreley Pazmiño*/
// Entidad Pelicula
data class Pelicula(
    val id: Int,
    var nombre: String,
    var fechaEstreno: LocalDate,
    var duracion: Double, // Duración en horas
    var genero: String,
    val actores: MutableList<Actor> = mutableListOf() // Relación Uno a Muchos
)

// Entidad Actor
data class Actor(
    val id: Int,
    var nombre: String,
    var fechaNacimiento: LocalDate,
    var esPrincipal: Boolean, // Actor principal o no
    var salario: Double // Salario del actor
)

// CRUD de Pelicula
class PeliculaService {

    private val peliculas = mutableListOf<Pelicula>()
    private val archivoPeliculas = "peliculas.txt"

    init {
        cargarPeliculasDesdeArchivo()
    }

    fun crearPelicula(pelicula: Pelicula) {
        peliculas.add(pelicula)
        guardarPeliculasEnArchivo()
    }

    fun leerPeliculas(): List<Pelicula> {
        return peliculas
    }

    fun actualizarPelicula(id: Int, peliculaActualizada: Pelicula) {
        val index = peliculas.indexOfFirst { it.id == id }
        if (index != -1) {
            peliculas[index] = peliculaActualizada
            guardarPeliculasEnArchivo()
        }
    }

    fun eliminarPelicula(id: Int) {
        peliculas.removeIf { it.id == id }
        guardarPeliculasEnArchivo()
    }

    private fun guardarPeliculasEnArchivo() {
        val file = File(archivoPeliculas)
        file.writeText("") // Limpiar el archivo

        // Cabecera de la película
        file.appendText("ID | Nombre              | Fecha de Estreno | Duración (hrs) | Género\n")
        file.appendText("---------------------------------------------------------------\n")

        // Ordenamos las películas por ID
        peliculas.sortedBy { it.id }.forEach { pelicula ->
            // Escribimos la película
            file.appendText(
                "${pelicula.id}  | ${
                    formatearString(
                        pelicula.nombre,
                        20
                    )
                } | ${
                    formatearString(
                        pelicula.fechaEstreno.toString(),
                        17
                    )
                } | ${formatearString(pelicula.duracion.toString(), 15)} | ${pelicula.genero}\n"
            )

            // Escribimos los actores, si los hay
            if (pelicula.actores.isNotEmpty()) {
                file.appendText("    Actores:\n")
                file.appendText("    ID | Nombre               | Fecha de Nacimiento | Es Principal | Salario\n")
                file.appendText("    -------------------------------------------------------------\n")
                pelicula.actores.sortedBy { it.id }.forEach { actor ->
                    file.appendText(
                        "    ${actor.id}  | ${
                            formatearString(
                                actor.nombre,
                                20
                            )
                        } | ${
                            formatearString(
                                actor.fechaNacimiento.toString(),
                                19
                            )
                        } | ${actor.esPrincipal}        | ${actor.salario}\n"
                    )
                }
            }
            file.appendText("\n")
        }
    }

    private fun cargarPeliculasDesdeArchivo() {
        val file = File(archivoPeliculas)
        if (file.exists()) {
            var peliculaActual: Pelicula? = null
            file.forEachLine { line ->
                // Ignorar líneas vacías, cabeceras o líneas con solo guiones
                if (line.startsWith("ID |") || line.trim().isEmpty() || line.contains("-")) return@forEachLine

                // Aquí asumimos que cada línea es de una película o actor
                try {
                    // Si la línea tiene la estructura de una película, procesa los datos
                    val datosPelicula = line.split("|").map { it.trim() }
                    if (datosPelicula.size >= 5 && datosPelicula[0].toIntOrNull() != null) {
                        val id = datosPelicula[0].toInt()
                        val nombre = datosPelicula[1]
                        val fechaEstreno = LocalDate.parse(datosPelicula[2])
                        val duracion = datosPelicula[3].toDouble()
                        val genero = datosPelicula[4]

                        peliculaActual = Pelicula(id, nombre, fechaEstreno, duracion, genero)
                        peliculas.add(peliculaActual!!)
                    }
                } catch (e: Exception) {
                    println("Error al procesar la línea: $line, error: ${e.message}")
                }

                // Si la línea contiene información de actores, procese los actores
                if (line.startsWith("    Actores:")) {
                    val actores = mutableListOf<Actor>()
                    file.forEachLine { actorLine ->
                        // Ignorar líneas vacías o guiones
                        if (actorLine.trim().isEmpty() || actorLine.contains("-")) return@forEachLine

                        val datosActor = actorLine.split("|").map { it.trim() }
                        if (datosActor.size >= 5 && datosActor[0].toIntOrNull() != null) {
                            try {
                                val idActor = datosActor[0].toInt()
                                val nombreActor = datosActor[1]
                                val fechaNacimiento = LocalDate.parse(datosActor[2])
                                val esPrincipal = datosActor[3].toBoolean()
                                val salario = datosActor[4].toDouble()

                                val actor = Actor(idActor, nombreActor, fechaNacimiento, esPrincipal, salario)
                                actores.add(actor)
                            } catch (e: Exception) {
                                println("Error al procesar la línea del actor: $actorLine, error: ${e.message}")
                            }
                        }
                    }
                    peliculaActual?.actores?.addAll(actores)
                }
            }
        }

}}

    private fun formatearString(texto: String, longitud: Int): String {
    return if (texto.length >= longitud) {
        texto.substring(0, longitud) // Truncamos el texto si es largo
    } else {
        texto.padEnd(longitud) // Agregamos espacios si es corto
    }
}


// Funciones del menú
fun mostrarMenu() {
    println("====== Menú CRUD de Películas ======")
    println("1. Crear Película")
    println("2. Leer todas las Películas")
    println("3. Actualizar Película")
    println("4. Eliminar Película")
    println("5. Salir")
    println("====================================")
    print("Seleccione una opción: ")
}

fun leerInt(mensaje: String): Int {
    while (true) {
        try {
            print(mensaje)
            return readLine()?.toInt() ?: throw NumberFormatException("Entrada no válida")
        } catch (e: NumberFormatException) {
            println("Error: Debe ingresar un número entero válido.")
        }
    }
}

fun leerDouble(mensaje: String): Double {
    while (true) {
        try {
            print(mensaje)
            return readLine()?.toDouble() ?: throw NumberFormatException("Entrada no válida")
        } catch (e: NumberFormatException) {
            println("Error: Debe ingresar un número válido.")
        }
    }
}

fun leerString(mensaje: String): String {
    print(mensaje)
    return readLine()?.trim() ?: ""
}

fun leerFecha(mensaje: String): LocalDate {
    while (true) {
        try {
            print(mensaje)
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return LocalDate.parse(readLine(), formato)
        } catch (e: Exception) {
            println("Error: Fecha inválida. Use el formato yyyy-MM-dd.")
        }
    }
}

fun mostrarPeliculas(peliculaService: PeliculaService) {
    val peliculas = peliculaService.leerPeliculas()
    if (peliculas.isEmpty()) {
        println("No hay películas registradas.")
    } else {
        println("===== Lista de Películas =====")
        peliculas.forEach { pelicula ->
            println("ID: ${pelicula.id}")
            println("Nombre: ${pelicula.nombre}")
            println("Fecha de Estreno: ${pelicula.fechaEstreno}")
            println("Duración: ${pelicula.duracion} horas")
            println("Género: ${pelicula.genero}")
            println("Actores:")
            if (pelicula.actores.isEmpty()) {
                println("  Ningún actor registrado.")
            } else {
                pelicula.actores.forEach { actor ->
                    println("  - ID: ${actor.id}")
                    println("    Nombre: ${actor.nombre}")
                    println("    Fecha de Nacimiento: ${actor.fechaNacimiento}")
                    println("    Es principal: ${actor.esPrincipal}")
                    println("    Salario: ${actor.salario}")
                }
            }
            println("===================================")
        }
    }
}

// Función principal
fun main() {
    val peliculaService = PeliculaService()

    while (true) {
        mostrarMenu()
        when (leerInt("")) {
            1 -> {
                val id = leerInt("Ingrese el ID de la película: ")
                val nombre = leerString("Ingrese el nombre de la película: ")
                val fechaEstreno = leerFecha("Ingrese la fecha de estreno (yyyy-MM-dd): ")
                val duracion = leerDouble("Ingrese la duración de la película (horas): ")
                val genero = leerString("Ingrese el género de la película: ")

                val actores = mutableListOf<Actor>()
                while (true) {
                    val opcion = leerInt("¿Desea agregar un actor? 1. Sí 2. No: ")
                    if (opcion == 2) break
                    val actorId = leerInt("Ingrese el ID del actor: ")
                    val actorNombre = leerString("Ingrese el nombre del actor: ")
                    val actorFechaNacimiento = leerFecha("Ingrese la fecha de nacimiento del actor (yyyy-MM-dd): ")
                    val esPrincipal = leerInt("¿Es actor principal? 1. Sí 2. No: ") == 1
                    val salario = leerDouble("Ingrese el salario del actor: ")

                    actores.add(Actor(actorId, actorNombre, actorFechaNacimiento, esPrincipal, salario))
                }

                val pelicula = Pelicula(id, nombre, fechaEstreno, duracion, genero, actores)
                peliculaService.crearPelicula(pelicula)
            }

            2 -> {
                mostrarPeliculas(peliculaService)
            }

            3 -> {
                val id = leerInt("Ingrese el ID de la película a actualizar: ")
                val peliculaExistente = peliculaService.leerPeliculas().find { it.id == id }
                if (peliculaExistente != null) {
                    val nombre =
                        leerString("Ingrese el nuevo nombre de la película (actual: ${peliculaExistente.nombre}): ")
                    val fechaEstreno =
                        leerFecha("Ingrese la nueva fecha de estreno (actual: ${peliculaExistente.fechaEstreno}): ")
                    val duracion =
                        leerDouble("Ingrese la nueva duración (actual: ${peliculaExistente.duracion} horas): ")
                    val genero = leerString("Ingrese el nuevo género (actual: ${peliculaExistente.genero}): ")

                    val actores = mutableListOf<Actor>()
                    peliculaExistente.actores.forEach { actor ->
                        actores.add(
                            Actor(
                                actor.id,
                                actor.nombre,
                                actor.fechaNacimiento,
                                actor.esPrincipal,
                                actor.salario
                            )
                        )
                    }

                    val peliculaActualizada = Pelicula(id, nombre, fechaEstreno, duracion, genero, actores)
                    peliculaService.actualizarPelicula(id, peliculaActualizada)
                } else {
                    println("Película no encontrada.")
                }
            }

            4 -> {
                val id = leerInt("Ingrese el ID de la película a eliminar: ")
                peliculaService.eliminarPelicula(id)
            }

            5 -> {
                println("Saliendo...")
                break
            }

            else -> println("Opción inválida.")
        }
    }
}