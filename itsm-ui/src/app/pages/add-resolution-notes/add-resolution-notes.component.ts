import { CommonModule, NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-add-resolution-notes',
  imports: [NgIf,CommonModule,FormsModule],
  templateUrl: './add-resolution-notes.component.html',
  styleUrl: './add-resolution-notes.component.scss'
})
export class AddResolutionNotesComponent {

   @Input() isVisible: boolean = false;
  @Output() confirmed = new EventEmitter<{notes: string, dontShowAgain: boolean}>();
  @Output() cancelled = new EventEmitter();
  
  resolutionNotes: string = '';
  dontShowAgain: boolean = false;

  onConfirm() {
    this.confirmed.emit({
      notes: this.resolutionNotes,
      dontShowAgain: this.dontShowAgain
    });
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
