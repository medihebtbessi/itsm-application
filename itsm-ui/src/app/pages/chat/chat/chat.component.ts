import { CommonModule, NgFor, NgIf } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { User, UserService } from '../../user/user.service';
import { ChatService } from '../chat.service';
import { MessageService } from '../../message/message.service';
import { ChatResponse } from '../../../models/chat-response';
import { UserResponse } from '../../../models/user-response';
import { MessageResponse } from '../../../models/message-response';
import { MessageRequest } from '../../../models/message-request';
import SockJS from 'sockjs-client'; 
import { HttpHeaders } from '@angular/common/http';
export interface Notification {
  chatId?: string;
  content?: string;
  senderId?: number;
  receiverId?: number;
  messageType?: 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO';
  type?: 'SEEN' | 'MESSAGE' | 'IMAGE' | 'VIDEO' | 'AUDIO';
  chatName?: string;
  media?: Array<string>;
}
@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy,AfterViewChecked {
 
  connectedUser!: User;                           // utilisateur connecté
  chats: ChatResponse[] = [];                    // toutes les conversations de l'utilisateur
  contacts: User[] = [];                         // liste des contacts lors d'une nouvelle recherche
  selectedChat?: ChatResponse;                   // conversation actuellement ouverte
  chatMessages: MessageResponse[] = [];          // messages de la conversation courante

  messageContent = '';                           // champ de saisie du message
  showEmojis = false;                            // affichage du sélecteur d'émojis
  searchNewContact = false;                      // panneau « nouvelle discussion »

  @Output() chatSelectedEvent = new EventEmitter<ChatResponse>();

  private subs = new Subscription();             // pour clean‑up des souscriptions
  private socketClient: any = null;              // client WebSocket

  constructor(
    private readonly userService: UserService,
    private readonly chatService: ChatService,
    private readonly messageService: MessageService
  ) {}

  ngOnInit(): void {
    
    this.subs.add(
      this.userService.getCurrentUser().subscribe(user => {
        this.connectedUser = user;
        if (this.connectedUser.id) {
          this.initWebSocket(); 
        }
        this.fetchChats();
       
        this.searchContact();
      })
    );
    
  }
 private notificationSubscription: any;
  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.disconnectWebSocket();
  }

   private initWebSocket() {
  if (this.connectedUser.id) {
    this.socketClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8090/api/v1/ws'),
      connectHeaders: {
        Authorization: 'Bearer ' + localStorage.getItem('token') || '',
      },
      debug: (str) => {
        console.log(str);
      },
      reconnectDelay: 5000,
    });

    this.socketClient.onConnect = () => {
      const subUrl = `/user/${this.connectedUser.id}/chat`;
      this.socketClient.subscribe(subUrl, (message:{body:string}) => {
        const notification: Notification = JSON.parse(message.body);
        this.handleNotification(notification);
      });
    };

    this.socketClient.activate();
  }
}

  private handleNotification(notification: Notification) {
    if (!notification) return;
    if (this.selectedChat && this.selectedChat.id === notification.chatId) {
      console.log('Notification for current chat:', this.selectedChat);
      switch (notification.type) {
        case 'MESSAGE':
        case 'IMAGE':
          const message: MessageResponse = {
            senderId: notification.senderId,
            receiverId: notification.receiverId,
            content: notification.content,
            type: notification.messageType,
            media: notification.media,
            createdAt: new Date().toString()
          };
          if (notification.type === 'IMAGE') {
            this.selectedChat.lastMessage = 'Attachment';
          } else {
            this.selectedChat.lastMessage = notification.content;
          }
          this.chatMessages.push(message);
          break;
        case 'SEEN':
          this.chatMessages.forEach(m => m.state = 'SEEN');
          break;
      }
    } else {
      const destChat = this.chats.find(c => c.id === notification.chatId);
      if (destChat && notification.type !== 'SEEN') {
        if (notification.type === 'MESSAGE') {
          destChat.lastMessage = notification.content;
        } else if (notification.type === 'IMAGE') {
          destChat.lastMessage = 'Attachment';
        }
        destChat.lastMessageTime = new Date().toString();
        destChat.unreadCount! += 1;
      } else if (notification.type === 'MESSAGE') {
        const newChat: ChatResponse = {
          id: notification.chatId,
          senderId: notification.senderId,
          receiverId: notification.receiverId,
          lastMessage: notification.content,
          name: notification.chatName,
          unreadCount: 1,
          lastMessageTime: new Date().toString()
        };
        this.chats.unshift(newChat);
      }
    }
  }

  private handleReceivedMessage(message: any): void {
    // Traitement du message reçu
    if (message && message.chatId === this.selectedChat?.id) {
      // Ajouter le message à la conversation courante
      const newMessage: MessageResponse = {
        senderId: message.senderId,
        receiverId: message.receiverId,
        content: message.content,
        type: message.type || 'TEXT',
        state: 'SENT',
        createdAt: message.createdAt || new Date().toISOString(),
        media: message.media
      };
      
      this.chatMessages.push(newMessage);
    }
    
    // Mettre à jour la liste des chats
    this.updateChatList(message);
  }

  private updateChatList(message: any): void {
    // Mettre à jour la liste des chats avec le nouveau message
    const chatIndex = this.chats.findIndex(chat => chat.id === message.chatId);
    
    if (chatIndex !== -1) {
      this.chats[chatIndex].lastMessage = message.content;
      this.chats[chatIndex].lastMessageTime = message.createdAt;
      
      // Incrémenter le compteur de messages non lus si ce n'est pas le chat actuel
      if (message.chatId !== this.selectedChat?.id) {
        this.chats[chatIndex].unreadCount = (this.chats[chatIndex].unreadCount || 0) + 1;
      }
      
      // Déplacer le chat en tête de liste
      const updatedChat = this.chats.splice(chatIndex, 1)[0];
      this.chats.unshift(updatedChat);
    }
  }

  private disconnectWebSocket(): void {
    if (this.socketClient && this.socketClient.connected) {
      this.socketClient.disconnect(() => {
        console.log('WebSocket déconnecté');
      });
    }
  }

  private sendMessageViaWebSocket(messageRequest: MessageRequest): void {
    if (this.socketClient && this.socketClient.connected) {
      this.socketClient.send('/app/chat', {}, JSON.stringify(messageRequest));
    }
  }

  private fetchChats(): void {
    this.subs.add(this.chatService.getChatsForReceiver().subscribe(chats => (this.chats = chats)));
  }

  searchContact(): void {
    this.subs.add(
      this.userService.getAllUsers().subscribe(users => {
        this.contacts = users;
        this.searchNewContact = true;
      })
    );
  }

  openChat(chat: ChatResponse): void {
    if (this.selectedChat?.id === chat.id) return; // déjà ouvert
    this.selectedChat = chat;
    this.chatSelectedEvent.emit(chat);
    this.loadMessages(chat.id as string);
    this.markMessagesAsSeen(chat.id as string);
    chat.unreadCount = 0;
  }

  startChatWith(contact: UserResponse): void {
    if (!this.connectedUser?.id) return;

    this.subs.add(
      this.chatService
        .createChat(this.connectedUser.id, contact.id!)
        .subscribe(res => {
          const newChat: ChatResponse = {
            id: res.response,
            name: `${contact.firstName} ${contact.lastName}`,
            recipientOnline: contact.online,
            lastMessageTime: contact.lastSeen,
            senderId: this.connectedUser!.id,
            receiverId: contact.id,
            unreadCount: 0
          };
          this.chats.unshift(newChat); // en‑tête de liste
          this.searchNewContact = false;
          this.openChat(newChat);
        })
    );
  }

  private loadMessages(chatId: string): void {
    this.subs.add(
      this.messageService.getMessagesByChatId(chatId).subscribe(msgs => (this.chatMessages = msgs))
    );
  }

  private markMessagesAsSeen(chatId: string): void {
    this.subs.add(this.messageService.setMessagesToSeen(chatId).subscribe());
  }

  isSelfMessage(message: MessageResponse): boolean {
    return message.senderId === this.connectedUser?.id;
  }

  sendMessage(): void {
    if (!this.messageContent.trim() || !this.selectedChat) return;

    const request: MessageRequest = {
      chatId: this.selectedChat.id,
      senderId: this.connectedUser!.id,
      receiverId: this.getReceiverId(),
      content: this.messageContent.trim(),
      type: 'TEXT'
    };

    // Envoyer via WebSocket
    this.sendMessageViaWebSocket(request);

    // Sauvegarder en base de données
    this.subs.add(
      this.messageService.saveMessage(request).subscribe(() => {
        const localMsg: MessageResponse = {
          senderId: request.senderId,
          receiverId: request.receiverId,
          content: request.content,
          type: 'TEXT',
          state: 'SENT',
          createdAt: new Date().toISOString()
        };
        this.chatMessages.push(localMsg);
        this.selectedChat!.lastMessage = request.content;
        this.messageContent = '';
        this.showEmojis = false;
      })
    );
  }

  private getReceiverId(): number {
    if (!this.selectedChat) return 0;
    return this.selectedChat.senderId === this.connectedUser?.id
      ? this.selectedChat.receiverId!
      : this.selectedChat.senderId!;
  }

  getAvatarColor(index: number): string {
    const colors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#84cc16'];
    return colors[index % colors.length];
  }

  uploadMedia(target: EventTarget | null): void {
    const file = this.extractFileFromTarget(target);
    if (!file || !this.selectedChat) return;

    const reader = new FileReader();
    reader.onload = () => {
      const base64 = (reader.result as string).split(',')[1];
      this.subs.add(
        this.messageService
          .uploadMedia(this.selectedChat?.id as string, file)
          .subscribe(() => {
            const msg: MessageResponse = {
              senderId: this.connectedUser!.id,
              receiverId: this.getReceiverId(),
              content: 'Attachment',
              type: 'IMAGE',
              state: 'SENT',
              media: [base64],
              createdAt: new Date().toISOString()
            };
            this.chatMessages.push(msg);
          })
      );
    };
    reader.readAsDataURL(file);
  }

  private extractFileFromTarget(target: EventTarget | null): File | null {
    const input = target as HTMLInputElement;
    return input?.files?.item(0) ?? null;
  }

  onSelectEmojis(event: any): void {
    this.messageContent += (event.emoji as { native: string }).native;
  }

  keyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
   @ViewChild('scrollableDiv') scrollableDiv!: ElementRef<HTMLDivElement>;
   ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

   private scrollToBottom() {
    if (this.scrollableDiv) {
      const div = this.scrollableDiv.nativeElement;
      div.scrollTop = div.scrollHeight;
    }
  }
}