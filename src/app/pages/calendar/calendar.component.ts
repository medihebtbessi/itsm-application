import {
  CalendarOptions,
  DateSelectArg,
  EventApi,
  EventClickArg,
  EventInput,
} from '@fullcalendar/core';
import { 
  CUSTOM_ELEMENTS_SCHEMA, 
  Component, 
  OnInit, 
  ChangeDetectorRef, 
  signal,
  ViewChild,
  ElementRef
} from '@angular/core';
import { FullCalendarModule, FullCalendarComponent } from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../ticket-management/ticket.service';
import { SideBarAdminComponent } from "../../side-bar-admin/side-bar-admin.component";
import { ChatBotComponent } from "../chat-bot/chat-bot.component";
import { ToastrService } from 'ngx-toastr';

declare var bootstrap: any;

@Component({
  selector: 'app-calendar',
  imports: [
    CommonModule,
    RouterOutlet,
    FormsModule,
    FullCalendarModule, 
    SideBarAdminComponent, 
    ChatBotComponent
  ],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss'
})
export class CalendarComponent implements OnInit {
  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;
  
  calendarVisible = signal(true);
  currentEvents = signal<EventApi[]>([]);
  
  // Stats properties
  totalTickets = 0;
  slaViolations = 0;
  resolvedTickets = 0;
  criticalTickets = 0;
  
  // Filter and control properties
  showSlaAlerts = true;
  currentFilter = 'ALL';
  selectedEvent: EventApi | null = null;
  
  // Calendar data
  allTickets: any[] = [];
  filteredTickets: any[] = [];
  
