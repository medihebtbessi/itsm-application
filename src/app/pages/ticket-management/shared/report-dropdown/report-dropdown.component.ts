import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-report-dropdown',
  imports: [CommonModule, FormsModule],
  templateUrl: './report-dropdown.component.html',
  styleUrl: './report-dropdown.component.scss'
})
export class ReportDropdownComponent {
  showDatePickers = false;
  startDate: string = '';
  endDate: string = '';

  @Output() monthlyReport = new EventEmitter<void>();
  @Output() customReport = new EventEmitter<{ startDate: string, endDate: string }>();
  @Output() quickAnalysis = new EventEmitter<{ startDate?: string, endDate?: string }>();

  onGenerateMonthlyReport() {
    this.monthlyReport.emit();
    this.closeDatePickers();
  }

  onShowCustomReportForm() {
    this.showDatePickers = true;
    // Initialiser avec des dates par défaut si nécessaire
    this.setDefaultDates();
  }

  onValidateCustomReport() {
    if (this.startDate && this.endDate) {
      // Validation des dates
      if (this.isValidDateRange()) {
        this.customReport.emit({ 
          startDate: this.startDate, 
          endDate: this.endDate 
        });
        this.reset();
      } else {
        // Vous pouvez ajouter ici une logique de notification d'erreur
        console.warn('Date de fin antérieure à la date de début');
      }
    }
  }

  onQuickAnalysis() {
    this.quickAnalysis.emit({ 
      startDate: this.startDate || undefined, 
      endDate: this.endDate || undefined 
    });
    this.reset();
  }

  reset() {
    this.showDatePickers = false;
    this.startDate = '';
    this.endDate = '';
  }

  private closeDatePickers() {
    this.showDatePickers = false;
  }

  private setDefaultDates() {
    // Définir la date de début comme le premier jour du mois actuel
    const now = new Date();
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    
    // Formater pour datetime-local
    this.startDate = this.formatDateForInput(firstDayOfMonth);
    this.endDate = this.formatDateForInput(lastDayOfMonth);
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private isValidDateRange(): boolean {
    if (!this.startDate || !this.endDate) return false;
    
    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    
    return start <= end;
  }

  // Getter pour vérifier si le formulaire est valide
  get isFormValid(): boolean {
    return this.startDate !== '' && this.endDate !== '' && this.isValidDateRange();
  }
}