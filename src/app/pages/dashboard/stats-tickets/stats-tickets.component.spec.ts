import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatsTicketsComponent } from './stats-tickets.component';

describe('StatsTicketsComponent', () => {
  let component: StatsTicketsComponent;
  let fixture: ComponentFixture<StatsTicketsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatsTicketsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StatsTicketsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
