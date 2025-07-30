import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SolutionSearchService } from '../solution-search.service';
import { CommonModule } from '@angular/common';
import { Tick } from 'chart.js';
import { TicketService } from '../ticket.service';
import { ActivatedRoute } from '@angular/router';
export interface TicketDto {
  title: string;
  description: string;
}

export interface Solution {
  title: string;
  link: string;
  score: string;
  answer_count: string;
}
@Component({
  selector: 'app-solution-stack-over-flow',
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './solution-stack-over-flow.component.html',
  styleUrl: './solution-stack-over-flow.component.scss'
})
export class SolutionStackOverFlowComponent implements OnInit {
ticketForm: FormGroup;
  solutions: Solution[] = [];
  isLoading = false;
  hasSearched = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private solutionService: SolutionSearchService,
    private ticketService:TicketService,
    private act:ActivatedRoute
  ) {
    this.ticketForm = this.fb.group({
      title: [null, [Validators.required]],
      description: [null, [Validators.required]]
    });
  }

  ngOnInit(): void {
    const id = this.act.snapshot.paramMap.get('id');
    this.ticketService.findById(id!).subscribe({
      next: (ticket) => {
        this.ticketForm.patchValue({
          title: ticket.title,
          description: ticket.description
        });
      },
      error: (error) => {
        console.error('Erreur lors de la récupération du ticket:', error);
        this.errorMessage = 'Erreur lors de la récupération du ticket.';
      }
    })
  }

  onSubmit(): void {
    if (this.ticketForm.valid) {
      this.searchSolutions();
    } else {
      this.markFormGroupTouched();
    }
  }

  searchSolutions(): void {
    this.isLoading = true;
    this.hasSearched = false;
    this.errorMessage = '';
    this.solutions = [];

    const ticketData: TicketDto = {
      title: this.ticketForm.get('title')?.value,
      description: this.ticketForm.get('description')?.value
    };

    this.solutionService.searchSolutions(ticketData).subscribe({
      next: (response: Solution[]) => {
        this.solutions = response;
        this.isLoading = false;
        this.hasSearched = true;
      },
      error: (error) => {
        console.error('Erreur lors de la recherche:', error);
        this.errorMessage = 'Erreur lors de la recherche des solutions. Vérifiez que le serveur backend est accessible.';
        this.isLoading = false;
        this.hasSearched = true;
      }
    });
  }

  openSolution(link: string): void {
    window.open(link, '_blank');
  }

  private markFormGroupTouched(): void {
    Object.keys(this.ticketForm.controls).forEach(key => {
      const control = this.ticketForm.get(key);
      control?.markAsTouched();
    });
  }

  get titleControl() { return this.ticketForm.get('title'); }
  get descriptionControl() { return this.ticketForm.get('description'); }
}



