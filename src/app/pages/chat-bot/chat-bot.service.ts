import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse } from './chat-bot.component';

@Injectable({
  providedIn: 'root'
})
export class ChatBotService {

   constructor(private http: HttpClient) {}

  sendQuestion(req: ChatRequest): Observable<ChatResponse> {
     const headers = new HttpHeaders({
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  });
    return this.http.post<ChatResponse>('http://localhost:8090/api/v1/chat', req,{headers:headers});
  }
}
