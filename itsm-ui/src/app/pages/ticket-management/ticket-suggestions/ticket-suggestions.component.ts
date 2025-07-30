import { Component, OnInit } from '@angular/core';
import { Ticket, TicketService, TicketSuggestion, TicketSuggestionRequest } from '../ticket.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-ticket-suggestions',
  imports: [CommonModule, NgFor, NgIf],
  templateUrl: './ticket-suggestions.component.html',
  styleUrl: './ticket-suggestions.component.scss'
})
export class TicketSuggestionsComponent implements OnInit {
  suggestions: TicketSuggestion[] = [];
  ticket: Ticket | undefined;
  isLoading: boolean = false;
  
  constructor(private suggestionService: TicketService, private ac: ActivatedRoute) {}

  ngOnInit(): void {
    const id = this.ac.snapshot.params['id'];
    this.isLoading = true;

    this.suggestionService.findById(id).subscribe({
      next: (data) => {
        this.ticket = data;
        const request: TicketSuggestionRequest = {
          title: this.ticket?.title!,
          description: this.ticket?.description!,
          excludeId: this.ticket?.id!,
          limit: 5
        };
        
        this.suggestionService.getSuggestions(request).subscribe({
          next: (data) => {
            this.suggestions = data;
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Erreur de récupération des suggestions :', err);
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Erreur de récupération du ticket :', err);
        this.isLoading = false;
      }
    });
  }
}