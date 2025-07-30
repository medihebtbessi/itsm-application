import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardOverviewDTO {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  averageResolutionTimeInHours: number;
}

export interface Ticket {
  id: string;
  title: string;
  description: string;
  priority: string;
  status: string;
  category: string;
  resolution_notes?: string;
  createdDate?: string;
  resolutionTime?: string;
  sender?: any;
  recipient?: any;
  attachments?: any[];
}

export interface UserLoadDTO {
  recipientName: string;
  totalTickets: number;
  resolvedTickets: number;
  inProgressTickets: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly apiUrl = 'http://localhost:8090/api/v1/dashboard';

  constructor(private http: HttpClient) {}

  getOverview(): Observable<DashboardOverviewDTO> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<DashboardOverviewDTO>(`${this.apiUrl}/overview`,{ headers :headers});
  }

  getTicketsByStatus(): Observable<{ [status: string]: number }> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<{ [status: string]: number }>(`${this.apiUrl}/tickets-by-status`,{ headers: headers });
  }

  getTicketsByPriority(): Observable<{ [priority: string]: number }> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<{ [priority: string]: number }>(`${this.apiUrl}/tickets-by-priority`,{ headers: headers });
  }

  getTicketsByCategory(): Observable<{ [category: string]: number }> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<{ [category: string]: number }>(`${this.apiUrl}/tickets-by-category`,{ headers: headers });
  }

  getUrgentTickets(): Observable<Ticket[]> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<Ticket[]>(`${this.apiUrl}/urgent-tickets`,{ headers: headers });
  }

  getLoadByRecipient(): Observable<UserLoadDTO[]> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<UserLoadDTO[]>(`${this.apiUrl}/load-by-recipient`,{ headers: headers });
  }
}
