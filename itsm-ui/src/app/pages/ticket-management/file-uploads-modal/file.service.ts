import { Injectable } from '@angular/core';
import { FileUploadInfo, FileUploadsModalComponent } from './file-uploads-modal.component';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileService {

 /* constructor(private dialog: MatDialog) {}

  openUploadDialog(ticketId: string, title?: string): Observable<FileUploadInfo[] | null> {
    const dialogRef = this.dialog.open(FileUploadsModalComponent, {
      width: '550px',
      disableClose: true,
      data: { ticketId, title }
    });

    return dialogRef.afterClosed();
  }*/
}
