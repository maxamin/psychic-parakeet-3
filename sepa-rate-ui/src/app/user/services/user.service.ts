import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private httpClient: HttpClient) { }

  login(user): Observable<any> {
    return this.httpClient.post(`${environment.backendUrl}/login`, user, {withCredentials: true});
  }
}
