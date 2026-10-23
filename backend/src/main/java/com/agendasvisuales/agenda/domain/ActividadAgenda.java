package com.agendasvisuales.agenda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "actividad_agenda")
public class ActividadAgenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_actividad")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_dia", nullable = false)
	private DiaAgenda dia;

	@Column(name = "titulo", nullable = false, length = 150)
	private String titulo;

	@Column(name = "descripcion", length = 500)
	private String descripcion;

	@Column(name = "orden", nullable = false)
	private Integer orden;

	@Column(name = "hora_inicio")
	private LocalTime horaInicio;

	@Column(name = "hora_fin")
	private LocalTime horaFin;

	@Column(name = "acompaniante", length = 150)
	private String acompaniante;

	@Column(name = "concepto_visual", nullable = false, length = 150)
	private String conceptoVisual;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_arasaac")
	private Pictograma pictograma;

	protected ActividadAgenda() {
	}

	public ActividadAgenda(String titulo, String descripcion, Integer orden, LocalTime horaInicio, LocalTime horaFin,
			String acompaniante, String conceptoVisual) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.orden = orden;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.acompaniante = acompaniante;
		this.conceptoVisual = conceptoVisual;
	}

	void setDia(DiaAgenda dia) {
		this.dia = dia;
	}

	public Long getId() {
		return id;
	}

	public DiaAgenda getDia() {
		return dia;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	public String getAcompaniante() {
		return acompaniante;
	}

	public void setAcompaniante(String acompaniante) {
		this.acompaniante = acompaniante;
	}

	public String getConceptoVisual() {
		return conceptoVisual;
	}

	public void setConceptoVisual(String conceptoVisual) {
		this.conceptoVisual = conceptoVisual;
	}

	public Pictograma getPictograma() {
		return pictograma;
	}

	public void setPictograma(Pictograma pictograma) {
		this.pictograma = pictograma;
	}
}