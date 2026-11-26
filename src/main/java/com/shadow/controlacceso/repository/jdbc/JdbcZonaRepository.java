package com.shadow.controlacceso.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Zona;
import com.shadow.controlacceso.model.enums.Nivel;
import com.shadow.controlacceso.repository.ZonaRepository;

public class JdbcZonaRepository implements ZonaRepository {

    private static final String COLUMNAS = "id, codigo, nombre, nivel_requerido";

    private final ConexionDB conexionDB;

    public JdbcZonaRepository(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    @Override
    public Optional<Zona> buscarPorId(long id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM zona WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<Zona> buscarPorCodigo(String codigo) {
        return buscarUno("SELECT " + COLUMNAS + " FROM zona WHERE codigo = ?", ps -> ps.setString(1, codigo));
    }

    @Override
    public List<Zona> listarTodas() {
        String sql = "SELECT " + COLUMNAS + " FROM zona ORDER BY id";
        try (Connection cn = conexionDB.abrir();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Zona> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo listar zonas", e);
        }
    }

    /** Pequena interfaz funcional para poder pasar "como se llenan los ?" como lambda. */
    @FunctionalInterface
    private interface Parametros {
        void aplicar(PreparedStatement ps) throws SQLException;
    }

    private Optional<Zona> buscarUno(String sql, Parametros parametros) {
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            parametros.aplicar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo buscar la zona", e);
        }
    }

    private static Zona mapear(ResultSet rs) throws SQLException {
        return new Zona(
                rs.getLong("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                Nivel.valueOf(rs.getString("nivel_requerido")));
    }
}
