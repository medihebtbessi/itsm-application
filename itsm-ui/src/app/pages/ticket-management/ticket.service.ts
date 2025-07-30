

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../user/user.service';

export interface Ticket {
  id?: string;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'NEW' | 'IN_PROGRESS' | 'RESOLVED' | 'ON_HOLD' | 'CLOSED';
  category: 'HARDWARE' | 'SOFTWARE' | 'NETWORK' | 'OTHER';
  type: 'BUG' | 'FEATURE' ;
  resolution_notes?: string;
  createdDate?: string;      
  resolutionTime?: string;   
  sender?: UserLight;
  recipient?: UserLight;
  attachmentUrls?: string[];
  comments:any[];
}

export interface UserLight {
  id: number;
  fullName: string;
  email: string;
}

export interface Attachment {
  id: string;
  fileName: string;
  url: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}
export interface TicketSuggestionRequest {
  title: string;
  description: string;
  excludeId?: string;
  limit?: number;
}

export interface TicketSuggestion {
  ticketId: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  category: string;
  createdDate: string; 
  similarityScore: number;
}
export interface Comment{
  content:string;
  type: 'TEXT' | 'FILE';
  user:User;
  createdDate :string;
}


@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private readonly api = 'http://localhost:8090/api/v1/ticket';

  constructor(private http: HttpClient) {}

  
  getAll(page = 0, size = 10): Observable<PageResponse<Ticket>> {
    const params = new HttpParams().set('page', page).set('size', size);
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<PageResponse<Ticket>>(`${this.api}/getAllTicket`, { headers:headers,params });
  }

  getAsRecipient(page = 0, size = 10): Observable<PageResponse<Ticket>> {
    const params = new HttpParams().set('page', page).set('size', size);
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<PageResponse<Ticket>>(`${this.api}/recipient`, { headers:headers,params });
  }

  getAsSender(page = 0, size = 10): Observable<PageResponse<Ticket>> {
    const params = new HttpParams().set('page', page).set('size', size);
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<PageResponse<Ticket>>(`${this.api}/sender`, { headers:headers,params });
  }

  create(ticket: Ticket): Observable<string> {
    const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.post(`${this.api}/createTicket`, ticket, { headers:headers,responseType: 'text' });
  }

  findById(id: string): Observable<any> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<any>(`${this.api}/${id}`,{headers:headers});
  }

  update(id: string, ticket: Ticket): Observable<string> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.put(`${this.api}/${id}`, ticket, { headers:headers,responseType: 'text' });
  }

  delete(id: string): Observable<void> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.delete<void>(`${this.api}/${id}`,{headers:headers});
  }

  resolve(ticketId: string, resolutionNotes: string): Observable<string> {
    const params = new HttpParams()
      .set('ticketId', ticketId)
      .set('resolutionNotes', resolutionNotes);
       const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.put(`${this.api}/ticketAsResolved`, null, {headers:headers, params, responseType: 'text' });
  }

  assignToUser( userId: number,ticketId: string,): Observable<string> {
    const params = new HttpParams()
      .set('ticketId', ticketId)
      .set('userId', userId);
       const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.put(`${this.api}/assignToUser`, null, {headers:headers, params, responseType: 'text' });
  }


  importCsv(file: File): Observable<void> {
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });

  const formData = new FormData();
  formData.append('file', file);

  return this.http.post<void>(this.api + '/uploadCsv', formData, { headers });
}

getSuggestions(request: TicketSuggestionRequest): Observable<TicketSuggestion[]> {
   const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });
    return this.http.post<TicketSuggestion[]>(this.api+"/suggestions", request,{headers:headers});
  }

  addComment(ticketId:string,comment:any): Observable<string> {
   const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });
    return this.http.put<string>(this.api+`/comment/${ticketId}`, comment,{headers:headers});
  }

}
