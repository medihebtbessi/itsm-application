import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ActivateAccountComponent } from './pages/activate-account/activate-account.component';
import { ForgetPasswordComponent } from './pages/forget-password/forget-password.component';
import { ModifyingPasswordComponent } from './pages/modifying-password/modifying-password.component';
import { ProfilePageComponent } from './pages/profile-page/profile-page.component';
import { TicketsComponent } from './pages/ticket-management/tickets/tickets.component';
import { TicketFormComponent } from './pages/ticket-management/ticket-form/ticket-form.component';
import { TicketAssignedToMeComponent } from './pages/ticket-management/ticket-assigned-to-me/ticket-assigned-to-me.component';
import { StatsTicketsComponent } from './pages/dashboard/stats-tickets/stats-tickets.component';
import { ChatComponent } from './pages/chat/chat/chat.component';
import { TicketsFrontOfficeComponent } from './pages/ticket-management/tickets-front-office/tickets-front-office.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { CalendarComponent } from './pages/calendar/calendar.component';
import { TicketSuggestionsComponent } from './pages/ticket-management/ticket-suggestions/ticket-suggestions.component';
import { TicketMonitoringComponent } from './pages/ticket-management/ticket-monitoring/ticket-monitoring.component';
import { SolutionStackOverFlowComponent } from './pages/ticket-management/solution-stack-over-flow/solution-stack-over-flow.component';

export const routes: Routes = [
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: 'login' , component: LoginComponent},
  {path: 'register' , component: RegisterComponent},
  {path: 'activation-account' , component: ActivateAccountComponent},
  {path: 'forget-password' , component: ForgetPasswordComponent},
  {path: 'modifying-password' , component: ModifyingPasswordComponent},
  {path: 'profile-info' , component: ProfilePageComponent},
  {path: 'getAllTicket' , component: TicketsComponent},
  {path: 'tickets-assigned-to-me' , component: TicketAssignedToMeComponent},
  {path:'create-ticket',component:TicketFormComponent},
  {path:'update-ticket/:id',component:TicketFormComponent},
  {path:'stats',component:StatsTicketsComponent},
  {path:'messages',component:ChatComponent},
  {path:'tickets',component:TicketsFrontOfficeComponent},
  {path:'user-management',component:UserManagementComponent},
  {path:'calendar',component:CalendarComponent},
  {path:'suggestions/:id',component:TicketSuggestionsComponent},
  {path:'monitoring',component:TicketMonitoringComponent},
  {path: 'solution-stack-over-flow/:id', component: SolutionStackOverFlowComponent},
  {path: '**', redirectTo: 'login' }
];
