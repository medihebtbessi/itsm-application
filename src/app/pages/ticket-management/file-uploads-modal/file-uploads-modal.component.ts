import { Component, EventEmitter, Inject, Input, Output } from '@angular/core';
import { FileUploadsService } from './file-uploads.service';
import { CommonModule, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
export interface FileUploadDialogData {
  ticketId: string;
  title?: string;
}

export interface FileUploadInfo {
  file: File;
  uploading: boolean;
  uploaded: boolean;
  error: boolean;
  attachmentId: number | null;
  errorMessage: string | null;
}
@Component({
  selector: 'app-file-uploads-modal',
  imports: [NgIf,CommonModule,FormsModule],
  templateUrl: './file-uploads-modal.component.html',
  styleUrl: './file-uploads-modal.component.scss'
})
export class FileUploadsModalComponent {

 @Input() isVisible: boolean = false;
  
  // Événements de sortie corrigés
  @Output() fileSelected = new EventEmitter<File>();
  @Output() cancel = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<{ file: File | null, dontShowAgain: boolean }>();

  dontShowAgain: boolean = false;
  selectedFile: File | null = null;

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.fileSelected.emit(this.selectedFile);
    }
  }

  onConfirm() {
    this.confirm.emit({
      file: this.selectedFile,
      dontShowAgain: this.dontShowAgain
    });
    this.closeDialog();
  }

  onCancel() {
    this.cancel.emit();
    this.closeDialog();
  }

  private closeDialog() {
    this.isVisible = false;
    this.dontShowAgain = false;
    this.selectedFile = null;
    // Réinitialiser l'input file
    const fileInput = document.getElementById('fileUpload') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
}
