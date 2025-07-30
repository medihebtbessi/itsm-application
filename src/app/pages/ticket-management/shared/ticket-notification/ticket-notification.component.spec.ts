import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketNotificationComponent } from './ticket-notification.component';

describe('TicketNotificationComponent', () => {
  let component: TicketNotificationComponent;
  let fixture: ComponentFixture<TicketNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketNotificationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
