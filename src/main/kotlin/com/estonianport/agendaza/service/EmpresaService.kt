package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.dto.CantidadesPanelAdmin
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.repository.EmpresaRepository
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.dto.PagoDto
import com.estonianport.agendaza.dto.UsuarioAbmDto
import com.estonianport.agendaza.repository.EventoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var empresaRepository: EmpresaRepository

    @Autowired
    lateinit var eventoRepository: EventoRepository

    override val dao: CrudRepository<Empresa, Long>
        get() = empresaRepository

    fun findById(id : Long): Empresa {
        return empresaRepository.findById(id).get()
    }

    fun findEmpresaById(id : Long): Empresa {
        return empresaRepository.findEmpresaById(id).get()
    }

    fun getEmpresaListaPagoById(id : Long): Empresa{
        return empresaRepository.getEmpresaListaPagoById(id).get()
    }

    fun getAllEventosByEmpresaId(empresa : Empresa): List<EventoDto> {
        return empresa.listaEvento.filter{ it.fechaBaja == null }
            .map { evento ->
            evento.toDto()
        }.sortedByDescending { it.inicio }
    }

    fun getAllEventoByEmpresaId(id: Long, pageNumber : Int): List<EventoDto> {
        return eventoRepository.eventosByEmpresa(id, PageRequest.of(pageNumber,10)).filter{ it.fechaBaja == null }
            .map { evento ->
                evento.toDto()
            }.sortedByDescending { it.inicio }
    }
    fun getAllEventoByFilterName(id : Long, pageNumber : Int, buscar: String): List<EventoDto>{
        return eventoRepository.eventosByNombre(id, buscar, PageRequest.of(pageNumber,10)).filter{ it.fechaBaja == null }
            .map { evento ->
                evento.toDto()
            }.sortedByDescending { it.inicio }
    }
    fun getAllPagoByEmpresaId(empresa : Empresa): List<PagoDto> {
        return empresa.listaEvento.flatMap { evento ->
            evento.listaPago.filter {
                it.fechaBaja == null }.map { pago ->
                pago.toDTO()
            }
        }.sortedByDescending { it.id }
    }

    fun getAllUsuariosByEmpresaId(empresa: Empresa): List<UsuarioAbmDto> {
        return empresa.listaEmpleados.map {
            UsuarioAbmDto(it.usuario.id, it.usuario.nombre, it.usuario.apellido, it.usuario.username)
        }
    }

    fun getAllCantidadesForPanelAdminByEmpresaId(id: Long): CantidadesPanelAdmin {
        return empresaRepository.getAllCantidadesForPanelAdminByEmpresaId(id)
    }


}