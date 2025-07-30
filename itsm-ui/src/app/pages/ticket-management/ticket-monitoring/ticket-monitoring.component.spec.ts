import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketMonitoringComponent } from './ticket-monitoring.component';

describe('TicketMonitoringComponent', () => {
  let component: TicketMonitoringComponent;
  let fixture: ComponentFixture<TicketMonitoringComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketMonitoringComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketMonitoringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
