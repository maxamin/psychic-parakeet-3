import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {SeparationService} from '../../services/separation.service';

@Component({
  selector: 'app-separation-detail',
  templateUrl: './separation-detail.component.html',
  styleUrls: ['./separation-detail.component.scss']
})
export class SeparationDetailComponent implements OnInit {
  separation: any;

  constructor(private activeRoute: ActivatedRoute, private separationService: SeparationService) { }

  ngOnInit() {
    this.activeRoute.params.subscribe(params => this.separationService.getSeparation(params['id']).subscribe(res => this.separation = res));
  }

}
