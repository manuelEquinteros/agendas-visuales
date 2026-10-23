import { Routes } from '@angular/router';

import { AgendasListPageComponent }
  from './features/agendas-list/agendas-list-page.component';

import { AgendaEditorPageComponent }
  from './features/agenda-editor/agenda-editor-page.component';

import {
  PictogramSelectorPageComponent
} from './features/pictogram-selector/pictogram-selector-page.component';

export const routes: Routes = [

  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'agendas'
  },

  {
    path: 'agendas',
    component: AgendasListPageComponent
  },

  {
    path: 'agendas/new',
    component: AgendaEditorPageComponent
  },

  {
    path: 'agendas/:id/edit',
    component: AgendaEditorPageComponent
  },

  {
    path:
      'agendas/new/activities/:activityId/pictogram',
    component:
      PictogramSelectorPageComponent
  },
  {
    path:
      'agendas/:id/activities/:activityId/pictogram',
    component:
      PictogramSelectorPageComponent
  },

  {
    path: '**',
    redirectTo: 'agendas'
  }

];