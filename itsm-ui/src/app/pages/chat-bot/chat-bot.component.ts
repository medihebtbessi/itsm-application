import { HttpClient } from '@angular/common/http';
import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatBotService } from './chat-bot.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ChatRequest {
  context: string;
  question: string;
}

export interface ChatResponse {
  response: string;
}

@Component({
  selector: 'app-chat-bot',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-bot.component.html',
  styleUrl: './chat-bot.component.scss'
})
export class ChatBotComponent implements AfterViewChecked {
  @ViewChild('chatMessages') private chatMessages!: ElementRef;
  @ViewChild('messageInput') private messageInput!: ElementRef;

  constructor(private chatbotService: ChatBotService) {}

  userInput = '';
  conversationHistory = '';
  messages: { from: 'user' | 'bot', text: string, timestamp?: Date }[] = [];
  isTyping = false;

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      if (this.chatMessages) {
        this.chatMessages.nativeElement.scrollTop = this.chatMessages.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Erreur lors du scroll:', err);
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  send(): void {
    if (!this.userInput.trim() || this.isTyping) return;

    const userMessage = this.userInput.trim();
    
    this.messages.push({ 
      from: 'user', 
      text: userMessage,
      timestamp: new Date()
    });

    this.isTyping = true;
    this.userInput = '';

    this.chatbotService.sendQuestion({
      question: userMessage,
      context: this.conversationHistory
    }).subscribe({
      next: (res) => {
        setTimeout(() => {
          this.messages.push({ 
            from: 'bot', 
            text: res.response,
            timestamp: new Date()
          });
          this.conversationHistory += `User: ${userMessage}\nBot: ${res.response}\n`;
          this.isTyping = false;
          
          setTimeout(() => {
            if (this.messageInput) {
              this.messageInput.nativeElement.focus();
            }
          }, 100);
        }, 1000);
      },
      error: (error) => {
        console.error('Erreur lors de l\'envoi du message:', error);
        this.messages.push({ 
          from: 'bot', 
          text: 'Désolé, une erreur s\'est produite. Veuillez réessayer.',
          timestamp: new Date()
        });
        this.isTyping = false;
      }
    });
  }

  getCurrentTime(): string {
    return new Date().toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  clearChat(): void {
    this.messages = [];
    this.conversationHistory = '';
  }
}