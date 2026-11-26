package com.shadow.controlacceso.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.enums.TipoAlerta;
import com.shadow.controlacceso.repository.AlertaRepository;

public class JdbcAlertaRepository implements AlertaRepository {

    private final ConexionDB conexionDB;

    public JdbcAlertaRepository(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    @Override
    public Alerta guardar(Alerta a) {
        String sql = "INSERT INTO alerta (tipo, mensaje, fecha_hora, registro_id) VALUES (?, ?, ?, ?)";
        try (Connection cn = conexionDB.abrir();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.tipo().name());
            ps.setString(2, a.mensaje());
            ps.setObject(3, a.fechaHora());
            ps.setLong(4, a.registroId());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return a.conId(claves.getLong(1));
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo guardar la alerta", e);
        }
    }

    @Override
    public List<Alerta> listarRecientes(int limite) {
        String sql = "SELECT id, tipo, mensaje, fecha_hora, registro_id FROM alerta ORDER BY id DESC LIMIT ?";
        try (Connection cn = conexionDB.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<Alerta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new Alerta(
                            rs.getLong("id"),
                            TipoAlerta.valueOf(rs.getString("tipo")),
                            rs.getString("mensaje"),
                            rs.getObject("fecha_hora", LocalDateTime.class),
                            rs.getLong("registro_id")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new DatosAccesoException("No se pudo listar alertas", e);
        }
    }
}
