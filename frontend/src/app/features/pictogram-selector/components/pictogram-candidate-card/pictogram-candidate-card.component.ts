import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  PictogramaCandidate
} from '../../../../core/models/pictogram.models';


@Component({
  selector: 'app-pictogram-candidate-card',
  standalone: true,
  imports: [],
  templateUrl:
    './pictogram-candidate-card.component.html',
  styleUrl:
    './pictogram-candidate-card.component.scss'
})
export class PictogramCandidateCardComponent {

  @Input({ required: true })
  candidate!: PictogramaCandidate;


  @Input()
  selected = false;


  @Output()
  candidateSelected =
    new EventEmitter<PictogramaCandidate>();


  select(): void {

    this.candidateSelected.emit(
      this.candidate
    );
  }
}