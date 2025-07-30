import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Ticket, TicketService } from '../ticket.service';
import { FooterComponent } from "../../../footer/footer.component";
import { TicketModalComponent } from "../ticket-modal/ticket-modal.component";
import { DeleteDialogComponent } from "../../delete-dialog/delete-dialog.component";
import { TicketDetailsComponent } from '../ticket-details/ticket-details.component';
import { FileUploadsModalComponent } from "../file-uploads-modal/file-uploads-modal.component";
import { FileUploadsService } from '../file-uploads-modal/file-uploads.service';
import { ToastrService } from 'ngx-toastr';
import { ChatBotComponent } from "../../chat-bot/chat-bot.component";
import { TicketNotificationComponent } from "../shared/ticket-notification/ticket-notification.component";

interface Column {
  title: string;
  count: number;
  tickets: Ticket[];
  color: string;
}


@Component({
  selector: 'app-tickets-front-office',
  imports: [CommonModule, FormsModule, FooterComponent, TicketModalComponent, DeleteDialogComponent, TicketDetailsComponent, FileUploadsModalComponent, ChatBotComponent, TicketNotificationComponent],
  templateUrl: './tickets-front-office.component.html',
  styleUrl: './tickets-front-office.component.scss'
})
export class TicketsFrontOfficeComponent implements OnInit {

   ticketsNew: Ticket[] = [];
  ticketsInProgress: Ticket[] = [];
  ticketsOnHold: Ticket[] = [];
  ticketsResolvedAndClosed: Ticket[] = [];
  
  isModalOpen = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedTicket: Ticket | null = null;
  isFileUploadModalVisible: boolean = false;
  showDeleteDialog = false;
  isDialogVisible = false;
  
  columns: Column[] = [];
  searchQuery: string = '';

  currentPage = 1;
  itemsPerPage = 5;
  totalElements = 0; 
  totalPages = 0;   

  constructor(
    private ticketService: TicketService,
    private fileService: FileUploadsService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.getAllTicketsAsSender();
  }

  private updateColumns(): void {
    this.columns = [
      {
        title: 'NEW',
        color: 'secondary',
        count: this.ticketsNew.length,
        tickets: this.ticketsNew
      },
      {
        title: 'IN PROGRESS',
        color: 'primary',
        count: this.ticketsInProgress.length,
        tickets: this.ticketsInProgress
      },
      {
        title: 'ON HOLD',
        color: 'warning',
        count: this.ticketsOnHold.length,
        tickets: this.ticketsOnHold
      },
      {
        title: 'RESOLVED & CLOSED',
        color: 'success',
        count: this.ticketsResolvedAndClosed.length,
        tickets: this.ticketsResolvedAndClosed
      }
    ];
  }

  getAllTicketsAsSender(): void {
     const pageIndex = this.currentPage - 1;
    this.ticketService.getAsSender(pageIndex, this.itemsPerPage).subscribe({
      next: (data) => {
        this.ticketsNew = this.filterByStatus(data.content, ['NEW']);
        this.ticketsInProgress = this.filterByStatus(data.content, ['IN_PROGRESS']);
        this.ticketsOnHold = this.filterByStatus(data.content, ['ON_HOLD']);
        this.ticketsResolvedAndClosed = this.filterByStatus(data.content, ['RESOLVED', 'CLOSED']);
        this.updateColumns();
         this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
        this.toastr.success('Tickets fetched successfully', 'Success');
        console.log('Tickets fetched successfully:', data.content);
      },
      error: (error) => {
       this.toastr.error('Error fetching tickets:', "Error");

        console.error('Error fetching tickets:', error);
      }
    });
  }

  private filterByStatus(tickets: any[], statuses: string[]): any[] {
    return tickets.filter(ticket => statuses.includes(ticket.status));
  }

  onTicketAction(ticket: Ticket, action: string): void {
    switch (action) {
      case 'edit':
        this.openEditModal(ticket);
        break;
      case 'delete':
        this.openDeleteDialog(ticket);
        break;
      case 'move':
        this.openTicketDetails(ticket);
        break;
      case 'upload': 
        this.openFileUploadModal(ticket);
        break;
      default:
        console.log('Unknown action:', action);
    }
    console.log('Action:', action, 'on ticket:', ticket.id);
  }

  openCreateModal() {
    this.modalMode = 'create';
    this.selectedTicket = null;
    this.isModalOpen = true;
  }

  openEditModal(ticket: Ticket) {
    this.modalMode = 'edit';
    this.selectedTicket = ticket;
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.selectedTicket = null;
  }

  openFileUploadModal(ticket: Ticket) {
    this.selectedTicket = ticket;
    this.isFileUploadModalVisible = true;
  }

  onFileSelected(file: File) {
    console.log('Fichier sélectionné:', file);
  }

  closeFileUploadModal() {
    this.isFileUploadModalVisible = false;
    this.selectedTicket = null; 
    console.log('Modal fermée');
  }

  confirmFileUpload(data: { file: File | null, dontShowAgain: boolean }) {
    console.log('Upload confirmé:', data);
    
    if (data.file && this.selectedTicket) {
      this.uploadFile(data.file, this.selectedTicket.id!);
    } else if (!this.selectedTicket) {
      console.error('Aucun ticket sélectionné pour l\'upload');
    }
    
    if (data.dontShowAgain) {
      localStorage.setItem('dontShowFileUploadDialog', 'true');
    }
    
    this.isFileUploadModalVisible = false;
    this.selectedTicket = null;
  }

