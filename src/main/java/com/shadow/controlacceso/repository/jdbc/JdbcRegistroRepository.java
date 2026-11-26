package com.shadow.controlacceso.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.shadow.controlacceso.model.RegistroAcceso;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.repository.RegistroRepository;

public class JdbcRegistroRepository implements RegistroRepository {

    private final ConexionDB conexionDB;

    public JdbcRegistroRepository(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    @Override
    public RegistroAcceso guardar(RegistroAcceso r) {
        String sql = "INSERT INTO registro_acceso (uid_hash, tarjeta_id, zona_id, fecha_hora, resultado) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = conexionDB.abrir();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.uidHash());
            if (r.tarjetaId() == null) {
                ps.setNull(2, Types.BIGINT);   // tarjeta desconocida
            } else {
                ps.setLong(2, r.tarjetaId());
            }
            ps.setLong(3, r.zonaId());
            ps.setObject(4, r.fechaHora());
            ps.setString(5, r.resultado().name());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return r.conId(claves.getLong(1));
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo guardar el registro de acceso", e);
        }
    }

    @Override
    public List<RegistroAcceso> listarRecientes(int limite) {
        String sql = "SELECT id, uid_hash, tarjeta_id, zona_id, fecha_hora, resultado "
                   + "FROM registro_acceso ORDER BY id DESC LIMIT ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegistroAcceso> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo listar registros", e);
        }
    }

    @Override
    public long contarDenegadosDesde(String uidHash, LocalDateTime desde) {
        String sql = "SELECT COUNT(*) FROM registro_acceso "
                   + "WHERE uid_hash = ? AND fecha_hora >= ? AND resultado <> ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uidHash);
            ps.setObject(2, desde);
            ps.setString(3, Resultado.PERMITIDO.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo contar intentos denegados", e);
        }
    }

    private static RegistroAcceso mapear(ResultSet rs) throws SQLException {
        // getLong devuelve 0 si la columna era NULL; wasNull() debe consultarse JUSTO despues
        long valorTarjeta = rs.getLong("tarjeta_id");
        Long tarjetaId = rs.wasNull() ? null : valorTarjeta;
        return new RegistroAcceso(
                rs.getLong("id"),
                rs.getString("uid_hash"),
                tarjetaId,
                rs.getLong("zona_id"),
                rs.getObject("fecha_hora", LocalDateTime.class),
                Resultado.valueOf(rs.getString("resultado")));
    }
}
