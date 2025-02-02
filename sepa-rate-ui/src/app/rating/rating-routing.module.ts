import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RatingOverviewComponent} from './rating-overview/rating-overview.component';


const routes: Routes = [
    {path: 'rating/overview', component: RatingOverviewComponent},
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class RatingRoutingModule { }
