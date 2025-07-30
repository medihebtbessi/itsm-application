import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Ticket, TicketService } from '../ticket.service';
import { Router } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { AddResolutionNotesComponent } from "../../add-resolution-notes/add-resolution-notes.component";
import { TicketDetailsComponent } from "../ticket-details/ticket-details.component";
import { ToastrService } from 'ngx-toastr';
import { ChatBotComponent } from "../../chat-bot/chat-bot.component";
import { User, UserService } from '../../user/user.service';
import { FileUploadsService } from '../file-uploads-modal/file-uploads.service';
import { FileUploadsModalComponent } from "../file-uploads-modal/file-uploads-modal.component";
import { ReportDropdownComponent } from "../shared/report-dropdown/report-dropdown.component";
import { ReportService } from '../../../services/report.service';
import { TicketNotificationComponent } from "../shared/ticket-notification/ticket-notification.component";

@Component({
  selector: 'app-ticket-assigned-to-me',
  imports: [FormsModule, CommonModule, AddResolutionNotesComponent, TicketDetailsComponent, ChatBotComponent, FileUploadsModalComponent, ReportDropdownComponent, TicketNotificationComponent],
  templateUrl: './ticket-assigned-to-me.component.html',
  styleUrl: './ticket-assigned-to-me.component.scss'
})
export class TicketAssignedToMeComponent implements OnInit{
stackOverFlowTickets(_t139: Ticket) {
  this.router.navigate(['/solution-stack-over-flow/'+_t139.id]);
}

   searchTerm: string = '';
  selectedStatus: string = '';
  selectedPriority: string = '';
  selectedAssignedTo: string = '';
  selectedCategory: string = '';
  selectedTicketType: string = '';
   tickets: Ticket[] = [];
   currentPage = 1;
  itemsPerPage = 5;
  totalElements = 0; 
  totalPages = 0;  
   isEditing: boolean = false;
  selectedUserId: number |undefined = undefined;
  availableUsers: User[] = [];  

  constructor(private ticketService:TicketService,private router:Router,private toastr: ToastrService,private userService:UserService ,   private fileService: FileUploadsService
,private reportService:ReportService) {}
  ngOnInit(): void {
  this.getAllTickets();  
  this.loadAvailableUsers();
}
suggestionsTickets(_t138: Ticket) {
  this.router.navigate(['/suggestions/'+_t138.id])
}
filteredTickets: Ticket[] = [];
resolutionNotes: string = '';
 resolveTicket(ticketId: string, resolutionNotes: string): void {
    this.ticketService.resolve(ticketId, resolutionNotes).subscribe({
      next: (response) => {
        this.getAllTickets();
        this.toastr.success('Ticket resolved successfully', 'Success');
        console.log('Ticket resolved successfully:', response);
      },
      error: (error) => {
        this.toastr.error('Failed to resolve ticket', 'Error');
        console.error('Error resolving ticket:', error);
      }
    });
  }
filterTickets(): void {
    this.filteredTickets = this.tickets.filter(ticket => {
      const matchesSearch = !this.searchTerm || 
        ticket.title.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.selectedStatus || ticket.status === this.selectedStatus;
      const matchesPriority = !this.selectedPriority || ticket.priority === this.selectedPriority;
      const matchesAssignedTo = !this.selectedAssignedTo || ticket.recipient?.fullName === this.selectedAssignedTo;
      const matchesCategory = !this.selectedCategory || ticket.category === this.selectedCategory;
      const matchesTicketType = !this.selectedTicketType || ticket.type === this.selectedTicketType;
      
      return matchesSearch && matchesStatus && matchesPriority && matchesAssignedTo && matchesCategory&& matchesTicketType;
    });
  }

  getAllTickets() {
    const pageIndex = this.currentPage - 1;
    this.ticketService.getAsRecipient(pageIndex, this.itemsPerPage).subscribe({
      next: (data) => {
        this.tickets = data.content;
        this.filteredTickets = [...this.tickets]; 
        
         
        this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
        this.toastr.success('Tickets fetched successfully', 'Success');
        console.log('Tickets fetched successfully:', data);
      },
      error: (error) => {
        this.toastr.error('Failed to fetch tickets', 'Error');
        console.error('Error fetching tickets:', error);
      }
    });
  }

  deleteTicket(ticketId: string) {
    this.ticketService.delete(ticketId).subscribe({
      next: () => {
        this.toastr.success('Ticket deleted successfully', 'Success');
         // Refresh the ticket list after deletion
        console.log('Ticket deleted successfully');
        this.getAllTickets(); 
      },
      error: (error) => {
        this.toastr.error('Failed to delete ticket', 'Error');
         // Handle error appropriately
        console.error('Error deleting ticket:', error);
      }
    });
  }

 

