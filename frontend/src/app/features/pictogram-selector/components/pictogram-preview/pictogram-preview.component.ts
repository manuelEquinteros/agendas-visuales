import {
  Component,
  Input
} from '@angular/core';

import {
  PictogramaCandidate
} from '../../../../core/models/pictogram.models';


@Component({
  selector: 'app-pictogram-preview',
  standalone: true,
  imports: [],
  templateUrl:
    './pictogram-preview.component.html',
  styleUrl:
    './pictogram-preview.component.scss'
})
export class PictogramPreviewComponent {

  @Input()
  candidate: PictogramaCandidate | null =
    null;
}