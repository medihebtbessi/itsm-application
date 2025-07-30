import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Ticket, TicketService } from '../ticket.service';
import { CommonModule, NgFor } from '@angular/common';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-ticket-modal',
  imports: [NgFor, CommonModule,ReactiveFormsModule],
  templateUrl: './ticket-modal.component.html',
  styleUrl: './ticket-modal.component.scss'
})
export class TicketModalComponent implements OnInit {
  @Input() isOpen = false;
  @Input() ticket: Ticket | null = null;
  @Input() mode: 'create' | 'edit' = 'create';
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() saveTicket = new EventEmitter<Ticket>();
  @Output() updateTicket = new EventEmitter<Ticket>();

  ticketForm: FormGroup;
  
  priorities = [
    { value: 'LOW', label: 'LOW', color: '#28a745' },
    { value: 'MEDIUM', label: 'MEDIUM', color: '#ffc107' },
    { value: 'HIGH', label: 'HIGH', color: '#fd7e14' },
    { value: 'CRITICAL', label: 'CRITICAL', color: '#dc3545' }
  ];

  statuses = [
    { value: 'NEW', label: 'NEW', color: '#007bff' },
    { value: 'IN_PROGRESS', label: 'IN PROGRESS', color: '#17a2b8' },
    { value: 'RESOLVED', label: 'RESOLVED', color: '#28a745' },
    { value: 'ON_HOLD', label: 'ON HOLD', color: '#6c757d' },
    { value: 'CLOSED', label: 'CLOSED', color: '#343a40' }
  ];

  categories = [
    { value: 'HARDWARE', label: 'HARDWARE', icon: 'fas fa-microchip' },
    { value: 'SOFTWARE', label: 'SOFTWARE', icon: 'fas fa-code' },
    { value: 'NETWORK', label: 'NETWORK', icon: 'fas fa-network-wired' },
    { value: 'OTHER', label: 'OTHER', icon: 'fas fa-question-circle' }
  ];

  types = [
    { value: 'BUG', label: 'BUG', icon: 'fas fa-bug', color: '#dc3545' },
    { value: 'FEATURE', label: 'FEATURE', icon: 'fas fa-plus-circle', color: '#28a745' }
  ];

  constructor(private fb: FormBuilder,private ticketService: TicketService,private toastr: ToastrService) {

    this.ticketForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      priority: ['MEDIUM', Validators.required],
      status: ['NEW', Validators.required],
      category: ['SOFTWARE', Validators.required],
      type: ['BUG', Validators.required]
    });

  }

  ngOnInit() {
    if (this.ticket && this.mode === 'edit') {
      this.ticketForm.patchValue({
        title: this.ticket.title,
        description: this.ticket.description,
        priority: this.ticket.priority || 'MEDIUM',
        status: this.ticket.status || 'NEW',
        category: this.ticket.category || 'SOFTWARE',
        type: this.ticket.type || 'BUG'
      });
                console.log('Ticket loaded for editing:', this.ticket?.title);

      //
    }
    this.ticketForm.patchValue(this.ticket!);


  }

  onClose() {
    this.closeModal.emit();
    this.resetForm();

  }

  onSubmit() {
    if (this.ticketForm.valid) {
      const formValue = this.ticketForm.value;
      
      if (this.mode === 'create') {
        this.ticketService.create(formValue).subscribe({
          next: (ticketId) => {
            this.toastr.success('Ticket créé avec succès', 'Succès');
          },
          error: (error) => {
            this.toastr.error('Erreur lors de la création du ticket', 'Erreur');
            console.error('Erreur lors de la création du ticket:', error);
          }
        }) ;
        
      } else {
        this.ticketService.update(this.ticket?.id || '', formValue).subscribe({
          next: (updatedTicket) => {
            this.toastr.success('Ticket mis à jour avec succès', 'Succès');
          }, error: (error) => {
            this.toastr.error('Erreur lors de la mise à jour du ticket', 'Erreur');
            console.error('Erreur lors de la création du ticket:', error);
          }});
        const updatedTicket = { ...this.ticket, ...formValue };
        this.updateTicket.emit(updatedTicket);
      }
      
      this.onClose();
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched() {
    Object.keys(this.ticketForm.controls).forEach(key => {
      this.ticketForm.get(key)?.markAsTouched();
    });
  }

  private resetForm() {
    this.ticketForm.reset({
      priority: 'MEDIUM',
      status: 'NEW',
      category: 'SOFTWARE',
      type: 'BUG'
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.ticketForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) return `${fieldName} est requis`;
      if (field.errors['minlength']) return `${fieldName} doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
      if (field.errors['maxlength']) return `${fieldName} ne peut pas dépasser ${field.errors['maxlength'].requiredLength} caractères`;
    }
    return '';
  }

  getPriorityColor(priority: string): string {
    return this.priorities.find(p => p.value === priority)?.color || '#6c757d';
  }

  getStatusColor(status: string): string {
    return this.statuses.find(s => s.value === status)?.color || '#6c757d';
  }

  getTypeColor(type: string): string {
    return this.types.find(t => t.value === type)?.color || '#6c757d';
  }
}