import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileUploadsService {

  private baseUrl = 'http://localhost:8090/api/v1/attachment'; // Ajustez l'URL selon votre configuration

  constructor(private http: HttpClient) {}

 
  uploadAttachment(ticketId: string, file: File): Observable<number> {
    const formData = new FormData();
    formData.append('ticketId', ticketId);
    formData.append('file', file);
    
       const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`
    
  });

    return this.http.post<number>(`${this.baseUrl}/create`, formData,{headers:headers});
  }

  
  uploadMultipleAttachments(ticketId: string, files: File[]): Observable<number[]> {
    const uploadPromises = files.map(file => 
      this.uploadAttachment(ticketId, file).toPromise()
    );
     
      
       const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });

    return new Observable(observer => {
      Promise.all(uploadPromises)
        .then(results => {
          observer.next(results.filter(result => result !== undefined) as number[]);
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }
}
