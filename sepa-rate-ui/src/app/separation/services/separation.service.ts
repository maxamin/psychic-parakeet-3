import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SeparationService {

  constructor(private httpClient: HttpClient) {
  }

  getSeparations(username, filter = {filter: ''}): Observable<any> {
    return this.httpClient.post(`${environment.backendUrl}/separations/${username}`, filter, {withCredentials: true});
  }

  getAllSeparations(): Observable<any> {
    return this.httpClient.get(`${environment.backendUrl}/separations`);
  }

  getSeparation(id): Observable<any> {
    return this.httpClient.get(`${environment.backendUrl}/separation/${id}`);
  }
}
