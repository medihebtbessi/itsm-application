import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

// Interface pour les notifications depuis Redis
interface Notification {
  id: string;
  status: string;
  priority: string;
  title: string;
  description: string;
  category: string;
  type: string;
  op?: string | null;
  timestamp?: number | null;
}

@Component({
  selector: 'app-ticket-notification',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './ticket-notification.component.html',
  styleUrls: ['./ticket-notification.component.scss']
})
export class TicketNotificationComponent implements OnInit, OnDestroy {
  private stompClient: Client = new Client();
  private subscription?: StompSubscription;

  public notifications: Notification[] = [];
  public isOpen = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadNotificationsFromRedis(); // Charger au démarrage
    this.connect();                    // Et démarrer le WebSocket
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  loadNotificationsFromRedis(): void {
    this.http.get<Notification[]>('http://localhost:8090/api/v1/notifications')
      .pipe(
        catchError(err => {
          console.error('Erreur de récupération des notifications depuis Redis:', err);
          return of([]);
        })
      )
      .subscribe((data) => {
        this.notifications = data;
      });
  }

  connect(): void {
    const socket = new SockJS('http://localhost:8090/api/v1/ws');

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        this.subscription = this.stompClient.subscribe('/topic/ticket-changes', (msg: IMessage) => {
          try {
            const notification: Notification = JSON.parse(msg.body);

            // Ajouter en tête de la liste
            this.notifications.unshift(notification);

            // Limiter à 20 notifications max
            if (this.notifications.length > 20) {
              this.notifications = this.notifications.slice(0, 20);
            }

            console.log('Notification reçue:', notification);

          } catch (err) {
            console.error('Erreur de parsing WebSocket message:', err);
          }
        });
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame);
      },
      onWebSocketError: (event) => {
        console.error('WebSocket Error:', event);
      }
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    this.subscription?.unsubscribe();
    if (this.stompClient.connected) {
      this.stompClient.deactivate();
    }
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
  }

  formatDate(timestamp?: number | null): string {
    if (!timestamp) return 'Date inconnue';
    return new Date(timestamp).toLocaleString('fr-FR');
  }
}


