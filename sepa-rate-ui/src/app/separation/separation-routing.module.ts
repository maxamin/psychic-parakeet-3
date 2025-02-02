import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SeparationOverviewComponent} from './components/separation-overview/separation-overview.component';
import {SeparationDetailComponent} from './components/separation-detail/separation-detail.component';


const routes: Routes = [
    {path: 'overview', component: SeparationOverviewComponent},
    {path: 'separation/overview', component: SeparationOverviewComponent},
    {path: 'separation/:id', component: SeparationDetailComponent}
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class SeparationRoutingModule { }
