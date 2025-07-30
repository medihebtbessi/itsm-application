import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SolutionStackOverFlowComponent } from './solution-stack-over-flow.component';

describe('SolutionStackOverFlowComponent', () => {
  let component: SolutionStackOverFlowComponent;
  let fixture: ComponentFixture<SolutionStackOverFlowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SolutionStackOverFlowComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SolutionStackOverFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
