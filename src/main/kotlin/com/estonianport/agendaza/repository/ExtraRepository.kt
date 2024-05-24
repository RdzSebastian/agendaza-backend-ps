package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ExtraRepository : CrudRepository<Extra, Long>{

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1")
    fun cantidadExtras(id : Long) : Int

    @Query("SELECT COUNT(e) FROM Extra e WHERE e.empresa.id = ?1 AND e.nombre ILIKE %?2%")
    fun cantidadExtrasFiltrados(id : Long, buscar: String) : Int

    @Query(value = "select e from Extra e where e.empresa.id = ?1 AND e.fechaBaja IS NULL")
    fun findAll(id: Long,  pageable : Pageable) : Page<Extra>

    @Query(value = "SELECT e FROM Extra e WHERE e.empresa.id = ?1 AND e.nombre ILIKE %?2%")
    fun extrasByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Extra>
}

