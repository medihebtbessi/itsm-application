import { Component, OnInit, OnDestroy } from '@angular/core';
import { DashboardOverviewDTO, DashboardService, Ticket, UserLoadDTO } from '../dashboard.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

import Chart, { BarElement, CategoryScale, DoughnutController, ArcElement, LinearScale, Tooltip, Legend } from 'chart.js/auto';
import { forkJoin } from 'rxjs';
import { ChatBotComponent } from "../../chat-bot/chat-bot.component";

Chart.register(BarElement, CategoryScale, DoughnutController, ArcElement, LinearScale, Tooltip, Legend);

@Component({
  selector: 'app-stats-tickets',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, CommonModule, ChatBotComponent],
  templateUrl: './stats-tickets.component.html',
  styleUrl: './stats-tickets.component.scss'
})
export class StatsTicketsComponent implements OnInit, OnDestroy {

  

  overview: DashboardOverviewDTO = {
    totalTickets: 0,
    openTickets: 0,
    inProgressTickets: 0,
    resolvedTickets: 0,
    averageResolutionTimeInHours: 0
  };

  ticketsByStatus:  Record<string, number> = {};
  ticketsByPriority:Record<string, number> = {};
  ticketsByCategory:Record<string, number> = {};

  urgentTickets: Ticket[] = [];
  userLoad:      UserLoadDTO[] = [];

  loading = {
    overview:       false,
    charts:         false,
    urgentTickets:  false,
    userLoad:       false
  };

 
  private statusChart?:   Chart<'bar'>;
  private priorityChart?: Chart<'bar'>;
  private categoryChart?: Chart<'doughnut'>;

  constructor(private dashboardService: DashboardService) {}


  ngOnInit(): void { this.loadDashboardData(); }

  ngOnDestroy(): void {
    this.statusChart?.destroy();
    this.priorityChart?.destroy();
    this.categoryChart?.destroy();
  }


  private loadDashboardData(): void {
    this.loadOverview();
    this.loadChartData();
    this.loadUrgentTickets();
    this.loadUserLoad();
  }

  private loadOverview(): void {
    this.loading.overview = true;
    this.dashboardService.getOverview().subscribe({
      next: data => this.overview = data,
      error: err => console.error('Error loading overview:', err),
      complete: () => this.loading.overview = false
    });
  }

  private loadChartData(): void {
    this.loading.charts = true;

    forkJoin({
      status:   this.dashboardService.getTicketsByStatus(),
      priority: this.dashboardService.getTicketsByPriority(),
      category: this.dashboardService.getTicketsByCategory()
    }).subscribe({
      next: ({ status, priority, category }) => {
        this.ticketsByStatus   = status;
        this.ticketsByPriority = priority;
        this.ticketsByCategory = category;

        this.buildStatusChart(status);
        this.buildPriorityChart(priority);
        this.buildCategoryChart(category);
      },
      error: err => console.error('Error loading chart data:', err),
      complete: () => this.loading.charts = false
    });
  }

  private loadUrgentTickets(): void {
    this.loading.urgentTickets = true;
    this.dashboardService.getUrgentTickets().subscribe({
      next: data => this.urgentTickets = data,
      error: err => console.error('Error loading urgent tickets:', err),
      complete: () => this.loading.urgentTickets = false
    });
  }

  private loadUserLoad(): void {
    this.loading.userLoad = true;
    this.dashboardService.getLoadByRecipient().subscribe({
      next: data => this.userLoad = data,
      error: err => console.error('Error loading user load:', err),
      complete: () => this.loading.userLoad = false
    });
  }


  private buildStatusChart(src: Record<string, number>): void {
    const labels = ['NEW', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED'];
    const data   = labels.map(l => src[l] ?? 0);
    const canvas = document.getElementById('statusChart') as HTMLCanvasElement | null;
    if (!canvas) return;

    this.statusChart?.destroy();
    this.statusChart = new Chart(canvas, {
      type: 'bar',
      data: { labels, datasets: [{ label: 'Tickets', data, borderWidth: 1 }] },
      options: { responsive: true, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
    });
  }

  private buildPriorityChart(src: Record<string, number>): void {
    const labels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const data   = labels.map(l => src[l] ?? 0);
    const canvas = document.getElementById('priorityChart') as HTMLCanvasElement | null;
    if (!canvas) return;

    this.priorityChart?.destroy();
    this.priorityChart = new Chart(canvas, {
      type: 'bar',
      data: { labels, datasets: [{ label: 'Tickets', data, borderWidth: 1 }] },
      options: { responsive: true, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
    });
  }

  private buildCategoryChart(src: Record<string, number>): void {
    const labels = Object.keys(src);
    const data   = labels.map(l => src[l]);
    const canvas = document.getElementById('categoryChart') as HTMLCanvasElement | null;
    if (!canvas) return;

    this.categoryChart?.destroy();
    this.categoryChart = new Chart(canvas, {
      type: 'doughnut',
      data: { labels, datasets: [{ data, hoverOffset: 4 }] },
      options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
  }


  getPriorityBadgeClass(priority: string): string {
    switch (priority?.toLowerCase()) {
      case 'low':      return 'badge-low';
      case 'medium':   return 'badge-medium';
      case 'high':     return 'badge-high';
      case 'critical': return 'badge-critical';
      default:         return 'bg-secondary';
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'open':        return 'bg-danger';
      case 'in_progress': return 'bg-warning';
      case 'resolved':    return 'bg-success';
      case 'closed':      return 'bg-secondary';
      default:            return 'bg-secondary';
    }
  }

  getStatusIcon(status: string): string {
    switch (status?.toLowerCase()) {
      case 'open':        return 'bi-exclamation-circle';
      case 'in_progress': return 'bi-clock';
      case 'resolved':    return 'bi-check-circle';
      case 'closed':      return 'bi-x-circle';
      default:            return 'bi-question-circle';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  formatTime(hours: number): string {
    if (hours === 0) return 'N/A';
    if (hours < 24)  return `${hours.toFixed(1)}h`;
    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;
    return `${days}d ${remainingHours.toFixed(1)}h`;
  }

  refreshData(): void { this.loadDashboardData(); }
}
