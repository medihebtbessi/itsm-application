import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StringResponse } from '../../models/string-response';
import { ChatResponse } from '../../models/chat-response';



@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = 'http://localhost:8090/api/v1/chats';

  constructor(private http: HttpClient) {}

  createChat(senderId: number, receiverId: number): Observable<StringResponse> {
    const params = new HttpParams()
      .set('sender-id', senderId)
      .set('receiver-id', receiverId);
       const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.post<StringResponse>(this.apiUrl, null, { headers:headers,params });
  }

  getChatsForReceiver(): Observable<ChatResponse[]> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.get<ChatResponse[]>(this.apiUrl,{headers:headers});
  }
}
