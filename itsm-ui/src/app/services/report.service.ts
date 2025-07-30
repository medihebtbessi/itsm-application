import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

 private baseUrl = 'http://localhost:8090/api/v1/reports'; // Adjust this if different in production

  constructor(private http: HttpClient) {}

 
  generateMonthlyReport(): Observable<any> {
    const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });
    return this.http.post(`${this.baseUrl}/monthly`, {},{headers:headers});
  }

  
  generateCustomReport(startDate: string, endDate: string): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
      const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });

    return this.http.post(`${this.baseUrl}/custom`, {}, { params,headers });
  }

  
  getQuickAnalysis(startDate?: string, endDate?: string): Observable<any> {
    let params = new HttpParams();

    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  });
    return this.http.get(`${this.baseUrl}/quick-analysis`, { params ,headers});
  }
}
