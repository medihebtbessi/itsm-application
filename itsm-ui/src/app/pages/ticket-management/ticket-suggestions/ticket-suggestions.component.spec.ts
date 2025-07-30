import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketSuggestionsComponent } from './ticket-suggestions.component';

describe('TicketSuggestionsComponent', () => {
  let component: TicketSuggestionsComponent;
  let fixture: ComponentFixture<TicketSuggestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketSuggestionsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketSuggestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
