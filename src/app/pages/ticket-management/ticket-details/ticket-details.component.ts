import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Attachment, Comment, Ticket, TicketService } from '../ticket.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ticket-details',
  imports: [CommonModule, FormsModule],
  templateUrl: './ticket-details.component.html',
  styleUrl: './ticket-details.component.scss'
})
export class TicketDetailsComponent implements OnInit {
  
  constructor(private ticketService: TicketService) {}

  @Input() ticket: Ticket | null = null;
  @Input() isVisible: boolean = false;
 
  @Output() close = new EventEmitter<void>();
  @Output() edit = new EventEmitter<Ticket>();
  @Output() exportPdf = new EventEmitter<Ticket>();
  
  currentTime = new Date();
  isSubmittingComment = false;
  
  newComment = {
    content: '',
    type: 'TEXT',
    creationDate: '',
    author: 'Utilisateur actuel' 
  };

  ngOnInit() {
    this.currentTime = new Date();
  }

  copyToClipboard(text: string): void {
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(text).catch(err => {
        console.error('Failed to copy text: ', err);
      });
    } else {
      this.fallbackCopyToClipboard(text);
    }
  }

  private fallbackCopyToClipboard(text: string): void {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
   
    try {
      document.execCommand('copy');
    } catch (err) {
      console.error('Fallback copy failed: ', err);
    } finally {
      document.body.removeChild(textArea);
    }
  }

  closeDialog() {
    this.close.emit();
  }

  editTicket() {
    if (this.ticket) {
      this.edit.emit(this.ticket);
    }
  }

  exportToPDF() {
    if (this.ticket) {
      this.exportPdf.emit(this.ticket);
    }
  }

  getStatusClass(): string {
    switch (this.ticket?.status?.toLowerCase()) {
      case 'resolved':
      case 'closed':
        return 'bg-success';
      case 'in_progress':
      case 'in-progress':
        return 'bg-warning text-dark';
      case 'open':
        return 'bg-primary';
      default:
        return 'bg-secondary';
    }
  }

  getStatusIcon(): string {
    switch (this.ticket?.status?.toLowerCase()) {
      case 'resolved':
      case 'closed':
        return 'bi bi-check-circle-fill me-1';
      case 'in_progress':
      case 'in-progress':
        return 'bi bi-clock-fill me-1';
      case 'open':
        return 'bi bi-exclamation-circle-fill me-1';
      default:
        return 'bi bi-circle-fill me-1';
    }
  }

  getPriorityClass(): string {
    switch (this.ticket?.priority?.toLowerCase()) {
      case 'high':
      case 'critical':
        return 'bg-danger';
      case 'medium':
        return 'bg-warning text-dark';
      case 'low':
        return 'bg-success';
      default:
        return 'bg-info';
    }
  }

  getPriorityIcon(): string {
    switch (this.ticket?.priority?.toLowerCase()) {
      case 'high':
      case 'critical':
        return 'bi bi-exclamation-triangle-fill me-1';
      case 'medium':
        return 'bi bi-dash-circle-fill me-1';
      case 'low':
        return 'bi bi-arrow-down-circle-fill me-1';
      default:
        return 'bi bi-info-circle-fill me-1';
    }
  }

  addComment() {
    if (!this.newComment.content.trim() || !this.ticket?.id) {
      return;
    }

    this.isSubmittingComment = true;
    
    this.newComment.creationDate = new Date().toISOString();
    
    this.ticketService.addComment(this.ticket.id, this.newComment).subscribe({
      next: (data) => {
        console.log("Comment added successfully", data);
        
        if (this.ticket?.comments) {
          this.ticket.comments.push({
            ...this.newComment,
            id: data || Date.now(), 
            creationDate: this.newComment.creationDate
          });
        } else if (this.ticket) {
          this.ticket.comments = [{
            ...this.newComment,
            id: data || Date.now(),
            creationDate: this.newComment.creationDate
          }];
        }
        
        this.resetCommentForm();
        this.isSubmittingComment = false;
      },
      error: (error) => {
        console.error("Error adding comment", error);
        this.isSubmittingComment = false;
      }
    });
  }

  resetCommentForm() {
    this.newComment = {
      content: '',
      type: 'TEXT',
      creationDate: '',
      author: 'Utilisateur actuel'
    };
  }

  trackByCommentId(index: number, comment: any): any {
    return comment.id || index;
  }
}