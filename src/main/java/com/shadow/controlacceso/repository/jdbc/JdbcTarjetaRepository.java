package com.shadow.controlacceso.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.TarjetaRFID;
import com.shadow.controlacceso.repository.TarjetaRepository;

public class JdbcTarjetaRepository implements TarjetaRepository {

    private static final String COLUMNAS = "id, uid_hash, trabajador_id, activa, fecha_emision";

    private final ConexionDB conexionDB;

    public JdbcTarjetaRepository(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    @Override
    public Optional<TarjetaRFID> buscarPorUidHash(String uidHash) {
        String sql = "SELECT " + COLUMNAS + " FROM tarjeta_rfid WHERE uid_hash = ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uidHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo buscar la tarjeta", e);
        }
    }

    @Override
    public List<TarjetaRFID> listarTodas() {
        String sql = "SELECT " + COLUMNAS + " FROM tarjeta_rfid ORDER BY id";
        try (Connection cn = conexionDB.abrir();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TarjetaRFID> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo listar tarjetas", e);
        }
    }

    @Override
    public void actualizarEstado(TarjetaRFID tarjeta) {
        String sql = "UPDATE tarjeta_rfid SET activa = ? WHERE id = ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, tarjeta.isActiva());
            ps.setLong(2, tarjeta.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo actualizar la tarjeta " + tarjeta.getId(), e);
        }
    }

    private static TarjetaRFID mapear(ResultSet rs) throws SQLException {
        return new TarjetaRFID(
                rs.getLong("id"),
                rs.getString("uid_hash"),
                rs.getLong("trabajador_id"),
                rs.getBoolean("activa"),
                rs.getObject("fecha_emision", LocalDate.class));
    }
}
