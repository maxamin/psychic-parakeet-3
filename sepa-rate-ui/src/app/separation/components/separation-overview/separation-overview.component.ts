import {Component, OnInit} from '@angular/core';
import {SeparationService} from '../../services/separation.service';
import {Router} from '@angular/router';
import {CookieService} from 'ngx-cookie';
import {Observable} from 'rxjs';
import {NgForm} from '@angular/forms';

@Component({
  selector: 'app-separation-overview',
  templateUrl: './separation-overview.component.html',
  styleUrls: ['./separation-overview.component.scss']
})
export class SeparationOverviewComponent implements OnInit {
  private username: any;
  separations: Observable<any[]>;

  constructor(private separationService: SeparationService, private router: Router, private cookieService: CookieService) {
  }

  ngOnInit() {
    const user = this.cookieService.get('user');
    if (!user) {
      this.router.navigateByUrl('/login');
    } else {
      this.username = JSON.parse(user).username;
      this.separations = this.separationService.getSeparations(this.username);
    }
  }

  doFilter(f: NgForm) {
    this.separations = this.separationService.getSeparations(this.username, {filter: f.value.filter});
  }
}
