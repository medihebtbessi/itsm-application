import { CommonModule, NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-delete-dialog',
  imports: [NgIf,CommonModule,FormsModule],
  templateUrl: './delete-dialog.component.html',
  styleUrl: './delete-dialog.component.scss'
})
export class DeleteDialogComponent {

  @Input() isVisible: boolean = false;
  @Output() confirmed = new EventEmitter<boolean>();
  @Output() cancelled = new EventEmitter<void>();
  
  dontShowAgain: boolean = false;

  onConfirm() {
    this.confirmed.emit(this.dontShowAgain);
    this.closeDialog();
  }

  onCancel() {
    this.cancelled.emit();
    this.closeDialog();
  }

  private closeDialog() {
    this.isVisible = false;
    this.dontShowAgain = false;
  }
}
