package net.rivergod.sec.seoulrnd.android.menu.dto

data class DayCuisionsDTO(
    var date: String?,
    // Using LinkedHashMap to preserve insertion order like the original HashMap might,
    // though for DTO purposes, order from a HashMap isn't usually guaranteed or relied upon.
    // Switched to var to allow modification by addCuisine.
    var cuisines: MutableMap<String, CuisineDTO> = mutableMapOf()
) {

    // Constructor to match Java's default constructor behavior if needed,
    // though data classes get a primary constructor.
    // The default parameter for cuisines achieves a similar effect for default instantiation.
    // constructor() : this(null, mutableMapOf())

    fun getCuisines(): List<CuisineDTO> {
        return cuisines.values.toList()
    }

    fun addCuisine(id: String, cuisine: CuisineDTO): Boolean {
        return cuisines.put(id, cuisine) != null
    }

    fun getCuisineById(id: String): CuisineDTO? {
        return cuisines[id]
    }
}