  updateTicket(id: string) {
    this.router.navigate(['/update-ticket/', id]);
  }
  createTicket() {
    this.router.navigate(['/create-ticket']);
  }

  getTypeIcon(type: string): string {
    return type === 'BUG' ? '🐛' : '⭐';
  }
 clearFilters(): void {
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.selectedAssignedTo = '';
    this.selectedCategory = '';
    this.selectedTicketType = '';
    this.searchTerm = '';
    this.filteredTickets = [...this.tickets];
  }
  onSearch(): void {
    this.filterTickets();
  }

  onFilterChange(): void {
    this.filterTickets();
  }
 getPriorityClass(priority: string): string {
  switch (priority) {
    case 'LOW': return 'badge-LOW';
    case 'MEDIUM': return 'badge-MEDIUM';
    case 'HIGH': return 'badge-HIGH';
    case 'CRITICAL': return 'badge-CRITICAL';
    default: return '';
  }
}

  getStatusClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'badge-open';
      case 'IN_PROGRESS': return 'badge-in-progress';
      case 'CLOSED': return 'badge-closed';
      case 'RESOLVED': return 'badge-resolved';
      default: return '';
    }
  }




 exportToPDF(ticket: Ticket): void {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  });

  const today = new Date().toLocaleDateString();
  const currentTime = new Date().toLocaleTimeString();

  const colors = {
    primary: [41, 128, 185] as [number, number, number],    // Bleu moderne
    secondary: [52, 73, 94] as [number, number, number],    // Gris foncé
    success: [39, 174, 96] as [number, number, number],     // Vert pour status resolved
    warning: [241, 196, 15] as [number, number, number],    // Jaune pour priority
    light: [236, 240, 241] as [number, number, number],     // Gris clair pour backgrounds
    text: [44, 62, 80] as [number, number, number],         // Texte principal
    muted: [149, 165, 166] as [number, number, number]      // Texte secondaire
  };

  doc.setFillColor(...colors.primary);
  doc.rect(0, 0, 210, 25, 'F');

  doc.setFont("helvetica", "bold");
  doc.setFontSize(20);
  doc.setTextColor(255, 255, 255);
  doc.text('Ticket Details', 14, 12);

  doc.setFontSize(10);
  doc.setTextColor(255, 255, 255);
  doc.text(`Live Preview - Generated on ${today} at ${currentTime}`, 14, 20);

  let yPosition = 40;

  doc.setFillColor(...colors.light);
  doc.rect(10, yPosition - 5, 190, 15, 'F');
  
  doc.setFont("helvetica", "bold");
  doc.setFontSize(16);
  doc.setTextColor(...colors.text);
  doc.text(`${ticket.title || 'new ticket'}`, 14, yPosition + 5);

  yPosition += 25;

  if (ticket.description) {
    doc.setFont("helvetica", "normal");
    doc.setFontSize(11);
    doc.setTextColor(...colors.secondary);
    doc.text(ticket.description, 14, yPosition);
    yPosition += 15;
  }

  yPosition += 5;
  
  let xPosition = 14;
  
  doc.setFillColor(240, 240, 240);
  doc.rect(xPosition, yPosition - 3, 25, 8, 'F');
  doc.setFont("helvetica", "normal");
  doc.setFontSize(9);
  doc.setTextColor(...colors.muted);
  doc.text('NETWORK', xPosition + 2, yPosition + 2);
  
  xPosition += 30;
  
  doc.setFillColor(240, 240, 240);
  doc.rect(xPosition, yPosition - 3, 18, 8, 'F');
  doc.text('BUG', xPosition + 2, yPosition + 2);

  yPosition += 20;

  doc.setFillColor(250, 250, 250);
  doc.rect(10, yPosition, 190, 80, 'F');
  doc.setDrawColor(220, 220, 220);
  doc.rect(10, yPosition, 190, 80, 'S');

  yPosition += 15;

  doc.setFont("helvetica", "bold");
  doc.setFontSize(12);
  doc.setTextColor(...colors.text);
  doc.text('Ticket Information', 14, yPosition);

  yPosition += 15;

  const leftColumnData = [
    ['Ticket ID:', ticket.id || 'N/A'],
    ['Created Date:', ticket.createdDate ? new Date(ticket.createdDate).toLocaleDateString() : 'N/A'],
    ['Category:', ticket.category || 'N/A'],
    ['Type:', ticket.type || 'N/A']
  ];

  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);

  leftColumnData.forEach(([label, value]) => {
    doc.setTextColor(...colors.muted);
    doc.text(label, 14, yPosition);
    doc.setTextColor(...colors.text);
    doc.text(value, 14, yPosition + 4);
    yPosition += 12;
  });

  let rightYPosition = yPosition - (leftColumnData.length * 12);

  doc.setFont("helvetica", "bold");
  doc.setFontSize(9);
  const priorityColor: [number, number, number] = ticket.priority === 'HIGH' ? [231, 76, 60] : 
                       ticket.priority === 'MEDIUM' ? colors.warning : 
                       [39, 174, 96]; 
  
  doc.setFillColor(...priorityColor);
  doc.rect(120, rightYPosition - 2, 35, 10, 'F');
  doc.setTextColor(255, 255, 255);
  doc.text(`${ticket.priority || 'LOW'}`, 125, rightYPosition + 4);
  
  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.setTextColor(...colors.muted);
  doc.text('PRIORITY LEVEL', 120, rightYPosition - 5);

  rightYPosition += 25;

  const statusColor: [number, number, number] = ticket.status === 'RESOLVED' ? colors.success : 
                     ticket.status === 'IN_PROGRESS' ? colors.warning : 
                     colors.primary;
  
  doc.setFont("helvetica", "bold");
  doc.setFontSize(9);
  doc.setFillColor(...statusColor);
  doc.rect(120, rightYPosition - 2, 35, 10, 'F');
  doc.setTextColor(255, 255, 255);
  doc.text(`${ticket.status || 'OPEN'}`, 125, rightYPosition + 4);
  
  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.setTextColor(...colors.muted);
  doc.text('CURRENT STATUS', 120, rightYPosition - 5);

  yPosition += 20;

  if (ticket.recipient?.fullName || ticket.sender?.fullName) {
    yPosition += 10;
    doc.setFillColor(248, 249, 250);
    doc.rect(10, yPosition, 190, 30, 'F');
    
    yPosition += 12;
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(...colors.text);
    doc.text('Assignment', 14, yPosition);
    
    yPosition += 10;
    doc.setFont("helvetica", "normal");
    doc.setFontSize(10);
    
    if (ticket.sender?.fullName) {
      doc.setTextColor(...colors.muted);
      doc.text('Created by:', 14, yPosition);
      doc.setTextColor(...colors.text);
      doc.text(ticket.sender.fullName, 50, yPosition);
    }
    
    if (ticket.recipient?.fullName) {
      doc.setTextColor(...colors.muted);
      doc.text('Assigned to:', 120, yPosition);
      doc.setTextColor(...colors.text);
      doc.text(ticket.recipient.fullName, 160, yPosition);
    }
    
    yPosition += 15;
  }

  if (ticket.resolution_notes || this.resolutionNotes) {
    yPosition += 15;
    doc.setFillColor(245, 245, 245);
    doc.rect(10, yPosition, 190, 40, 'F');
    
    yPosition += 12;
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(...colors.text);
    doc.text('Resolution Notes', 14, yPosition);
    
    yPosition += 10;
    doc.setFont("helvetica", "normal");
    doc.setFontSize(10);
    doc.setTextColor(...colors.secondary);
    
    const notes = ticket.resolution_notes || this.resolutionNotes;
    const splitNotes = doc.splitTextToSize(notes, 170);
    doc.text(splitNotes, 14, yPosition);
  }

  const pageCount = doc.getNumberOfPages();
  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);
    
    doc.setDrawColor(...colors.light);
    doc.line(14, doc.internal.pageSize.getHeight() - 20, 196, doc.internal.pageSize.getHeight() - 20);
    
    doc.setFontSize(8);
    doc.setTextColor(...colors.muted);
    
    doc.text(
      `Page ${i} of ${pageCount}`,
      doc.internal.pageSize.getWidth() / 2,
      doc.internal.pageSize.getHeight() - 12,
      { align: 'center' }
    );
    
    doc.text(
      'Confidential - For Internal Use Only',
      doc.internal.pageSize.getWidth() / 2,
      doc.internal.pageSize.getHeight() - 6,
      { align: 'center' }
    );
  }

  const dateStr = new Date().toISOString().slice(0, 10);
  const fileName = `ticket-${ticket.id || 'unknown'}-${dateStr}.pdf`;
  doc.save(fileName);
}
  selectedTicket: Ticket | null = null;


  showResolutionDialog= false;

 openResolutionDialog(ticket: Ticket) {
    this.selectedTicket = ticket;
    this.showResolutionDialog = true;
  }
  
  onResolutionConfirmed(data: {notes: string, dontShowAgain: boolean}) {
    console.log('Resolution confirmed');
    console.log('Notes:', data.notes);
    console.log('Don\'t show again:', data.dontShowAgain);
    
    this.resolveTicket(this.selectedTicket?.id!, data.notes);
    this.showResolutionDialog = false;
    this.selectedTicket = null;
  }
  
  onResolutionCancelled() {
    console.log('Resolution cancelled');
    this.showResolutionDialog = false;
    this.selectedTicket = null;
  }
  
  isDialogVisible = false;

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
  }

  exportTicketToPDF(ticket: Ticket) {
    console.log('Export ticket to PDF:', ticket);
  }


   goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.getAllTickets();
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
    const maxVisiblePages = 5;

    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

    startPage = Math.max(1, endPage - maxVisiblePages + 1);

    for (let i = startPage; i <= endPage; i++) {
      range.push(i);
    }

    return range;
  }

  get paginatedTickets() {
    return this.filteredTickets;
  }

  get paginationInfo() {
    const start = (this.currentPage - 1) * this.itemsPerPage + 1;
    const end = Math.min(this.currentPage * this.itemsPerPage, this.totalElements);
    return {
      start,
      end,
      total: this.totalElements
    };
  }

  editingTicketId: string | null = null;
