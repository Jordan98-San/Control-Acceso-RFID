package com.shadow.controlacceso.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Trabajador;
import com.shadow.controlacceso.model.enums.Nivel;
import com.shadow.controlacceso.model.enums.Rol;
import com.shadow.controlacceso.repository.TrabajadorRepository;

public class JdbcTrabajadorRepository implements TrabajadorRepository {

    private static final String COLUMNAS = "id, cedula, nombre, rol, nivel_acceso";

    private final ConexionDB conexionDB;

    public JdbcTrabajadorRepository(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    @Override
    public Optional<Trabajador> buscarPorId(long id) {
        String sql = "SELECT " + COLUMNAS + " FROM trabajador WHERE id = ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo buscar el trabajador " + id, e);
        }
    }

    @Override
    public List<Trabajador> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM trabajador ORDER BY nombre";
        try (Connection cn = conexionDB.abrir();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Trabajador> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo listar trabajadores", e);
        }
    }

    /** Convierte una fila del ResultSet en un objeto del modelo. */
    private static Trabajador mapear(ResultSet rs) throws SQLException {
        return new Trabajador(
                rs.getLong("id"),
                rs.getString("cedula"),
                rs.getString("nombre"),
                Rol.valueOf(rs.getString("rol")),
                Nivel.valueOf(rs.getString("nivel_acceso")));
    }
}
