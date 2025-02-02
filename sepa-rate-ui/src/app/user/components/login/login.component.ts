import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {validateRequired} from '../../../util/required.validator';
import {UserService} from '../../services/user.service';
import {CookieService} from 'ngx-cookie';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loginFailed: boolean;

  constructor(private fb: FormBuilder, private userService: UserService, private cookieService: CookieService, private router: Router) {
    this.loginForm = fb.group({
      username: ['', validateRequired],
      password: ['', validateRequired]
    });
  }

  ngOnInit() {
  }

  login() {
    if (this.loginForm.valid) {
      this.userService.login(this.loginForm.value)
        .subscribe(res => {
          if (!res) {
            this.loginFailed = true;
          } else {
            this.cookieService.put('user', JSON.stringify(res), {expires: new Date(2099, 11, 24, 10, 33, 30, 0)});
            this.cookieService.put('HACKME', 'This cookie was set from sepa-rate', {expires: new Date(2099, 11, 24, 10, 33, 30, 0)});
            this.router.navigateByUrl('');
          }
        });
    }
  }
}
