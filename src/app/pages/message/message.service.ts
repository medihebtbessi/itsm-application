import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageRequest } from '../../models/message-request';
import { MessageResponse } from '../../models/message-response';



@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private readonly apiUrl = 'http://localhost:8090/api/v1/messages';

  constructor(private http: HttpClient) {}

  saveMessage(message: MessageRequest): Observable<void> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.post<void>(`${this.apiUrl}`, message,{ headers :headers });
  }

  uploadMedia(chatId: string, file: File): Observable<void> {
    const formData = new FormData();
    formData.append('chat-id', chatId);
    formData.append('file', file);
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });

    return this.http.post<void>(`${this.apiUrl}/upload-media`, formData,{headers: headers});
  }

  setMessagesToSeen(chatId: string): Observable<void> {
    const params = new HttpParams().set('chat-id', chatId);
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.patch<void>(`${this.apiUrl}`, null, { headers:headers,params });
  }

  getMessagesByChatId(chatId: string): Observable<MessageResponse[]> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/chat/${chatId}`,{headers: headers});
  }
}
