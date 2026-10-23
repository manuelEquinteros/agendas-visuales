package com.agendasvisuales.agenda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pictograma")
public class Pictograma {

	@Id
	@Column(name = "id_arasaac")
	private Long idArasaac;

	@Column(name = "descripcion", nullable = false, length = 200)
	private String descripcion;

	@Column(name = "url_imagen", nullable = false, length = 500)
	private String urlImagen;

	protected Pictograma() {
	}

	public Pictograma(Long idArasaac, String descripcion, String urlImagen) {
		this.idArasaac = idArasaac;
		this.descripcion = descripcion;
		this.urlImagen = urlImagen;
	}

	public Long getIdArasaac() {
		return idArasaac;
	}

	public void setIdArasaac(Long idArasaac) {
		this.idArasaac = idArasaac;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getUrlImagen() {
		return urlImagen;
	}

	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}
}