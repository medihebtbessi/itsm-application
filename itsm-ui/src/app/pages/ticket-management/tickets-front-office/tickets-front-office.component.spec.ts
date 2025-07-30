import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketsFrontOfficeComponent } from './tickets-front-office.component';

describe('TicketsFrontOfficeComponent', () => {
  let component: TicketsFrontOfficeComponent;
  let fixture: ComponentFixture<TicketsFrontOfficeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsFrontOfficeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketsFrontOfficeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
