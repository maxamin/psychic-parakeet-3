import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {SeparationService} from '../../separation/services/separation.service';
import {Router} from '@angular/router';
import {CookieService} from 'ngx-cookie';

@Component({
  selector: 'app-rating-overview',
  templateUrl: './rating-overview.component.html',
  styleUrls: ['./rating-overview.component.scss']
})
export class RatingOverviewComponent implements OnInit {
  private username: any;
  separations: Observable<any[]>;
  userCountry;

  constructor(private separationService: SeparationService, private router: Router, private cookieService: CookieService) {
  }

  ngOnInit() {
    const user = this.cookieService.get('user');
    if (!user) {
      this.router.navigateByUrl('/login');
    } else {
      this.username = JSON.parse(user).username;
      this.userCountry = JSON.parse(user).country;
      this.separations = this.separationService.getAllSeparations();
    }
  }

  isUsersCountry(country: string) {
    return country.toLowerCase() === this.userCountry.toLowerCase();
  }
}
