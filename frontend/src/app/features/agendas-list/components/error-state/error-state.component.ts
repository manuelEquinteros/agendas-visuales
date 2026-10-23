import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [],
  templateUrl: './error-state.component.html',
  styleUrl: './error-state.component.scss'
})
export class ErrorStateComponent {

  @Input()
  message =
    'No fue posible cargar las agendas.';

  @Output()
  retry = new EventEmitter<void>();

  onRetry(): void {
    this.retry.emit();
  }
}