currentTicketIndex: number = -1;


startEditing(ticketId: string, ticketIndex: number): void {
  this.editingTicketId = ticketId;
  this.currentTicketIndex = ticketIndex;
  this.selectedUserId = this.tickets[ticketIndex].recipient?.id || undefined;
  this.isEditing=true;
  
  setTimeout(() => {
    const selectElement = document.querySelector('select') as HTMLSelectElement;
    if (selectElement) {
      selectElement.focus();
    }
  });
}

assignTicket(ticketIndex: number): void {
  const currentTicket = this.tickets[ticketIndex];
  
  if (this.selectedUserId === currentTicket.recipient?.id) {
    this.cancelEditing();
    return;
  }

  const selectedUser = this.availableUsers.find(user => user.id === this.selectedUserId);
  
  this.ticketService.assignToUser(this.selectedUserId!, currentTicket.id!).subscribe({
    next: (updatedTicket) => {
      const userLight = selectedUser
        ? { id: selectedUser.id, fullName: selectedUser.firstname +' '+selectedUser.lastname, email: selectedUser.email }
        : undefined;
      this.tickets[ticketIndex].recipient = userLight;
      const filteredIndex = this.filteredTickets.findIndex(t => t.id === currentTicket.id);
      this.isEditing=false;
      this.getAllTickets();
      if (filteredIndex !== -1) {
        this.filteredTickets[filteredIndex].recipient = userLight || undefined;
      }
      this.cancelEditing();
      this.toastr.success('Ticket assigné avec succès', 'Success');
      console.log('Ticket assigné avec succès');
    },
    error: (error) => {
      console.error('Erreur lors de l\'assignation:', error);
      this.toastr.error('Erreur lors de l\'assignation', 'Error');
      this.cancelEditing();
    }
  });
}

