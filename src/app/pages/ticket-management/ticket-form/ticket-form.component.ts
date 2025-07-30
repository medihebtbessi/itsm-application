import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Ticket, TicketService } from '../ticket.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ChatBotComponent } from "../../chat-bot/chat-bot.component";

@Component({
  selector: 'app-ticket-form',
  imports: [FormsModule, CommonModule, ReactiveFormsModule, ChatBotComponent],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.scss'
})
export class TicketFormComponent implements OnInit {
   @Input() ticket: Ticket | null = null;
  @Input() isVisible: boolean = false;
  @Output() onSave = new EventEmitter<Ticket>();
  @Output() onCancel = new EventEmitter<void>();

  ticketForm: FormGroup;
  isEditMode: boolean = false;
   priorityOptions = [
    { value: 'LOW', label: 'Low', class: 'badge-low' },
    { value: 'MEDIUM', label: 'Medium', class: 'badge-medium' },
    { value: 'HIGH', label: 'High', class: 'badge-high' },
    { value: 'CRITICAL', label: 'Critical', class: 'badge-urgent' }
  ];

 statusOptions = [
  { value: 'NEW', label: 'New', class: 'badge-new' },
  { value: 'IN_PROGRESS', label: 'In Progress', class: 'badge-in-progress' },
  { value: 'ON_HOLD', label: 'On Hold', class: 'badge-on-hold' },
  { value: 'RESOLVED', label: 'Resolved', class: 'badge-resolved' },
  { value: 'CLOSED', label: 'Closed', class: 'badge-closed' }
];

  categoryOptions = [
    { value: 'HARDWARE', label: 'Hardware' },
    { value: 'SOFTWARE', label: 'Software' },
    { value: 'NETWORK', label: 'Network' },
    { value: 'OTHER', label: 'Other' },
  ];
  typeOptions = [
    { value: 'BUG', label: 'Bug' },
    {value:'FEATURE', label: 'Feature' },
  ];

  constructor(private ticketService:TicketService,private fb: FormBuilder,private ac:ActivatedRoute,private router:Router,private toastr: ToastrService) {
    this.ticketForm = this.createForm();
  }
  ngOnInit(): void {
    const id = this.ac.snapshot.paramMap.get('id');
    if(id){
    this.ticketService.findById(id!).subscribe({
      next: (ticket) => {
        if (ticket) {
          this.ticket = ticket;
          this.populateForm(ticket);
          this.isEditMode = true;
        } else {
          this.isEditMode = false;
          this.resetForm();
        }
        this.toastr.success('Ticket loaded successfully', 'Success');
      },
      error: (error) => {
        this.toastr.error('Error loading ticket', 'Error');
        console.error('Error fetching ticket:', error);
        this.isEditMode = false;
        this.resetForm();
      }
    });
  }
  }
  private createForm(): FormGroup {
    return this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      priority: ['MEDIUM', Validators.required],
      status: ['NEW', Validators.required],
      category: ['SOFTWARE', Validators.required],
      type: ['BUG', Validators.required],
      resolution_notes: ['', Validators.maxLength(300)]
    });
  }
  
 ngOnChanges(): void {
    if (this.ticket) {
      this.isEditMode = true;
      this.populateForm(this.ticket);
    } else {
      this.isEditMode = false;
      this.resetForm();
    }
  }
  private setupForm(): void {
    this.ticketForm.get('status')?.valueChanges.subscribe(status => {
      const resolutionNotesControl = this.ticketForm.get('resolution_notes');
      if (status === 'Resolved' || status === 'Closed') {
        resolutionNotesControl?.setValidators([Validators.required, Validators.maxLength(300)]);
      } else {
        resolutionNotesControl?.setValidators([Validators.maxLength(300)]);
      }
      resolutionNotesControl?.updateValueAndValidity();
    });
  }

  private populateForm(ticket: Ticket): void {
    this.ticketForm.patchValue({
      title: ticket.title,
      description: ticket.description,
      priority: ticket.priority,
      status: ticket.status,
      category: ticket.category,
      resolution_notes: ticket.resolution_notes || ''
    });
  }

  private resetForm(): void {
    this.ticketForm.reset({
      title: '',
      description: '',
      priority: 'MEDIUM',
      status: 'NEW',
      category: 'SOFTWARE',
      type: 'BUG',
      resolutionNotes: ''
    });
  }


  createTicket(ticketData: any) {
    this.ticketService.create(ticketData).subscribe({
      next: (response) => {
        this.router.navigate(["/getAllTicket"]);
        this.toastr.success('Ticket created successfully', 'Success');
        console.log('Ticket created successfully:', response);
      },
      error: (error) => {
        this.toastr.error('Error creating ticket', 'Error');
        console.error('Error creating ticket:', error);
      }
    });
  }

  updateTicket(ticketId: string, ticketData: any) {
    this.ticketService.update(ticketId, ticketData).subscribe({
      next: (response) => {
        this.router.navigate(["/getAllTicket"]);
        this.toastr.success('Ticket updated successfully', 'Success');
        console.log('Ticket updated successfully:', response);
      },
      error: (error) => {
        this.toastr.error('Error updating ticket', 'Error');
        console.error('Error updating ticket:', error);
      }
    });
  }

  
  onSubmit(): void {
    if (this.ticketForm.valid) {
      const formValue = this.ticketForm.value;
      const ticketData: Ticket = {
        ...formValue,
        id: this.ticket?.id
      };

      if (!ticketData.resolution_notes?.trim()) {
        delete ticketData.resolution_notes;
      }
      if (this.isEditMode && this.ticket) {
        this.updateTicket(this.ticket.id!, ticketData);
      } else {
        this.createTicket(ticketData);
      }

      this.onSave.emit(ticketData);
      this.resetForm();
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancelClick(): void {
    this.resetForm();
    this.onCancel.emit();
    this.router.navigate(['/getAllTicket']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.ticketForm.controls).forEach(key => {
      const control = this.ticketForm.get(key);
      control?.markAsTouched();
    });
  }

  get title() { return this.ticketForm.get('title'); }
  get description() { return this.ticketForm.get('description'); }
  get priority() { return this.ticketForm.get('priority'); }
  get status() { return this.ticketForm.get('status'); }
  get category() { return this.ticketForm.get('category'); }
  get type() { return this.ticketForm.get('type'); }
  get resolution_notes() { return this.ticketForm.get('resolution_notes'); }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.ticketForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.ticketForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['minlength']) return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
      if (field.errors['maxlength']) return `${fieldName} cannot exceed ${field.errors['maxlength'].requiredLength} characters`;
    }
    return '';
  }

  shouldShowResolutionNotes(): boolean {
    const status = this.ticketForm.get('status')?.value;
    return status === 'RESOLVED' || status === 'CLOSED';
  }

  getPriorityBadgeClass(priority: string): string {
    const option = this.priorityOptions.find(p => p.value === priority);
    return option?.class || '';
  }

  getStatusBadgeClass(status: string): string {
    const option = this.statusOptions.find(s => s.value === status);
    return option?.class || '';
  }

  /*openAttachmentUpload(ticketId: string) {
    this.fileUploadService.openUploadDialog(ticketId, 'Upload Ticket Attachments')
      .subscribe(result => {
        if (result && result.length > 0) {
          console.log('Files uploaded successfully:', result);
          // Rafraîchir la liste des attachments du ticket
          this.loadTicketAttachments(ticketId);
        }
      });
  }*/

      

}
