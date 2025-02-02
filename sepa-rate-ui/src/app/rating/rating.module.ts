import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RatingOverviewComponent} from './rating-overview/rating-overview.component';
import {RatingRoutingModule} from './rating-routing.module';
import {SeparationModule} from '../separation/separation.module';

@NgModule({
  declarations: [RatingOverviewComponent],
  imports: [
    CommonModule,
    SeparationModule,
    RatingRoutingModule
  ]
})
export class RatingModule { }
