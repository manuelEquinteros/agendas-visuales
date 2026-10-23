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
  ActividadAgendaDraft
} from '../../models/agenda-draft.models';


export interface ActivityEditValue {
  titulo: string;
  descripcion: string | null;
  horaInicio: string | null;
  horaFin: string | null;
  acompaniante: string | null;
}


@Component({
  selector: 'app-activity-edit-dialog',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './activity-edit-dialog.component.html',
  styleUrl: './activity-edit-dialog.component.scss'
})
export class ActivityEditDialogComponent
  implements OnChanges {

  @Input({ required: true })
  actividad!: ActividadAgendaDraft;


  @Output()
  saved =
    new EventEmitter<ActivityEditValue>();


  @Output()
  cancelled =
    new EventEmitter<void>();


  form: ActivityEditValue = {
    titulo: '',
    descripcion: null,
    horaInicio: null,
    horaFin: null,
    acompaniante: null
  };


  validationMessage:
    string | null = null;


  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['actividad'] &&
      this.actividad
    ) {

      this.form = {
        titulo:
          this.actividad.titulo,

        descripcion:
          this.actividad.descripcion,

        horaInicio:
          this.actividad.horaInicio
            ?.substring(0, 5)
            ?? null,

        horaFin:
          this.actividad.horaFin
            ?.substring(0, 5)
            ?? null,

        acompaniante:
          this.actividad.acompaniante
      };

      this.validationMessage = null;
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


    if (
      this.form.horaInicio &&
      this.form.horaFin &&
      this.form.horaFin <
        this.form.horaInicio
    ) {

      this.validationMessage =
        'La hora de fin no puede ser anterior a la hora de inicio.';

      return;
    }


    this.saved.emit({

      titulo,

      descripcion:
        this.normalizeOptionalText(
          this.form.descripcion
        ),

      horaInicio:
        this.form.horaInicio || null,

      horaFin:
        this.form.horaFin || null,

      acompaniante:
        this.normalizeOptionalText(
          this.form.acompaniante
        )

    });
  }


  onCancel(): void {
    this.cancelled.emit();
  }


  private normalizeOptionalText(
    value: string | null
  ): string | null {

    if (!value) {
      return null;
    }

    const normalized =
      value.trim();

    return normalized || null;
  }
}