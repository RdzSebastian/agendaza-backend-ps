package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsuarioRepository : CrudRepository<Usuario, Long> {

    //@EntityGraph(attributePaths = ["listaEventosContratados", "listaCargo"])
    override fun findAll() : List<Usuario>

    //@EntityGraph(attributePaths = ["listaCargo"])
    fun getByUsername(username: String): Usuario
    @Query("SELECT c.usuario FROM Cargo c where c.empresa.id = ?1")
    fun findAll(id: Long, pageable: PageRequest) : Page<Usuario>

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.empresa.id = ?1")
    fun cantidadDeUsuarios(id : Long) : Int
    fun findOneByUsername(username: String): Usuario?

    fun getUsuarioByEmail(email : String): Usuario?

    fun getUsuarioByCelular(celular : Long) : Usuario?

    //@EntityGraph(attributePaths = ["listaCargo"])
    override fun findById(id: Long) : Optional<Usuario>
}