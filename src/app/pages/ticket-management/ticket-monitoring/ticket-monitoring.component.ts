import { CommonModule, NgFor } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface TicketData {
  id: string;
  created_date: number;
  last_modified_date?: number;
  category: string;
  description: string;
  priority: string;
  status: string;
  title: string;
  type: string;
  recipient_id?: number;
  sender_id?: number;
  resolution_notes?: string;
  resolution_time?: number;
}

interface KafkaMessage {
  schema: any;
  payload: {
    before?: TicketData;
    after?: TicketData;
    source: {
      version: string;
      connector: string;
      name: string;
      ts_ms: number;
      snapshot: string;
      db: string;
      schema: string;
      table: string;
      txId: number;
      lsn: number;
    };
    op: string; // 'c' = create, 'u' = update, 'd' = delete
    ts_ms: number;
  };
}

@Component({
  selector: 'app-ticket-monitoring',
  imports: [NgFor, CommonModule],
  templateUrl: './ticket-monitoring.component.html',
  styleUrl: './ticket-monitoring.component.scss'
})
export class TicketMonitoringComponent implements OnInit, OnDestroy {
  private stompClient: Client = new Client();
  private subscription?: StompSubscription;
  public messages: KafkaMessage[] = [];
  public isConnected = false;
  public connectionError = '';
  
  // Statistiques
  public stats = {
    totalMessages: 0,
    creates: 0,
    updates: 0,
    deletes: 0
  };

  ngOnInit(): void {
    this.connectToWebSocket();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  connectToWebSocket(): void {
    const socket = new SockJS('http://localhost:8090/api/v1/ws');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('[WebSocket DEBUG]', str),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('✅ Connected to WebSocket');
        this.isConnected = true;
        this.connectionError = '';
        
        this.subscription = this.stompClient.subscribe(
          '/topic/ticket-changes',
          (message: IMessage) => {
            const payload: KafkaMessage = JSON.parse(message.body);
            console.log('📥 Message reçu:', payload);
            
            this.messages.unshift(payload);
            this.updateStats(payload);
            
            // Garder seulement les 100 derniers messages
            if (this.messages.length > 100) {
              this.messages = this.messages.slice(0, 100);
            }
          }
        );
      },
      onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame.headers['message']);
        this.connectionError = frame.headers['message'] || 'Erreur de connexion';
        this.isConnected = false;
      },
      onDisconnect: () => {
        console.log('🔌 Disconnected from WebSocket');
        this.isConnected = false;
      }
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.deactivate();
    }
    this.isConnected = false;
  }

  reconnect(): void {
    this.disconnect();
    setTimeout(() => this.connectToWebSocket(), 1000);
  }

  clearMessages(): void {
    this.messages = [];
    this.resetStats();
  }

  private updateStats(message: KafkaMessage): void {
    this.stats.totalMessages++;
    switch (message.payload.op) {
      case 'c':
        this.stats.creates++;
        break;
      case 'u':
        this.stats.updates++;
        break;
      case 'd':
        this.stats.deletes++;
        break;
    }
  }

  private resetStats(): void {
    this.stats = {
      totalMessages: 0,
      creates: 0,
      updates: 0,
      deletes: 0
    };
  }

  // Méthodes utilitaires pour le template
  getOperationLabel(op: string): string {
    switch (op) {
      case 'c': return 'Création';
      case 'u': return 'Mise à jour';
      case 'd': return 'Suppression';
      default: return 'Inconnu';
    }
  }

  getOperationIcon(op: string): string {
    switch (op) {
      case 'c': return 'bi-plus-circle';
      case 'u': return 'bi-pencil-square';
      case 'd': return 'bi-trash';
      default: return 'bi-question-circle';
    }
  }

  getOperationColor(op: string): string {
    switch (op) {
      case 'c': return 'success';
      case 'u': return 'warning';
      case 'd': return 'danger';
      default: return 'secondary';
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority?.toUpperCase()) {
      case 'HIGH': return 'danger';
      case 'MEDIUM': return 'warning';
      case 'LOW': return 'success';
      default: return 'secondary';
    }
  }

  getStatusColor(status: string): string {
    switch (status?.toUpperCase()) {
      case 'NEW': return 'primary';
      case 'IN_PROGRESS': return 'warning';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'secondary';
      default: return 'light';
    }
  }

  formatTimestamp(timestamp: number): string {
    return new Date(timestamp / 1000).toLocaleString('fr-FR');
  }

  formatMicroTimestamp(microTimestamp: number): string {
    return new Date(microTimestamp / 1000).toLocaleString('fr-FR');
  }
}