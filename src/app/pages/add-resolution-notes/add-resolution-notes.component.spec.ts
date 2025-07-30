import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddResolutionNotesComponent } from './add-resolution-notes.component';

describe('AddResolutionNotesComponent', () => {
  let component: AddResolutionNotesComponent;
  let fixture: ComponentFixture<AddResolutionNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddResolutionNotesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddResolutionNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
