package com.agendasvisuales.agenda.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agenda_visual")
public class AgendaVisual {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_agenda")
	private Long id;

	@Column(name = "titulo", nullable = false, length = 150)
	private String titulo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_agenda", nullable = false, length = 20)
	private TipoAgendaEnum tipoAgenda;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDate fechaFin;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_modificacion", nullable = false)
	private LocalDateTime fechaModificacion;

	@OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden ASC")
	private List<DiaAgenda> dias = new ArrayList<>();

	protected AgendaVisual() {
	}

	public AgendaVisual(String titulo, TipoAgendaEnum tipoAgenda, LocalDate fechaInicio, LocalDate fechaFin) {
		this.titulo = titulo;
		this.tipoAgenda = tipoAgenda;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	@PrePersist
	protected void prePersist() {
		LocalDateTime ahora = LocalDateTime.now();
		this.fechaCreacion = ahora;
		this.fechaModificacion = ahora;
	}

	@PreUpdate
	protected void preUpdate() {
		this.fechaModificacion = LocalDateTime.now();
	}

	public void addDia(DiaAgenda dia) {
		dias.add(dia);
		dia.setAgenda(this);
	}

	public void removeDia(DiaAgenda dia) {
		dias.remove(dia);
		dia.setAgenda(null);
	}

	public Long getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public TipoAgendaEnum getTipoAgenda() {
		return tipoAgenda;
	}

	public void setTipoAgenda(TipoAgendaEnum tipoAgenda) {
		this.tipoAgenda = tipoAgenda;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public LocalDateTime getFechaModificacion() {
		return fechaModificacion;
	}

	public List<DiaAgenda> getDias() {
		return dias;
	}
}