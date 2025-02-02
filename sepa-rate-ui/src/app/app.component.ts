import {Component} from '@angular/core';
import {CookieService} from 'ngx-cookie';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'sepa-rate-ui';

  constructor(private cookieService: CookieService, private router: Router) {
  }

  isLoggedIn() {
    return this.cookieService.get('user');
  }

  logOut() {
    this.cookieService.remove('user');
    this.cookieService.remove('HACKME');
    this.router.navigateByUrl('/login');
  }

  getUsername() {
    return JSON.parse(this.cookieService.get('user')).username;
  }
}