cancelEditing(): void {
  this.editingTicketId = null;
  this.currentTicketIndex = -1;
  this.selectedUserId = undefined;
  this.isEditing=false;
}
  loadAvailableUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.availableUsers = users;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des utilisateurs:', error);
      }
    });
  }



   isFileUploadModalVisible: boolean = false;

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
        this.getAllTickets();
        //this.showSuccessMessage('Fichier uploadé avec succès');
      },
      error: (error) => {
        this.toastr.error('Error uploading file', 'Error');
        console.error('Error uploading file:', error);
        //this.showErrorMessage('Erreur lors de l\'upload du fichier');
      }
    });
  }


handleMonthlyReport() {
  this.reportService.generateMonthlyReport().subscribe({
    next:()=>{
      this.toastr.success("Votre rapport a été généré. Vous pouvez aprés 4 min le consulter.");
    }
  });
}

handleCustomReport(event: { startDate: string; endDate: string }) {
  this.reportService.generateCustomReport(event.startDate, event.endDate).subscribe({
    next:()=>{
      this.toastr.success("Votre rapport a été généré. Vous pouvez aprés 4 min le consulter.");
    }
  });
}

handleQuickAnalysis(event: { startDate?: string; endDate?: string }) {
  this.reportService.getQuickAnalysis(event.startDate, event.endDate).subscribe(console.log);
}

}



