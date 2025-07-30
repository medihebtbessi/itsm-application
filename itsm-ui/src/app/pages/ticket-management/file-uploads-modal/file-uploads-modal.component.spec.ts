import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FileUploadsModalComponent } from './file-uploads-modal.component';

describe('FileUploadsModalComponent', () => {
  let component: FileUploadsModalComponent;
  let fixture: ComponentFixture<FileUploadsModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileUploadsModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FileUploadsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
