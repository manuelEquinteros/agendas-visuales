package com.agendasvisuales.agenda.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dia_agenda")
public class DiaAgenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_dia")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_agenda", nullable = false)
	private AgendaVisual agenda;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;

	@Column(name = "orden", nullable = false)
	private Integer orden;

	@OneToMany(mappedBy = "dia", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden ASC")
	private List<ActividadAgenda> actividades = new ArrayList<>();

	protected DiaAgenda() {
	}

	public DiaAgenda(LocalDate fecha, Integer orden) {
		this.fecha = fecha;
		this.orden = orden;
	}

	public void addActividad(ActividadAgenda actividad) {
		actividades.add(actividad);
		actividad.setDia(this);
	}

	public void removeActividad(ActividadAgenda actividad) {
		actividades.remove(actividad);
		actividad.setDia(null);
	}

	void setAgenda(AgendaVisual agenda) {
		this.agenda = agenda;
	}

	public Long getId() {
		return id;
	}

	public AgendaVisual getAgenda() {
		return agenda;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public List<ActividadAgenda> getActividades() {
		return actividades;
	}
}