  calendarOptions = signal<CalendarOptions>({
    plugins: [
      interactionPlugin,
      dayGridPlugin,
      timeGridPlugin,
      listPlugin,
    ],
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek',
    },
    initialView: 'dayGridMonth',
    weekends: true,
    editable: true,
    selectable: true,
    selectMirror: true,
    dayMaxEvents: true,
    height: 'auto',
    eventDisplay: 'block',
    eventClick: this.handleEventClick.bind(this),
    events: (fetchInfo, successCallback, failureCallback) => {
      this.loadCalendarEvents(successCallback, failureCallback);
    },
    eventDidMount: (info) => {
      // Add custom styling and tooltips
      const event = info.event;
      const el = info.el;
      
      // Add tooltip
      el.setAttribute('title', `${event.title} - ${event.extendedProps['status']}`);
      el.setAttribute('data-bs-toggle', 'tooltip');
      
      // Add SLA violation indicator
      if (this.isSlaViolated(event)) {
        el.classList.add('sla-violated');
      }
      
      // Add priority indicator
      const priority = event.extendedProps['priority'];
      if (priority) {
        el.classList.add(`priority-${priority.toLowerCase()}`);
      }
    }
  });

  constructor(
    private changeDetector: ChangeDetectorRef,
    private http: HttpClient,
    private ticketService: TicketService,
    private toastrService: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
    this.setupSlaAlerts();
    this.initializeTooltips();
  }

  private loadInitialData(): void {
    this.loadTicketStats();
    this.loadCalendarData();
  }

  private loadTicketStats(): void {
    const headers = this.getAuthHeaders();
    
    this.http.get<any>('http://localhost:8090/api/v1/dashboard/overview', { headers })
      .subscribe({
        next: (stats) => {
          this.totalTickets = stats.totalTickets || 0;
          this.slaViolations = stats.slaViolations || 0;
          this.resolvedTickets = stats.resolvedTickets || 0;
          this.criticalTickets = stats.openTickets || 0;
        },
        error: (err) => {
          console.error('Error loading ticket stats:', err);
        }
      });
  }

  private loadCalendarData(): void {
    const headers = this.getAuthHeaders();
    
    this.http.get<any[]>('http://localhost:8090/api/v1/ticket/calendar-sla', { headers })
      .subscribe({
        next: (data) => {
          this.allTickets = data;
          this.filteredTickets = [...data];
          this.updateCalendarEvents();
        },
        error: (err) => {
          console.error('Error loading calendar data:', err);
          this.toastrService.error('Failed to load calendar data');
        }
      });
  }

  private loadCalendarEvents(successCallback: Function, failureCallback: Function): void {
    const events = this.filteredTickets.map(ticket => ({
      id: ticket.id,
      title: ticket.title,
      start: ticket.start,
      end: ticket.end,
      backgroundColor: this.getEventColor(ticket.status),
      borderColor: this.getEventBorderColor(ticket.priority),
      textColor: this.getEventTextColor(ticket.status),
      extendedProps: {
        ...ticket,
        description: ticket.description || '',
        priority: ticket.priority || 'MEDIUM',
        assignee: ticket.assignee || 'Unassigned',
        createdAt: ticket.createdAt
      }
    }));
    
    successCallback(events);
  }

  private updateCalendarEvents(): void {
    if (this.calendarComponent) {
      this.calendarComponent.getApi().refetchEvents();
    }
  }

  private setupSlaAlerts(): void {
    this.checkSlaAlerts();
    // Set up periodic SLA checks every 5 minutes
    setInterval(() => {
      if (this.showSlaAlerts) {
        this.checkSlaAlerts();
      }
    }, 5 * 60 * 1000);
  }

  private checkSlaAlerts(): void {
    const now = new Date().getTime();
    
    this.allTickets.forEach(ticket => {
      const due = new Date(ticket.end).getTime();
      const diffInMinutes = (due - now) / 60000;
      
      if (diffInMinutes <= 30 && diffInMinutes > 0 && ticket.status !== 'RESOLVED') {
        this.toastrService.warning(
          `SLA for ticket "${ticket.title}" expires in ${Math.round(diffInMinutes)} minutes.`,
          'SLA Alert',
          { timeOut: 10000 }
        );
      } else if (diffInMinutes <= 0 && ticket.status !== 'RESOLVED') {
        this.toastrService.error(
          `SLA for ticket "${ticket.title}" has been violated!`,
          'SLA Violation',
          { timeOut: 15000 }
        );
      }
    });
  }

  private initializeTooltips(): void {
    // Initialize Bootstrap tooltips
    setTimeout(() => {
      const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
      tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    }, 1000);
  }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    });
  }

  // Event color methods
  getEventColor(status: string): string {
    const colors = {
      'NEW': '#007bff',
      'IN_PROGRESS': '#FFD700',
      'RESOLVED': '#28a745',
      'CLOSED': '#6c757d',
      'CRITICAL': '#dc3545',
      'ON_HOLD': '#ffc107'
    };
    return colors[status as keyof typeof colors] || '#17a2b8';
  }

  getEventBorderColor(priority: string): string {
    const colors = {
      'LOW': '#28a745',
      'MEDIUM': '#ffc107',
      'HIGH': '#fd7e14',
      'URGENT': '#dc3545'
    };
    return colors[priority as keyof typeof colors] || '#6c757d';
  }

  getEventTextColor(status: string): string {
    return status === 'IN_PROGRESS' ? '#000' : '#fff';
  }

  // Badge class methods
  getStatusBadgeClass(status: string): string {
    const classes = {
      'NEW': 'badge-new',
      'IN_PROGRESS': 'badge-in-progress',
      'RESOLVED': 'badge-resolved',
      'CLOSED': 'badge-closed',
      'CRITICAL': 'badge-high',
      'ON_HOLD': 'badge-on-hold'
    };
    return classes[status as keyof typeof classes] || 'badge-secondary';
  }

  getPriorityBadgeClass(priority: string): string {
    const classes = {
      'LOW': 'badge-low',
      'MEDIUM': 'badge-medium',
      'HIGH': 'badge-high',
      'URGENT': 'badge-urgent'
    };
    return classes[priority as keyof typeof classes] || 'badge-medium';
  }

  // Event handlers
  handleEventClick(clickInfo: EventClickArg): void {
    this.selectedEvent = clickInfo.event;
    const modal = new bootstrap.Modal(document.getElementById('eventDetailModal'));
    modal.show();
  }

  // View control methods
  changeView(viewType: string): void {
    if (this.calendarComponent) {
      this.calendarComponent.getApi().changeView(viewType);
    }
  }

  toggleCalendarView(): void {
    this.calendarVisible.set(!this.calendarVisible());
    setTimeout(() => {
      this.calendarVisible.set(true);
      this.updateCalendarEvents();
    }, 100);
  }

  // Filter methods
  filterByStatus(status: string): void {
    this.currentFilter = status;
    
    if (status === 'ALL') {
      this.filteredTickets = [...this.allTickets];
    } else {
      this.filteredTickets = this.allTickets.filter(ticket => ticket.status === status);
    }
    
    this.updateCalendarEvents();
    this.toastrService.info(`Filtered by: ${status}`);
  }

  toggleSlaAlerts(): void {
    if (this.showSlaAlerts) {
      this.toastrService.success('SLA alerts enabled');
    } else {
      this.toastrService.info('SLA alerts disabled');
    }
  }

  // Utility methods
  isSlaViolated(event: EventApi): boolean {
    const now = new Date().getTime();
    const end = new Date(event.endStr).getTime();
    return now > end && event.extendedProps['status'] !== 'RESOLVED';
  }

  // Action methods
  exportCalendar(): void {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(this.filteredTickets));
    const downloadAnchorNode = document.createElement('a');
    downloadAnchorNode.setAttribute("href", dataStr);
    downloadAnchorNode.setAttribute("download", "calendar-export.json");
    document.body.appendChild(downloadAnchorNode);
    downloadAnchorNode.click();
    downloadAnchorNode.remove();
    
    this.toastrService.success('Calendar exported successfully!');
  }

  editTicket(event: EventApi): void {
    // Navigate to edit ticket page
    window.location.href = `/update-ticket/${event.id}`;
  }

  // Refresh methods
  refreshCalendar(): void {
    this.loadInitialData();
    this.toastrService.success('Calendar refreshed!');
  }
}