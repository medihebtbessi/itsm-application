import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {  Solution, TicketDto } from './solution-stack-over-flow/solution-stack-over-flow.component';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SolutionSearchService {

  private apiUrl = `http://localhost:8090/api/v1/solutions`;

  constructor(private http: HttpClient) {}

  searchSolutions(ticket: TicketDto): Observable<any> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });
  return this.http.post<any>(
    `${this.apiUrl}/search`,
    ticket,{headers: headers}
  );
}

}
