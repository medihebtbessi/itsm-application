import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketAssignedToMeComponent } from './ticket-assigned-to-me.component';

describe('TicketAssignedToMeComponent', () => {
  let component: TicketAssignedToMeComponent;
  let fixture: ComponentFixture<TicketAssignedToMeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketAssignedToMeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketAssignedToMeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
