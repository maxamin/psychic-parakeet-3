import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SeparationRoutingModule} from './separation-routing.module';
import {SeparationOverviewComponent} from './components/separation-overview/separation-overview.component';
import {EscapeHtmlPipe} from '../util/keep-html.pipe';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { SeparationDetailComponent } from './components/separation-detail/separation-detail.component';

@NgModule({
  declarations: [SeparationOverviewComponent, EscapeHtmlPipe, SeparationDetailComponent
  ],
  imports: [
    CommonModule,
    SeparationRoutingModule,
    ReactiveFormsModule,
    FormsModule
  ],
  exports: [EscapeHtmlPipe]
})
export class SeparationModule {
}
