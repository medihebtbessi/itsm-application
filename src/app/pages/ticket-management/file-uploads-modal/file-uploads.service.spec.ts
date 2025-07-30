import { TestBed } from '@angular/core/testing';

import { FileUploadsService } from './file-uploads.service';

describe('FileUploadsService', () => {
  let service: FileUploadsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileUploadsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
