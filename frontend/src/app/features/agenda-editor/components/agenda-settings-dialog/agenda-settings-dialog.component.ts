import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  TipoAgenda
} from '../../../../core/models/agenda.models';

import {
  AgendaDraft
} from '../../models/agenda-draft.models';


export interface AgendaSettingsValue {

  titulo: string;

  tipoAgenda: TipoAgenda;

  fechaInicio: string;

  fechaFin: string | null;
}


@Component({
  selector: 'app-agenda-settings-dialog',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './agenda-settings-dialog.component.html',
  styleUrl: './agenda-settings-dialog.component.scss'
})
export class AgendaSettingsDialogComponent
  implements OnChanges {

  @Input({ required: true })
  agenda!: AgendaDraft;


  @Output()
  saved =
    new EventEmitter<AgendaSettingsValue>();


  @Output()
  cancelled =
    new EventEmitter<void>();


  form: AgendaSettingsValue = {

    titulo: '',

    tipoAgenda: 'DIARIA',

    fechaInicio: '',

    fechaFin: null
  };


  validationMessage:
    string | null = null;


  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['agenda'] &&
      this.agenda
    ) {

      this.form = {

        titulo:
          this.agenda.titulo,

        tipoAgenda:
          this.agenda.tipoAgenda,

        fechaInicio:
          this.agenda.fechaInicio,

        fechaFin:
          this.agenda.fechaFin
      };

      this.validationMessage = null;
    }
  }


  onTypeChange(): void {

    if (
      this.form.tipoAgenda === 'DIARIA'
    ) {

      this.form.fechaFin = null;
    }
  }


  onSave(): void {

    const titulo =
      this.form.titulo.trim();


    if (!titulo) {

      this.validationMessage =
        'El título es obligatorio.';

      return;
    }


    if (!this.form.fechaInicio) {

      this.validationMessage =
        'La fecha de inicio es obligatoria.';

      return;
    }


    if (
      this.form.tipoAgenda === 'SEMANAL'
    ) {

      if (!this.form.fechaFin) {

        this.validationMessage =
          'La fecha de fin es obligatoria para una agenda semanal.';

        return;
      }


      if (
        this.form.fechaFin <
          this.form.fechaInicio
      ) {

        this.validationMessage =
          'La fecha de fin no puede ser anterior a la fecha de inicio.';

        return;
      }


      if (
        this.form.fechaFin ===
          this.form.fechaInicio
      ) {

        this.validationMessage =
          'Una agenda semanal debe contener al menos dos días.';

        return;
      }
    }


    this.saved.emit({

      titulo,

      tipoAgenda:
        this.form.tipoAgenda,

      fechaInicio:
        this.form.fechaInicio,

      fechaFin:
        this.form.tipoAgenda === 'SEMANAL'
          ? this.form.fechaFin
          : null
    });
  }


  onCancel(): void {
    this.cancelled.emit();
  }
}