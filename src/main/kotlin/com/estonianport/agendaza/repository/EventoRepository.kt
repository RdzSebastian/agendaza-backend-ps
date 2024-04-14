package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.*

interface EventoRepository : CrudRepository<Evento, Long>{

/*
    @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento"])
*/
    override fun findAll() : List<Evento>

/*    @EntityGraph(attributePaths = [
        "capacidad",
        "cliente",
        "encargado",
        "tipoEvento"
    ])*/
    override fun findById(id: Long) : Optional<Evento>

    fun findAllByInicioBetweenAndEmpresa(inicio: LocalDateTime, fin: LocalDateTime, empresa: Empresa): List<Evento>

    @Query(value = "select e from Evento e where e.empresa.id = ?1")
    fun eventosByEmpresa(id : Long, pageable : Pageable) : Page<Evento>

    @EntityGraph(attributePaths = ["capacidad", "encargado", "cliente", "tipoEvento.capacidad"])
    fun findAllByEmpresa(empresa: Empresa) : List<Evento>

}