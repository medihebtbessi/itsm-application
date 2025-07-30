import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifyingPasswordComponent } from './modifying-password.component';

describe('ModifyingPasswordComponent', () => {
  let component: ModifyingPasswordComponent;
  let fixture: ComponentFixture<ModifyingPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModifyingPasswordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModifyingPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
