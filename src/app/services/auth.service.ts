import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RegistrationRequest } from '../models/RegistrationRequest';
import { AuthenticationRequest } from '../models/AuthenticationRequest';
import { AuthenticationResponse } from '../models/AuthenticationResponse';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

   private apiUrl = 'http://localhost:8090/api/v1/auth'; 

  constructor(private http: HttpClient) {
  }

  register(request: RegistrationRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/register`, request);
  }

  authenticate(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/authenticate`, request);
  }

  activateAccount(token: string): Observable<void> {
    const params = new HttpParams().set('token', token);
    return this.http.get<void>(`${this.apiUrl}/activate-account`, { params });
  }

  modifyPassword(payload: { [key: string]: string }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/modifyPassword`, payload);
  }

 
 setNewPassword(email: string, password: string, code: string) {
    const payload = {
      email: email,
      password: password,
      code: code
    };
    return this.http.post(`${this.apiUrl}/newPassword`, payload);
  }


  getUserInfo(): Observable<any> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });

    return this.http.get(`${this.apiUrl}/getUserInfo`,{headers:headers});
  }
}
