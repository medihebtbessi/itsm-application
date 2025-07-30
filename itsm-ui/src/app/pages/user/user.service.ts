import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
export interface User {
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  role: string;
  group: string;
  enabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

 private baseUrl = 'http://localhost:8090/api/v1/user';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<User[]>(this.baseUrl, { headers :headers});
  }

  getCurrentUser(): Observable<User> {
      const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<User>(`${this.baseUrl}/me`, { headers: headers });
  }

  getUserByEmail(email: string): Observable<User> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<User>(`${this.baseUrl}/${email}`,{headers:headers});
  }

  deleteUser(email: string): Observable<void> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.delete<void>(`${this.baseUrl}/${email}`,{headers:headers} );
  }

  updateUser(email:string,user: User): Observable<User> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.put<User>(`${this.baseUrl}/${email}`, user, { headers: headers });
  }
}