  private uploadFile(file: File, ticketId: string) {
    console.log('Uploading file:', file.name, 'to ticket:', ticketId);
    
    this.fileService.uploadAttachment(ticketId, file).subscribe({
      next: (attachmentId) => {
        console.log('File uploaded successfully with ID:', attachmentId);
        this.toastr.success('File uploaded successfully', 'Success');
        this.getAllTicketsAsSender();
        this.showSuccessMessage('Fichier uploadé avec succès');
      },
      error: (error) => {
        this.toastr.error('Error uploading file', 'Error');
        console.error('Error uploading file:', error);
        this.showErrorMessage('Erreur lors de l\'upload du fichier');
      }
    });
  }

  openDeleteDialog(ticket: Ticket) {
    this.selectedTicket = ticket;
    this.showDeleteDialog = true;
  }

  onDeleteConfirmed(dontShowAgain: boolean) {
    console.log('Delete confirmed');
    console.log('Don\'t show again:', dontShowAgain);
    this.deleteTicket();
    this.showDeleteDialog = false;
    this.selectedTicket = null;
  }

  onDeleteCancelled() {
    console.log('Delete cancelled');
    this.showDeleteDialog = false;
    this.selectedTicket = null;
  }

  deleteTicket() {
    if (!this.selectedTicket) return;
    
    this.ticketService.delete(this.selectedTicket.id!).subscribe({
      next: () => {
        this.toastr.success('Ticket deleted successfully', 'Success');
        console.log('Ticket deleted successfully:', this.selectedTicket?.id);
        this.getAllTicketsAsSender();
      },
      error: (error) => {
        this.toastr.error('Error deleting ticket', 'Error');
        console.error('Error deleting ticket:', error);
      }
    });
  }

  openTicketDetails(ticket: Ticket) {
    this.selectedTicket = ticket;
    this.isDialogVisible = true;
  }

  closeDialog() {
    this.isDialogVisible = false;
    this.selectedTicket = null;
  }

  editTicket(ticket: Ticket) {
    console.log('Edit ticket:', ticket);
    this.closeDialog();
    this.openEditModal(ticket);
  }

  exportTicketToPDF(ticket: Ticket) {
    console.log('Export ticket to PDF:', ticket);
  }

  onSaveTicket(ticket: Ticket) {
    const newTicket = { ...ticket, id: this.generateId() };
    console.log('Nouveau ticket créé:', newTicket);
  }

  onUpdateTicket(ticket: Ticket) {
    console.log('Ticket mis à jour:', ticket);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }

  getRandomColor(): string {
    const colors = ['007bff', '28a745', 'ffc107', 'dc3545', '6f42c1', '20c997'];
    return colors[Math.floor(Math.random() * colors.length)];
  }

  getPriorityIcon(priority: string): string {
    switch (priority) {
      case 'HIGH': return 'bi-exclamation-triangle-fill';
      case 'MEDIUM': return 'bi-dash-circle-fill';
      case 'LOW': return 'bi-info-circle-fill';
      case 'CRITICAL': return 'bi-bug-fill';
      default: return 'bi-circle-fill';
    }
  }

  getPriorityColor(priority: string): string {
    const colors = {
      'LOW': '#28a745',
      'MEDIUM': '#ffc107',
      'HIGH': '#fd7e14',
      'CRITICAL': '#dc3545'
    };
    return colors[priority as keyof typeof colors] || '#6c757d';
  }

  getPriorityLabel(priority: string): string {
    const labels = {
      'LOW': 'LOW',
      'MEDIUM': 'MEDIUM',
      'HIGH': 'HIGH',
      'CRITICAL': 'CRITICAL'
    };
    return labels[priority as keyof typeof labels] || priority;
  }

  getTypeColor(type: string): string {
    const colors = {
      'BUG': '#dc3545',
      'FEATURE': '#28a745'
    };
    return colors[type as keyof typeof colors] || '#6c757d';
  }

  getTypeLabel(type: string): string {
    const labels = {
      'BUG': 'BUG',
      'FEATURE': 'FEATURE'
    };
    return labels[type as keyof typeof labels] || type;
  }

  getCategoryLabel(category: string): string {
    const labels = {
      'HARDWARE': 'HARDWARE',
      'SOFTWARE': 'SOFTWARE',
      'NETWORK': 'NETWORK',
      'OTHER': 'OTHER'
    };
    return labels[category as keyof typeof labels] || category;
  }

  onSearch(): void {
    console.log('Searching for:', this.searchQuery);
  }

  logout(): void {
    localStorage.clear();
    window.location.href = '/login';
  }

  private showSuccessMessage(message: string) {
    console.log('Success:', message);
  }

  private showErrorMessage(message: string) {
    console.error('Error:', message);
  }


   goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.getAllTicketsAsSender();
    }
  }

  goToFirstPage(): void {
    this.goToPage(1);
  }

  goToLastPage(): void {
    this.goToPage(this.totalPages);
  }

  goToPreviousPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  goToNextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  getPaginationRange(): number[] {
    const range: number[] = [];
    const maxVisiblePages = 3;

    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

    startPage = Math.max(1, endPage - maxVisiblePages + 1);

    for (let i = startPage; i <= endPage; i++) {
      range.push(i);
    }

    return range;
  }

 /* get paginatedTickets() {
    return this.filteredTickets;
  }*/

  get paginationInfo() {
    const start = (this.currentPage - 1) * this.itemsPerPage + 1;
    const end = Math.min(this.currentPage * this.itemsPerPage, this.totalElements);
    return {
      start,
      end,
      total: this.totalElements
    };
  }

}